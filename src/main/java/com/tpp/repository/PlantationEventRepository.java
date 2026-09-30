package com.tpp.repository;

import com.tpp.model.PlantationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlantationEventRepository extends JpaRepository<PlantationEvent, Long> {
    List<PlantationEvent> findBySpeciesContainingIgnoreCase(String species);
    List<PlantationEvent> findByRegionContainingIgnoreCase(String region);
    List<PlantationEvent> findByStatus(PlantationEvent.Status status);
}
