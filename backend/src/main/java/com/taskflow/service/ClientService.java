package com.taskflow.service;

import com.taskflow.dto.ClientDto;
import com.taskflow.entity.Client;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional(readOnly = true)
    public List<ClientDto> findAll() {
        return clientRepository.findAll().stream().map(ClientDto::from).toList();
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

    private void apply(Client c, ClientDto dto) {
        c.setNom(dto.nom().trim());
        c.setTel(dto.tel());
        c.setEmail(dto.email());
        c.setAdresse(dto.adresse());
    }
}
