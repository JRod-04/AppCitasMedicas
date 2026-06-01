package com.citasmedicas.appcitasmedicas.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "appointment_types")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AppointmentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer durationMinutes;

    private String description;

    @OneToMany(mappedBy = "appointmentType", fetch = FetchType.LAZY)
    private List<Appointment> appointments;
}

