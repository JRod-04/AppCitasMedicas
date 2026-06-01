package com.citasmedicas.appcitasmedicas.entity;

import com.citasmedicas.appcitasmedicas.Enums.OfficeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "offices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Office {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String location;

    private String floor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OfficeStatus status = OfficeStatus.ACTIVE;

    @OneToMany(mappedBy = "office", fetch = FetchType.LAZY)
    private List<Appointment> appointments;
}

