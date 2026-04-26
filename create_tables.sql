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

-- Tables Equipe / MatchGame (si non creees ailleurs)
CREATE TABLE IF NOT EXISTS equipe (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    max_members INT NOT NULL,
    logo VARCHAR(500) NOT NULL,
    owner_id INT NOT NULL,
    CONSTRAINT fk_equipe_owner FOREIGN KEY (owner_id) REFERENCES `user`(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS match_game (
    id INT AUTO_INCREMENT PRIMARY KEY,
    date_match TIMESTAMP NOT NULL,
    score_team1 INT NULL,
    score_team2 INT NULL,
    statut VARCHAR(50) NOT NULL,
    equipe1_id INT NOT NULL,
    equipe2_id INT NOT NULL,
    tournoi_id INT NOT NULL,
    CONSTRAINT fk_match_equipe1 FOREIGN KEY (equipe1_id) REFERENCES equipe(id) ON DELETE CASCADE,
    CONSTRAINT fk_match_equipe2 FOREIGN KEY (equipe2_id) REFERENCES equipe(id) ON DELETE CASCADE,
    CONSTRAINT fk_match_tournoi FOREIGN KEY (tournoi_id) REFERENCES tournoi(id) ON DELETE CASCADE
);

-- Workflow adhesion equipe
CREATE TABLE IF NOT EXISTS equipe_member (
    id INT AUTO_INCREMENT PRIMARY KEY,
    equipe_id INT NOT NULL,
    user_id INT NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_equipe_member (equipe_id, user_id),
    CONSTRAINT fk_equipe_member_equipe FOREIGN KEY (equipe_id) REFERENCES equipe(id) ON DELETE CASCADE,
    CONSTRAINT fk_equipe_member_user FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS equipe_join_request (
    id INT AUTO_INCREMENT PRIMARY KEY,
    equipe_id INT NOT NULL,
    joueur_id INT NOT NULL,
    statut VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    processed_by INT NULL,
    motif VARCHAR(500) NULL,
    CONSTRAINT fk_join_request_equipe FOREIGN KEY (equipe_id) REFERENCES equipe(id) ON DELETE CASCADE,
    CONSTRAINT fk_join_request_joueur FOREIGN KEY (joueur_id) REFERENCES `user`(id) ON DELETE CASCADE,
    CONSTRAINT fk_join_request_processed_by FOREIGN KEY (processed_by) REFERENCES `user`(id) ON DELETE SET NULL
);

-- Inscriptions equipes a un tournoi pour generation round-robin
CREATE TABLE IF NOT EXISTS tournoi_equipe (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tournoi_id INT NOT NULL,
    equipe_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_tournoi_equipe (tournoi_id, equipe_id),
    CONSTRAINT fk_tournoi_equipe_tournoi FOREIGN KEY (tournoi_id) REFERENCES tournoi(id) ON DELETE CASCADE,
    CONSTRAINT fk_tournoi_equipe_equipe FOREIGN KEY (equipe_id) REFERENCES equipe(id) ON DELETE CASCADE
);