package com.taskflow.service;

import com.taskflow.dto.ClientDto;
import com.taskflow.entity.Client;
import com.taskflow.entity.Projet;
import com.taskflow.entity.Tache;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.ClientRepository;
import com.taskflow.repository.CommentaireRepository;
import com.taskflow.repository.FichierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private FichierRepository fichierRepository;
    @Mock
    private CommentaireRepository commentaireRepository;

    @InjectMocks
    private ClientService clientService;

    private Client client(Long id, String nom) {
        Client c = new Client();
        c.setId(id);
        c.setNom(nom);
        c.setEmail(nom.toLowerCase() + "@mail.com");
        return c;
    }

    @Test
    void findAll_renvoieTousLesClients() {
        when(clientRepository.findAllAsDto()).thenReturn(List.of(
                new ClientDto(1L, "Alpha", null, "alpha@mail.com", null, 0, false),
                new ClientDto(2L, "Beta", null, "beta@mail.com", null, 0, false)));

        List<ClientDto> result = clientService.findAll();

        assertThat(result).hasSize(2).extracting(ClientDto::nom).containsExactly("Alpha", "Beta");
    }

    @Test
    void create_enregistreLeClient() {
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> {
            Client c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        ClientDto created = clientService.create(new ClientDto(null, "  Gamma ", "22123456", "g@mail.com", "Tunis", null, null));

        assertThat(created.id()).isEqualTo(10L);
        assertThat(created.nom()).isEqualTo("Gamma");
        assertThat(created.nbProjets()).isZero();
    }

    @Test
    void update_modifieLeClientExistant() {
        Client existant = client(1L, "Ancien");
        when(clientRepository.findById(1L)).thenReturn(Optional.of(existant));
        when(clientRepository.save(existant)).thenReturn(existant);

        ClientDto updated = clientService.update(1L, new ClientDto(null, "Nouveau", null, "n@mail.com", "Sousse", null, null));

        assertThat(updated.nom()).isEqualTo("Nouveau");
        assertThat(updated.adresse()).isEqualTo("Sousse");
    }

    @Test
    void findById_inexistant_leveNotFound() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void delete_supprimeLeClient() {
        Client existant = client(1L, "A");
        when(clientRepository.findById(1L)).thenReturn(Optional.of(existant));

        clientService.delete(1L);

        verify(clientRepository).delete(existant);
    }

    @Test
    void delete_nettoieFichiersEtCommentairesDesTachesEtProjets() {
        Client existant = client(1L, "A");
        Projet projet = new Projet();
        projet.setId(20L);
        Tache tache = new Tache();
        tache.setId(200L);
        projet.getTaches().add(tache);
        existant.getProjets().add(projet);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(existant));

        clientService.delete(1L);

        verify(fichierRepository).deleteByTacheId(200L);
        verify(commentaireRepository).deleteByTacheId(200L);
        verify(fichierRepository).deleteByProjetId(20L);
        verify(clientRepository).delete(existant);
    }
}
