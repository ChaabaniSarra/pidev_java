package tn.esprit.entities;

import java.sql.Timestamp;

public class Comment {
    private int id;
    private int blogId;
    private int userId;
    private String userName; // For easy display
    private String content;
    private Timestamp createdAt;

    public Comment() {
    }

    public Comment(int blogId, int userId, String content) {
        this.blogId = blogId;
        this.userId = userId;
        this.content = content;
    }

    public Comment(int id, int blogId, int userId, String userName, String content, Timestamp createdAt) {
        this.id = id;
        this.blogId = blogId;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBlogId() { return blogId; }
    public void setBlogId(int blogId) { this.blogId = blogId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", blogId=" + blogId +
                ", userName='" + userName + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
