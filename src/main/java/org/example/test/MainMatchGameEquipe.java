package org.example.test;

import org.example.entities.Equipe;
import org.example.entities.MatchGame;
import org.example.services.ServiceEquipe;
import org.example.services.ServiceMatchGame;
import org.example.utils.MyDatabase;

import java.sql.Timestamp;

public class MainMatchGameEquipe {
    public static void main(String[] args) {
        MyDatabase.getInstance();

        ServiceEquipe serviceEquipe = new ServiceEquipe();
        ServiceMatchGame serviceMatchGame = new ServiceMatchGame();

        try {
          /* 
            Equipe equipe = new Equipe("Equipe A", 11, "equipe-a.png");
            serviceEquipe.ajouter(equipe);
            serviceEquipe.getAll().forEach(System.out::println);

            if (!serviceEquipe.getAll().isEmpty()) {
                Equipe firstEquipe = serviceEquipe.getAll().get(0);
                firstEquipe.setNom("Equipe A mise a jour");
                serviceEquipe.modifier(firstEquipe);
                System.out.println(serviceEquipe.getAll());
            }

            // Créer une deuxième équipe
            Equipe equipe2 = new Equipe("Equipe B", 11, "equipe-b.png");
            serviceEquipe.ajouter(equipe2);
            System.out.println("\n--- Toutes les équipes ---");
            serviceEquipe.getAll().forEach(System.out::println);
*/
            MatchGame matchGame = new MatchGame(
                    Timestamp.valueOf("2026-04-08 10:00:00"),
                    0,
                    0,
                    "PLANIFIE",
                    3,
                    5,
                    1
            );
            
            serviceMatchGame.ajouter(matchGame);
            serviceMatchGame.getAll().forEach(System.out::println);

            if (!serviceMatchGame.getAll().isEmpty()) {
                MatchGame firstMatchGame = serviceMatchGame.getAll().get(0);
                firstMatchGame.setStatut("TERMINE");
                firstMatchGame.setScoreTeam1(2);
                firstMatchGame.setScoreTeam2(1);
                serviceMatchGame.modifier(firstMatchGame);
                System.out.println("\n--- MatchGame après modification ---");
                serviceMatchGame.getAll().forEach(System.out::println);
            }

            int idMatchToSupprimer = 7; 
            serviceMatchGame.supprimer(idMatchToSupprimer);
            System.out.println("MatchGame supprime avec id=" + idMatchToSupprimer);
          
 /* 
            int idEquipeToSupprimer = 11; 
            serviceEquipe.supprimer(idEquipeToSupprimer);
            System.out.println("Equipe supprime avec id=" + idEquipeToSupprimer);
*/
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}