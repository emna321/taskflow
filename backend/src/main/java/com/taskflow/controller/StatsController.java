package com.taskflow.controller;

import com.taskflow.dto.StatsResponse;
import com.taskflow.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/api/stats")
    public StatsResponse stats() {
        return statsService.calculer();
    }
}
