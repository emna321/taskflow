package com.taskflow.controller;

import com.taskflow.dto.ProjetRequest;
import com.taskflow.dto.ProjetResponse;
import com.taskflow.service.ProjetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projets")
public class ProjetController {

    private final ProjetService projetService;

    public ProjetController(ProjetService projetService) {
        this.projetService = projetService;
    }

    /** GET /api/projets ou /api/projets?clientId=1 */
    @GetMapping
    public List<ProjetResponse> findAll(@RequestParam(required = false) Long clientId) {
        return projetService.findAll(clientId);
    }

    @GetMapping("/{id}")
    public ProjetResponse findById(@PathVariable Long id) {
        return projetService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjetResponse create(@Valid @RequestBody ProjetRequest request) {
        return projetService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProjetResponse update(@PathVariable Long id, @Valid @RequestBody ProjetRequest request) {
        return projetService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        projetService.delete(id);
    }
}
