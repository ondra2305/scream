package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Entity
public class Game implements EntityWithId<Long> {
    @Id
    @GeneratedValue
    private Long id;

    public Game() {
    }

    public Game(Long id, String name, LocalDate dateReleased) {
        this.id = id;
        this.name = name;
        this.dateReleased = dateReleased;
    }

    public Game(String name, LocalDate dateReleased, Integer price) {
        this.name = name;
        this.dateReleased = dateReleased;
        this.price = price;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @NotEmpty
    @Column(nullable = false)
    private String name;

    @NotEmpty
    @Column(nullable = false, columnDefinition = "DATE")
    private LocalDate dateReleased;

    @NotEmpty
    private Integer price;

    private Boolean isDiscounted = false;

    private Integer oldPrice;

    @ManyToMany(mappedBy = "ownedGames")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private Collection<Player> ownedBy = new ArrayList<>();

    @OneToMany(mappedBy = "belongsTo")
    private Collection<Review> gameReviews = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Game game = (Game) o;

        if (!id.equals(game.id)) return false;
        if (!name.equals(game.name)) return false;
        if (!dateReleased.equals(game.dateReleased)) return false;
        if (!price.equals(game.price)) return false;
        if (!Objects.equals(isDiscounted, game.isDiscounted)) return false;
        if (!Objects.equals(oldPrice, game.oldPrice)) return false;
        if (!Objects.equals(ownedBy, game.ownedBy)) return false;
        return Objects.equals(gameReviews, game.gameReviews);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + dateReleased.hashCode();
        result = 31 * result + price.hashCode();
        result = 31 * result + (isDiscounted != null ? isDiscounted.hashCode() : 0);
        result = 31 * result + (oldPrice != null ? oldPrice.hashCode() : 0);
        result = 31 * result + (ownedBy != null ? ownedBy.hashCode() : 0);
        result = 31 * result + (gameReviews != null ? gameReviews.hashCode() : 0);
        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDateReleased() {
        return dateReleased;
    }

    public void setDateReleased(LocalDate dateReleased) {
        this.dateReleased = dateReleased;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Collection<Player> getOwnedBy() {
        return ownedBy;
    }

    public Collection<Review> getGameReviews() {
        return gameReviews;
    }

    public void setOwnedBy(Collection<Player> ownedBy) {
        this.ownedBy = ownedBy;
    }

    public void setGameReviews(Collection<Review> gameReviews) {
        this.gameReviews = gameReviews;
    }

    public void addOwner(Player player) {
        this.ownedBy.add(player);
    }

    public Boolean getDiscounted() {
        return isDiscounted;
    }

    public void setDiscounted(Boolean discounted) {
        isDiscounted = discounted;
    }

    public Integer getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(Integer oldPrice) {
        this.oldPrice = oldPrice;
    }
}
