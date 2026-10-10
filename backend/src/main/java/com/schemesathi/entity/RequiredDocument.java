package com.schemesathi.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "required_documents")
public class RequiredDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheme_id", nullable = false)
    private Scheme scheme;

    @Column(nullable = false, length = 100)
    private String documentName;

    @Column(length = 500)
    private String description;

    private Boolean mandatory = true;

    @Column(length = 200)
    private String alternatives;

    public RequiredDocument() {}

    public RequiredDocument(Long id, Scheme scheme, String documentName, String description, Boolean mandatory, String alternatives) {
        this.id = id;
        this.scheme = scheme;
        this.documentName = documentName;
        this.description = description;
        this.mandatory = mandatory != null ? mandatory : true;
        this.alternatives = alternatives;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Scheme scheme;
        private String documentName;
        private String description;
        private Boolean mandatory = true;
        private String alternatives;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder scheme(Scheme scheme) { this.scheme = scheme; return this; }
        public Builder documentName(String documentName) { this.documentName = documentName; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder mandatory(Boolean mandatory) { this.mandatory = mandatory; return this; }
        public Builder alternatives(String alternatives) { this.alternatives = alternatives; return this; }

        public RequiredDocument build() {
            return new RequiredDocument(id, scheme, documentName, description, mandatory, alternatives);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Scheme getScheme() { return scheme; }
    public void setScheme(Scheme scheme) { this.scheme = scheme; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getMandatory() { return mandatory; }
    public void setMandatory(Boolean mandatory) { this.mandatory = mandatory; }

    public String getAlternatives() { return alternatives; }
    public void setAlternatives(String alternatives) { this.alternatives = alternatives; }
}
