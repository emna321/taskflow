package com.taskflow.service;

import com.taskflow.dto.ClientDto;
import com.taskflow.entity.Client;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final FichierValidationService fichierValidationService;

    public ClientService(ClientRepository clientRepository, FichierValidationService fichierValidationService) {
        this.clientRepository = clientRepository;
        this.fichierValidationService = fichierValidationService;
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

    /** Supprime le client ainsi que ses projets et leurs taches (cascade). */
    @Transactional
    public void delete(Long id) {
        clientRepository.delete(getEntity(id));
    }

    public Client getEntity(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable (id=" + id + ")"));
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
