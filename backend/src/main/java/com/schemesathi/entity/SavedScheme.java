package com.schemesathi.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_schemes")
public class SavedScheme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheme_id", nullable = false)
    private Scheme scheme;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime savedAt = LocalDateTime.now();

    public SavedScheme() {}

    public SavedScheme(Long id, User user, Scheme scheme, String notes, LocalDateTime savedAt) {
        this.id = id;
        this.user = user;
        this.scheme = scheme;
        this.notes = notes;
        this.savedAt = savedAt != null ? savedAt : LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private User user;
        private Scheme scheme;
        private String notes;
        private LocalDateTime savedAt = LocalDateTime.now();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder scheme(Scheme scheme) { this.scheme = scheme; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder savedAt(LocalDateTime savedAt) { this.savedAt = savedAt; return this; }

        public SavedScheme build() {
            return new SavedScheme(id, user, scheme, notes, savedAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Scheme getScheme() { return scheme; }
    public void setScheme(Scheme scheme) { this.scheme = scheme; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getSavedAt() { return savedAt; }
    public void setSavedAt(LocalDateTime savedAt) { this.savedAt = savedAt; }
}
