package com.taskflow.service;

import com.taskflow.entity.User;
import com.taskflow.exception.BadRequestException;
import com.taskflow.repository.ActiviteRepository;
import com.taskflow.repository.RoleRepository;
import com.taskflow.repository.TacheRepository;
import com.taskflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private TacheRepository tacheRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private FichierValidationService fichierValidationService;
    @Mock private ActiviteRepository activiteRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setPassword("$2a$hashActuel");
    }

    @Test
    void changerMotDePasse_ancienCorrect_metAJourLeHash() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("ancien123", "$2a$hashActuel")).thenReturn(true);
        when(passwordEncoder.encode("nouveau123")).thenReturn("$2a$nouveauHash");

        userService.changerMotDePasse(1L, "ancien123", "nouveau123");

        assertThat(user.getPassword()).isEqualTo("$2a$nouveauHash");
        verify(userRepository).save(user);
    }

    @Test
    void changerMotDePasse_ancienIncorrect_refuse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("mauvais", "$2a$hashActuel")).thenReturn(false);

        assertThatThrownBy(() -> userService.changerMotDePasse(1L, "mauvais", "nouveau123"))
                .isInstanceOf(BadRequestException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void delete_supprimeAussiSonJournalDActivite() {
        user.setEmail("sami@taskflow.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.delete(1L, "admin@taskflow.com");

        verify(activiteRepository).deleteByActeurId(1L);
        verify(userRepository).delete(user);
    }
}
