package com.taskflow.service;

import com.taskflow.dto.ClientDto;
import com.taskflow.dto.PageResponse;
import com.taskflow.entity.Client;
import com.taskflow.entity.Projet;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.ClientRepository;
import com.taskflow.repository.CommentaireRepository;
import com.taskflow.repository.FichierRepository;
import com.taskflow.repository.spec.ClientSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final FichierValidationService fichierValidationService;
    private final FichierRepository fichierRepository;
    private final CommentaireRepository commentaireRepository;

    public ClientService(ClientRepository clientRepository, FichierValidationService fichierValidationService,
                         FichierRepository fichierRepository, CommentaireRepository commentaireRepository) {
        this.clientRepository = clientRepository;
        this.fichierValidationService = fichierValidationService;
        this.fichierRepository = fichierRepository;
        this.commentaireRepository = commentaireRepository;
    }

    /** Liste sans le logo (colonne binaire) : seul un indicateur hasLogo est renvoye. */
    @Transactional(readOnly = true)
    public List<ClientDto> findAll() {
        return clientRepository.findAllAsDto();
    }

    @Transactional(readOnly = true)
    public ClientDto findById(Long id) {
        return ClientDto.from(getEntity(id));
    }

    @Transactional
    public ClientDto create(ClientDto dto) {
        Client c = new Client();
        apply(c, dto);
        return ClientDto.from(clientRepository.save(c));
    }

    @Transactional
    public ClientDto update(Long id, ClientDto dto) {
        Client c = getEntity(id);
        apply(c, dto);
        return ClientDto.from(clientRepository.save(c));
    }

    /**
     * Supprime le client ainsi que ses projets et leurs taches (cascade JPA). Les fichiers et
     * commentaires des taches/projets n'etant pas des associations JPA (pour ne jamais charger
     * leur contenu binaire), ils doivent etre nettoyes explicitement avant, sous peine de
     * violation de contrainte de cle etrangere.
     */
    @Transactional
    public void delete(Long id) {
        Client c = getEntity(id);
        for (Projet p : c.getProjets()) {
            p.getTaches().forEach(t -> {
                fichierRepository.deleteByTacheId(t.getId());
                commentaireRepository.deleteByTacheId(t.getId());
            });
            fichierRepository.deleteByProjetId(p.getId());
        }
        clientRepository.delete(c);
    }

    public Client getEntity(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable (id=" + id + ")"));
    }

    private static final List<String> TRIS_AUTORISES = List.of("nom", "email");

    @Transactional(readOnly = true)
    public PageResponse<ClientDto> rechercher(String q, String tri, int page, int taille) {
        String proprieteTri = TRIS_AUTORISES.contains(tri) ? tri : "nom";
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.max(taille, 1), Sort.by(proprieteTri));
        Page<Client> resultat = clientRepository.findAll(ClientSpecifications.avecRecherche(q), pageable);
        return PageResponse.from(resultat.map(ClientDto::from));
    }

    @Transactional
    public void uploaderLogo(Long id, MultipartFile fichier) {
        Client c = getEntity(id);
        fichierValidationService.validerImage(fichier);
        try {
            c.setLogoContenu(fichier.getBytes());
        } catch (IOException e) {
            throw new BadRequestException("Impossible de lire le fichier envoye");
        }
        c.setLogoTypeMime(fichier.getContentType());
        clientRepository.save(c);
    }

    private void apply(Client c, ClientDto dto) {
        c.setNom(dto.nom().trim());
        c.setTel(dto.tel());
        c.setEmail(dto.email());
        c.setAdresse(dto.adresse());
    }
}
