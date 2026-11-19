package com.taskflow.service;

import com.taskflow.dto.RegisterRequest;
import com.taskflow.dto.UserResponse;
import com.taskflow.entity.Role;
import com.taskflow.entity.RoleName;
import com.taskflow.entity.User;
import com.taskflow.exception.ConflictException;
import com.taskflow.repository.RoleRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private EmailService emailService;
    @Mock private ActiviteService activiteService;

    @InjectMocks
    private AuthService authService;

    private final RegisterRequest request =
            new RegisterRequest("Emna@Mail.com", "secret123", "22123456", "Ben Salah", "Emna");

    @Test
    void register_hacheLeMotDePasse_roleCollaborateur_etNotifieAdmin() {
        when(userRepository.existsByEmail("emna@mail.com")).thenReturn(false);
        when(roleRepository.findByLibelle(RoleName.COLLABORATEUR))
                .thenReturn(Optional.of(new Role(RoleName.COLLABORATEUR)));
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse r = authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("$2a$hash"); // jamais en clair
        assertThat(r.email()).isEqualTo("emna@mail.com");
        assertThat(r.roles()).containsExactly("COLLABORATEUR");
        verify(emailService).notifierAdminInscription(any(User.class));
    }

    @Test
    void register_emailDejaUtilise_leveConflict() {
        when(userRepository.existsByEmail("emna@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request)).isInstanceOf(ConflictException.class);
        verify(userRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }
}
