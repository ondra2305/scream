package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.dtos;

import java.time.LocalDate;

public class ReviewDTO {
    private int points;
    private String reviewText;
    private String writtenBy; // ID of the player
    private Long belongsTo; // ID of the game
    private LocalDate dateAdded;

    public ReviewDTO(int points, String reviewText, String writtenBy, Long belongsTo, LocalDate dateAdded) {
        this.points = points;
        this.reviewText = reviewText;
        this.writtenBy = writtenBy;
        this.belongsTo = belongsTo;
        this.dateAdded = dateAdded;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public String getWrittenBy() {
        return writtenBy;
    }

    public void setWrittenBy(String writtenBy) {
        this.writtenBy = writtenBy;
    }

    public Long getBelongsTo() {
        return belongsTo;
    }

    public void setBelongsTo(Long belongsTo) {
        this.belongsTo = belongsTo;
    }

    public LocalDate getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDate dateAdded) {
        this.dateAdded = dateAdded;
    }
}
