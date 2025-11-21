package com.school.security.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "meteo_journalier")
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode

public class MeteoJournalier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    private double temperatureMoyenne;
    private double precipitationMm;
    private double humidite;
    private double vitesseVent;
    @ManyToOne
    @JoinColumn(name = "parcelle_id")
    private Parcelle parcelle;
}
