package com.school.security.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "parcelles")
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode

public class Parcelle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double superficie;

    private double latitude;
    private double longitude;

    @ManyToOne
    @JoinColumn(name = "commune_id")
    private Commune commune;

    @ManyToOne
    @JoinColumn(name = "variete_id")
    private Variete variete;

    @OneToMany(mappedBy = "parcelle")
    private List<MeteoJournalier> meteo;

    @OneToMany(mappedBy = "parcelle")
    private List<Production> productions;
}
