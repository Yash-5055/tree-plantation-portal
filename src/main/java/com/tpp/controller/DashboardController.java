package com.tpp.controller;

import com.tpp.model.PlantationEvent;
import com.tpp.repository.PlantationEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private PlantationEventRepository repository;

    // Searchable dashboard
    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String species,
                             @RequestParam(required = false) String region,
                             Model model) {
        List<PlantationEvent> events;
        if (species != null && !species.isEmpty()) {
            events = repository.findBySpeciesContainingIgnoreCase(species);
        } else if (region != null && !region.isEmpty()) {
            events = repository.findByRegionContainingIgnoreCase(region);
        } else {
            events = repository.findAll();
        }
        model.addAttribute("events", events);
        return "dashboard";
    }

    // Summary KPI indicators
    @GetMapping("/dashboard/summary")
    public String summary(Model model) {
        List<PlantationEvent> all = repository.findAll();
        long total = all.size();
        long alive = all.stream().filter(e -> e.getStatus() == PlantationEvent.Status.ALIVE).count();
        long dead = all.stream().filter(e -> e.getStatus() == PlantationEvent.Status.DEAD).count();
        long unhealthy = all.stream().filter(e -> e.getStatus() == PlantationEvent.Status.UNHEALTHY).count();

        model.addAttribute("total", total);
        model.addAttribute("alive", alive);
        model.addAttribute("dead", dead);
        model.addAttribute("unhealthy", unhealthy);
        model.addAttribute("survivalRate", total == 0 ? 0 : Math.round((alive * 100.0) / total));
        return "summary";
    }

    // Status drill-down: click a KPI -> see matching records
    @GetMapping("/dashboard/status/{status}")
    public String drillDown(@PathVariable String status, Model model) {
        PlantationEvent.Status statusEnum = PlantationEvent.Status.valueOf(status.toUpperCase());
        model.addAttribute("events", repository.findByStatus(statusEnum));
        model.addAttribute("filterLabel", status);
        return "dashboard";
    }

    // Alert/exception view: everything not ALIVE
    @GetMapping("/alerts")
    public String alerts(Model model) {
        List<PlantationEvent> alerts = repository.findAll().stream()
                .filter(e -> e.getStatus() != PlantationEvent.Status.ALIVE)
                .toList();
        model.addAttribute("events", alerts);
        return "alerts";
    }
}
