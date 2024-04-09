package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
public class Player implements EntityWithId<Long> {
    @Id
    @GeneratedValue
    private Long id;

    public Player() {
    }

    public Player(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Player(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    @NotEmpty
    @Column(unique = true, nullable = false)
    private String username;

    @JsonIgnore
    private String password;

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(String password) {
        this.password = password;
    }

    private Integer hoursPlayed;

    private Boolean isAdmin = false;

    public Boolean getAdmin() {
        if(isAdmin == null)
            return false;
        else
            return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }

    @ManyToMany
    private Collection<Game> ownedGames = new ArrayList<>();

    @OneToMany
    private Collection<Review> writtenReviews = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        if (!id.equals(player.id)) return false;
        if (!username.equals(player.username)) return false;
        if (!Objects.equals(password, player.password)) return false;
        if (!Objects.equals(hoursPlayed, player.hoursPlayed)) return false;
        if (!Objects.equals(isAdmin, player.isAdmin)) return false;
        if (!Objects.equals(ownedGames, player.ownedGames)) return false;
        return Objects.equals(writtenReviews, player.writtenReviews);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + username.hashCode();
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (hoursPlayed != null ? hoursPlayed.hashCode() : 0);
        result = 31 * result + (isAdmin != null ? isAdmin.hashCode() : 0);
        result = 31 * result + (ownedGames != null ? ownedGames.hashCode() : 0);
        result = 31 * result + (writtenReviews != null ? writtenReviews.hashCode() : 0);
        return result;
    }

    public Collection<GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (isAdmin) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return authorities;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public Collection<Game> getOwnedGames() {
        return ownedGames;
    }

    public Collection<Review> getWrittenReviews() {
        return writtenReviews;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getHoursPlayed() {
        return hoursPlayed;
    }

    public void setHoursPlayed(Integer hoursPlayed) {
        this.hoursPlayed = hoursPlayed;
    }

    public void setOwnedGames(Collection<Game> ownedGames) {
        this.ownedGames = ownedGames;
    }

    public void setWrittenReviews(Collection<Review> writtenReviews) {
        this.writtenReviews = writtenReviews;
    }

    public void addOwnedGame(Game game) {
        this.ownedGames.add(game);
    }

    public void addWrittenReview(Review review) {
        this.writtenReviews.add(review);
    }
}
