-- Activer PostGIS si nécessaire
CREATE EXTENSION IF NOT EXISTS postgis;

----------------------------------------
-- Schéma (optionnel) : itasy
----------------------------------------
CREATE SCHEMA IF NOT EXISTS itasy;
SET search_path = itasy, public;

----------------------------------------
-- 1) REGION, COMMUNE, FOKONTANY
----------------------------------------
CREATE TABLE IF NOT EXISTS region (
  id_region SERIAL PRIMARY KEY,
  nom_region TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS commune (
  id_commune SERIAL PRIMARY KEY,
  nom_commune TEXT NOT NULL,
  id_region INTEGER NOT NULL REFERENCES region(id_region) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS fokontany (
  id_fokontany SERIAL PRIMARY KEY,
  nom_fokontany TEXT NOT NULL,
  id_commune INTEGER NOT NULL REFERENCES commune(id_commune) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_commune_region ON commune(id_region);
CREATE INDEX IF NOT EXISTS idx_fokontany_commune ON fokontany(id_commune);

----------------------------------------
-- 2) EXPLOITATION & PARCELLE
----------------------------------------
CREATE TABLE IF NOT EXISTS exploitation (
  id_exploitation SERIAL PRIMARY KEY,
  nom_exploitant TEXT,
  contact TEXT,
  id_fokontany INTEGER REFERENCES fokontany(id_fokontany) ON DELETE SET NULL,
  superficie_totale_ha NUMERIC(10,3)
);

CREATE TABLE IF NOT EXISTS parcelle (
  id_parcelle SERIAL PRIMARY KEY,
  id_exploitation INTEGER REFERENCES exploitation(id_exploitation) ON DELETE CASCADE,
  nom_parcelle TEXT,
  superficie_ha NUMERIC(10,3),
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  geom GEOMETRY(POLYGON, 4326)  -- polygon WGS84
);

CREATE INDEX IF NOT EXISTS idx_parcelle_exploitation ON parcelle(id_exploitation);
CREATE INDEX IF NOT EXISTS idx_parcelle_geom ON parcelle USING GIST(geom);

----------------------------------------
-- 3) VARIÉTÉS & SAISONS
----------------------------------------
CREATE TABLE IF NOT EXISTS variete_riz (
  id_variete SERIAL PRIMARY KEY,
  nom_variete TEXT NOT NULL,
  duree_cycle_jours INTEGER,
  description TEXT
);

CREATE TABLE IF NOT EXISTS saison_rizicole (
  id_saison SERIAL PRIMARY KEY,
  nom_saison TEXT NOT NULL,
  date_debut DATE NOT NULL,
  date_fin DATE NOT NULL,
  remarque TEXT
);

----------------------------------------
-- 4) MÉTÉO (partitionnée par année) - granularité journalière
----------------------------------------
-- Table parent (partitioned)
CREATE TABLE IF NOT EXISTS meteo_journaliere (
  id_meteo BIGSERIAL PRIMARY KEY,
  id_fokontany INTEGER REFERENCES fokontany(id_fokontany) ON DELETE SET NULL,
  date_obs DATE NOT NULL,
  temperature_moy_c REAL,
  precipitation_mm REAL,
  humidite_pct REAL,
  vent_m_s REAL,
  ensoleillement_wh_m2 REAL,
  source TEXT,
  quality_flag TEXT  -- ex: OK, SUSPICIOUS, MISSING
) PARTITION BY RANGE (date_obs);

-- Exemple : partitions par année (créer pour les années nécessaires)
CREATE TABLE IF NOT EXISTS meteo_2024 PARTITION OF meteo_journaliere
  FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');

CREATE TABLE IF NOT EXISTS meteo_2025 PARTITION OF meteo_journaliere
  FOR VALUES FROM ('2025-01-01') TO ('2026-01-01');

-- Index sur (id_fokontany, date_obs) (crée sur parent pour propager aux partitions)
CREATE INDEX IF NOT EXISTS idx_meteo_fokontany_date ON meteo_journaliere (id_fokontany, date_obs);

----------------------------------------
-- 5) PRATIQUES CULTURALES
----------------------------------------
CREATE TABLE IF NOT EXISTS pratiques_culturales (
  id_pratique BIGSERIAL PRIMARY KEY,
  id_parcelle INTEGER REFERENCES parcelle(id_parcelle) ON DELETE CASCADE,
  date_action DATE NOT NULL,
  type_action TEXT NOT NULL, -- semis, repiquage, engrais, irrigation, désherbage, récolte...
  quantite NUMERIC(12,4),
  unite TEXT,
  observation TEXT
);

CREATE INDEX IF NOT EXISTS idx_pratiques_parcelle ON pratiques_culturales(id_parcelle);
CREATE INDEX IF NOT EXISTS idx_pratiques_date ON pratiques_culturales(date_action);

