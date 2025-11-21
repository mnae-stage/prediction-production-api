package com.school.security.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ml_features")
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode

public class MLFeatures {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int annee;
    private double superficie;
    private Long varieteId;

    private double sommePrecipitation;
    private double temperatureMoyenne;
    private double humiditeMoyenne;

    private double productionReelle;
}
