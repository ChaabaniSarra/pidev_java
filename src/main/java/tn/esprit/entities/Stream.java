package tn.esprit.entities;

import java.time.LocalDateTime;

public class Stream {

    private int id;
    private String url;
    private boolean isActive;
    private LocalDateTime createdAt;

    public Stream() {
        this.createdAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}