package com.school.security.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "predictions")
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode

public class Prediction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate datePrediction;
    private double rendementEstimeKgHa;
    @ManyToOne
    @JoinColumn(name = "parcelle_id")
    private Parcelle parcelle;
}