----------------------------------------
-- 6) ANALYSE SOL
----------------------------------------
CREATE TABLE IF NOT EXISTS analyse_sol (
  id_analyse BIGSERIAL PRIMARY KEY,
  id_parcelle INTEGER REFERENCES parcelle(id_parcelle) ON DELETE CASCADE,
  date_prelevement DATE NOT NULL,
  ph REAL,
  matiere_organique_pct REAL,
  azote_ppm REAL,
  phosphore_ppm REAL,
  potassium_ppm REAL,
  texture TEXT,
  observation TEXT
);

CREATE INDEX IF NOT EXISTS idx_analyse_parcelle ON analyse_sol(id_parcelle);

----------------------------------------
-- 7) RENDEMENT OBSERVÉ (cible)
----------------------------------------
CREATE TABLE IF NOT EXISTS rendement_observe (
  id_rendement BIGSERIAL PRIMARY KEY,
  id_parcelle INTEGER REFERENCES parcelle(id_parcelle) ON DELETE CASCADE,
  id_saison INTEGER REFERENCES saison_rizicole(id_saison) ON DELETE SET NULL,
  date_recolte DATE,
  rendement_kg NUMERIC(14,3),
  rendement_t_ha NUMERIC(12,3),
  taux_humidite_pct REAL,
  commentaire TEXT
);

CREATE INDEX IF NOT EXISTS idx_rendement_parcelle_saison ON rendement_observe(id_parcelle, id_saison);

----------------------------------------
-- 8) OBSERVATIONS SATELLITES
----------------------------------------
CREATE TABLE IF NOT EXISTS observation_satellite (
  id_sat BIGSERIAL PRIMARY KEY,
  id_parcelle INTEGER REFERENCES parcelle(id_parcelle) ON DELETE SET NULL,
  date_obs DATE NOT NULL,
  ndvi REAL,
  evi REAL,
  couverture_nuageuse_pct REAL,
  tile_url TEXT,        -- lien vers tuiles / fichier S3
  source TEXT
);

CREATE INDEX IF NOT EXISTS idx_sat_parcelle_date ON observation_satellite(id_parcelle, date_obs);

----------------------------------------
-- 9) MODÈLES & PRÉDICTIONS
----------------------------------------
CREATE TABLE IF NOT EXISTS modele_prediction (
  id_modele SERIAL PRIMARY KEY,
  nom_modele TEXT NOT NULL,
  version TEXT,
  algorithme TEXT,
  hyperparametres JSONB,
  date_entrainement TIMESTAMP,
  metriques JSONB,
  artifact_path TEXT
);

CREATE TABLE IF NOT EXISTS prediction_rendement (
  id_prediction BIGSERIAL PRIMARY KEY,
  id_parcelle INTEGER REFERENCES parcelle(id_parcelle) ON DELETE SET NULL,
  id_saison INTEGER REFERENCES saison_rizicole(id_saison) ON DELETE SET NULL,
  id_modele INTEGER REFERENCES modele_prediction(id_modele) ON DELETE SET NULL,
  date_prediction TIMESTAMP NOT NULL DEFAULT now(),
  rendement_pred_t_ha NUMERIC(12,4),
  features_utilises JSONB,
  metadata JSONB
);

CREATE INDEX IF NOT EXISTS idx_prediction_parcelle_date ON prediction_rendement(id_parcelle, date_prediction);

----------------------------------------
-- 10) JOURNAL / ETL
----------------------------------------
CREATE TABLE IF NOT EXISTS journal_donnees (
  id_journal BIGSERIAL PRIMARY KEY,
  source TEXT,
  date_traitement TIMESTAMP NOT NULL DEFAULT now(),
  nb_enregistrements BIGINT,
  statut TEXT, -- OK / ERREUR
  details TEXT
);

CREATE INDEX IF NOT EXISTS idx_journal_source ON journal_donnees(source);

----------------------------------------
-- 11) VUES UTILES (exemples)
----------------------------------------
-- Vue : parcelles avec leur commune / fokontany / region
CREATE OR REPLACE VIEW v_parcelle_localisation AS
SELECT
  p.id_parcelle,
  p.nom_parcelle,
  p.superficie_ha,
  p.latitude,
  p.longitude,
  f.id_exploitation,
  e.nom_exploitant,
  fk.id_fokontany,
  fk.nom_fokontany,
  c.id_commune,
  c.nom_commune,
  r.id_region,
  r.nom_region
FROM parcelle p
LEFT JOIN exploitation e ON p.id_exploitation = e.id_exploitation
LEFT JOIN fokontany fk ON e.id_fokontany = fk.id_fokontany
LEFT JOIN commune c ON fk.id_commune = c.id_commune
LEFT JOIN region r ON c.id_region = r.id_region;

----------------------------------------
-- 12) EXEMPLES DE CONTRAINTES ET BONNES PRATIQUES
----------------------------------------
-- Exemple : empêcher date_fin < date_debut dans saison_rizicole
ALTER TABLE saison_rizicole
  ADD CONSTRAINT chk_saison_dates CHECK (date_fin >= date_debut);

