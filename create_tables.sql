-- Créer la base de données si elle n'existe pas
CREATE DATABASE IF NOT EXISTS `esport-db`;
USE `esport-db`;

-- Table test
CREATE TABLE IF NOT EXISTS test (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

-- Table jeu
CREATE TABLE IF NOT EXISTS jeu (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    genre VARCHAR(100),
    plateforme VARCHAR(100),
    description TEXT,
    statut VARCHAR(50)
);

-- Table tournoi
CREATE TABLE IF NOT EXISTS tournoi (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    date_debut DATE,
    date_fin DATE,
    statut VARCHAR(50),
    type VARCHAR(100),
    max_participants INT,
    cagnotte DOUBLE,
    date_inscription_limite DATE,
    frais_inscription DOUBLE,
    description TEXT,
    jeu_id INT,
    FOREIGN KEY (jeu_id) REFERENCES jeu(id)
);

-- Table profiling
CREATE TABLE IF NOT EXISTS profiling (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    genre VARCHAR(100),
    plateforme VARCHAR(100),
    description TEXT,
    statut VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS `user` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    roles VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nom VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    google2fa_secret VARCHAR(255),
    is_2fa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    google_oauth_id VARCHAR(255),
    oauth_provider VARCHAR(100),
    face_encoding TEXT,
    is_face_enabled BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE DATABASE IF NOT EXISTS esport_db;
USE esport_db;

CREATE TABLE stream (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        url VARCHAR(255),
                        is_active BOOLEAN,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO stream (url, is_active)
VALUES ('rtmp://192.168.126.144/live', 1);

CREATE TABLE stream_reaction (
                                 id INT AUTO_INCREMENT PRIMARY KEY,
                                 type VARCHAR(50),
                                 comment TEXT,
                                 username VARCHAR(255),
                                 created_at TIMESTAMP,
                                 stream_id INT,
                                 FOREIGN KEY (stream_id) REFERENCES stream(id) ON DELETE CASCADE
);