package com.schemesathi.dto;

public class AdminSchemeStatsDto {
    private long totalSchemes;
    private long publishedSchemes;
    private long draftSchemes;
    private long archivedSchemes;
    private long verifiedSchemes;
    private long needsReviewSchemes;
    private long unverifiedSchemes;

    public AdminSchemeStatsDto() {}

    public AdminSchemeStatsDto(long totalSchemes, long publishedSchemes, long draftSchemes, long archivedSchemes,
                               long verifiedSchemes, long needsReviewSchemes, long unverifiedSchemes) {
        this.totalSchemes = totalSchemes;
        this.publishedSchemes = publishedSchemes;
        this.draftSchemes = draftSchemes;
        this.archivedSchemes = archivedSchemes;
        this.verifiedSchemes = verifiedSchemes;
        this.needsReviewSchemes = needsReviewSchemes;
        this.unverifiedSchemes = unverifiedSchemes;
    }

    public long getTotalSchemes() { return totalSchemes; }
    public void setTotalSchemes(long totalSchemes) { this.totalSchemes = totalSchemes; }

    public long getPublishedSchemes() { return publishedSchemes; }
    public void setPublishedSchemes(long publishedSchemes) { this.publishedSchemes = publishedSchemes; }

    public long getDraftSchemes() { return draftSchemes; }
    public void setDraftSchemes(long draftSchemes) { this.draftSchemes = draftSchemes; }

    public long getArchivedSchemes() { return archivedSchemes; }
    public void setArchivedSchemes(long archivedSchemes) { this.archivedSchemes = archivedSchemes; }

    public long getVerifiedSchemes() { return verifiedSchemes; }
    public void setVerifiedSchemes(long verifiedSchemes) { this.verifiedSchemes = verifiedSchemes; }

    public long getNeedsReviewSchemes() { return needsReviewSchemes; }
    public void setNeedsReviewSchemes(long needsReviewSchemes) { this.needsReviewSchemes = needsReviewSchemes; }

    public long getUnverifiedSchemes() { return unverifiedSchemes; }
    public void setUnverifiedSchemes(long unverifiedSchemes) { this.unverifiedSchemes = unverifiedSchemes; }
}
