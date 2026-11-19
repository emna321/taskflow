package com.taskflow.controller;

import com.taskflow.dto.ActiviteResponse;
import com.taskflow.service.ActiviteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ActiviteController {

    private final ActiviteService activiteService;

    public ActiviteController(ActiviteService activiteService) {
        this.activiteService = activiteService;
    }

    @GetMapping("/api/activites")
    public List<ActiviteResponse> lister(@RequestParam(defaultValue = "20") int limit) {
        return activiteService.lister(limit);
    }
}
