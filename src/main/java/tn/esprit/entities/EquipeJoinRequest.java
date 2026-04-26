package tn.esprit.entities;

import java.sql.Timestamp;

public class EquipeJoinRequest {
    private int id;
    private int equipeId;
    private int joueurId;
    private String statut;
    private Timestamp createdAt;
    private Timestamp processedAt;
    private Integer processedBy;
    private String motif;

    public EquipeJoinRequest() {
    }

    public EquipeJoinRequest(int id, int equipeId, int joueurId, String statut, Timestamp createdAt,
                             Timestamp processedAt, Integer processedBy, String motif) {
        this.id = id;
        this.equipeId = equipeId;
        this.joueurId = joueurId;
        this.statut = statut;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
        this.processedBy = processedBy;
        this.motif = motif;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEquipeId() {
        return equipeId;
    }

    public void setEquipeId(int equipeId) {
        this.equipeId = equipeId;
    }

    public int getJoueurId() {
        return joueurId;
    }

    public void setJoueurId(int joueurId) {
        this.joueurId = joueurId;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Timestamp processedAt) {
        this.processedAt = processedAt;
    }

    public Integer getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(Integer processedBy) {
        this.processedBy = processedBy;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }
}