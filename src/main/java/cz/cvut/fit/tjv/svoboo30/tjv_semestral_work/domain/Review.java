package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Review implements EntityWithId<Long> {
    @Id
    @GeneratedValue
    private Long id;

    public Review() {
    }

    public Review(Long id, LocalDate dateAdded, Integer points, Game belongsTo, Player writtenBy) {
        this.id = id;
        this.dateAdded = dateAdded;
        this.points = points;
        this.belongsTo = belongsTo;
        this.writtenBy = writtenBy;
    }

    public Review(LocalDate dateAdded, Integer points, String reviewText, Game belongsTo, Player writtenBy) {
        this.dateAdded = dateAdded;
        this.points = points;
        this.reviewText = reviewText;
        this.belongsTo = belongsTo;
        this.writtenBy = writtenBy;
    }

    @NotEmpty
    @Column(nullable = false, columnDefinition = "DATE")
    private LocalDate dateAdded;

    @NotEmpty
    @Column(nullable = false)
    private Integer points;

    private String reviewText;

    @NotEmpty
    @ManyToOne
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private Player writtenBy;

    @NotEmpty
    @ManyToOne
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private Game belongsTo;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Review review = (Review) o;

        if (!id.equals(review.id)) return false;
        if (!dateAdded.equals(review.dateAdded)) return false;
        if (!points.equals(review.points)) return false;
        if (!Objects.equals(reviewText, review.reviewText)) return false;
        if (!writtenBy.equals(review.writtenBy)) return false;
        return belongsTo.equals(review.belongsTo);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + dateAdded.hashCode();
        result = 31 * result + points.hashCode();
        result = 31 * result + (reviewText != null ? reviewText.hashCode() : 0);
        result = 31 * result + writtenBy.hashCode();
        result = 31 * result + belongsTo.hashCode();
        return result;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDate dateAdded) {
        this.dateAdded = dateAdded;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public Player getWrittenBy() {
        return writtenBy;
    }

    public void setWrittenBy(Player writtenBy) {
        this.writtenBy = writtenBy;
    }

    public Game getBelongsTo() {
        return belongsTo;
    }

    public void setBelongsTo(Game belongsTo) {
        this.belongsTo = belongsTo;
    }
}
