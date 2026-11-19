package com.taskflow.controller;

import com.taskflow.service.ExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Export des listes taches/projets en Excel ou PDF. */
@RestController
public class ExportController {

    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/api/export/taches")
    public ResponseEntity<byte[]> exporterTaches(@RequestParam(defaultValue = "xlsx") String format) {
        boolean pdf = "pdf".equalsIgnoreCase(format);
        byte[] contenu = pdf ? exportService.exporterTachesPdf() : exportService.exporterTachesExcel();
        return telecharger(contenu, pdf ? "taches.pdf" : "taches.xlsx", pdf ? MediaType.APPLICATION_PDF : XLSX);
    }

    @GetMapping("/api/export/projets")
    public ResponseEntity<byte[]> exporterProjets(@RequestParam(defaultValue = "xlsx") String format) {
        boolean pdf = "pdf".equalsIgnoreCase(format);
        byte[] contenu = pdf ? exportService.exporterProjetsPdf() : exportService.exporterProjetsExcel();
        return telecharger(contenu, pdf ? "projets.pdf" : "projets.xlsx", pdf ? MediaType.APPLICATION_PDF : XLSX);
    }

    private ResponseEntity<byte[]> telecharger(byte[] contenu, String nomFichier, MediaType type) {
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomFichier + "\"")
                .body(contenu);
    }
}
