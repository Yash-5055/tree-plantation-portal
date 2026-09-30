package com.tpp.controller;

import com.tpp.model.PlantationEvent;
import com.tpp.repository.PlantationEventRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/events")
public class EventController {

    @Autowired
    private PlantationEventRepository repository;

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("event", new PlantationEvent());
        return "event-form";
    }

    @PostMapping
    public String submitEvent(@Valid @ModelAttribute("event") PlantationEvent event,
                               BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "event-form";
        }
        repository.save(event);
        model.addAttribute("message", "Event recorded successfully!");
        return "event-success";
    }

    @GetMapping("/{id}")
    public String viewEvent(@PathVariable Long id, Model model) {
        model.addAttribute("event", repository.findById(id).orElseThrow());
        return "event-detail";
    }
}
