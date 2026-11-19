package com.taskflow.controller;

import com.taskflow.dto.PageResponse;
import com.taskflow.dto.ProjetRequest;
import com.taskflow.dto.ProjetResponse;
import com.taskflow.service.ExportService;
import com.taskflow.service.ProjetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projets")
public class ProjetController {

    private final ProjetService projetService;
    private final ExportService exportService;

    public ProjetController(ProjetService projetService, ExportService exportService) {
        this.projetService = projetService;
        this.exportService = exportService;
    }

    /** GET /api/projets ou /api/projets?clientId=1 */
    @GetMapping
    public List<ProjetResponse> findAll(@RequestParam(required = false) Long clientId) {
        return projetService.findAll(clientId);
    }

    /** Recherche avancee paginee : GET /api/projets/recherche?q=...&clientId=...&page=0&taille=10 */
    @GetMapping("/recherche")
    public PageResponse<ProjetResponse> rechercher(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long clientId,
            @RequestParam(defaultValue = "libelle") String tri,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int taille) {
        return projetService.rechercher(q, clientId, tri, page, taille);
    }

    @GetMapping("/{id}")
    public ProjetResponse findById(@PathVariable Long id) {
        return projetService.findById(id);
    }

    /** Fiche du projet en PDF (informations + liste de ses taches). */
    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exporter(@PathVariable Long id) {
        byte[] pdf = exportService.exporterFicheProjetPdf(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"projet-" + id + ".pdf\"")
                .body(pdf);
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
