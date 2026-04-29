package tn.esprit.entities;

public class Video {

    private int id;
    private String title;
    private String path;

    public Video() {}

    public Video(String title, String path) {
        this.title = title;
        this.path = path;
    }

    public Video(int id, String title, String path) {
        this.id = id;
        this.title = title;
        this.path = path;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getPath() { return path; }

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setPath(String path) { this.path = path; }
}