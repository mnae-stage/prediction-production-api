package com.school.security.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "productions")
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode

public class Production {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int annee;

    private LocalDate dateSemis;
    private LocalDate dateRecolte;

    private double rendementKgHa;

    @ManyToOne
    @JoinColumn(name = "parcelle_id")
    private Parcelle parcelle;
}
