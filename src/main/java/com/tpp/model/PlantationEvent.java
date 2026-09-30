package com.tpp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
public class PlantationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Species is required")
    private String species;

    @NotBlank(message = "Planter name is required")
    private String planterName;

    private Double latitude;
    private Double longitude;

    @NotNull(message = "Date is required")
    private LocalDate eventDate;

    private String region;
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ALIVE;

    public enum Status { ALIVE, DEAD, UNHEALTHY }

    public Long getId() { return id; }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }

    public String getPlanterName() { return planterName; }
    public void setPlanterName(String planterName) { this.planterName = planterName; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
