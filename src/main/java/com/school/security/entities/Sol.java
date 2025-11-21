package com.school.security.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sols")
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode

public class Sol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String typeSol;

    private double ph;
    private double azote;
    private double phosphore;
    private double potassium;

    @ManyToOne
    @JoinColumn(name = "parcelle_id")
    private Parcelle parcelle;
}
