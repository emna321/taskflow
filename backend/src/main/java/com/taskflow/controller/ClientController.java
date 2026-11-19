package com.taskflow.controller;

import com.taskflow.dto.ClientDto;
import com.taskflow.entity.Client;
import com.taskflow.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    // Admin + Collaborateur : lister, consulter, ajouter
    @GetMapping
    public List<ClientDto> findAll() {
        return clientService.findAll();
    }

    @GetMapping("/{id}")
    public ClientDto findById(@PathVariable Long id) {
        return clientService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientDto create(@Valid @RequestBody ClientDto dto) {
        return clientService.create(dto);
    }

    // Admin uniquement : modifier, supprimer
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ClientDto update(@PathVariable Long id, @Valid @RequestBody ClientDto dto) {
        return clientService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        clientService.delete(id);
    }

    @PostMapping("/{id}/logo")
    public ClientDto uploaderLogo(@PathVariable Long id, @RequestParam("logo") MultipartFile logo) {
        clientService.uploaderLogo(id, logo);
        return clientService.findById(id);
    }

    @GetMapping("/{id}/logo")
    public ResponseEntity<byte[]> telechargerLogo(@PathVariable Long id) {
        Client c = clientService.getEntity(id);
        if (c.getLogoContenu() == null) {
            return ResponseEntity.notFound().build();
        }
        MediaType type;
        try {
            type = MediaType.parseMediaType(c.getLogoTypeMime());
        } catch (Exception e) {
            type = MediaType.APPLICATION_OCTET_STREAM;
        }
        return ResponseEntity.ok().contentType(type).body(c.getLogoContenu());
    }
}
