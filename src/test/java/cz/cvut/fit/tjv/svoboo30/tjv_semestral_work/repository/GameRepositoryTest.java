package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.repository;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Game;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Review;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase
public class GameRepositoryTest {
    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    void findReviewsByGameAndMonthAndYearTest() {
        Game game = new Game();
        game.setName("TestGame");
        game.setDateReleased(LocalDate.parse("2023-11-01"));
        gameRepository.save(game);

        Review review1 = new Review();
        review1.setPoints(10);
        review1.setBelongsTo(game);
        review1.setDateAdded(LocalDate.parse("2023-12-30"));
        reviewRepository.save(review1);

        Review review2 = new Review();
        review2.setPoints(10);
        review2.setBelongsTo(game);
        review2.setDateAdded(LocalDate.parse("2023-12-31"));
        reviewRepository.save(review2);

        // This review is in a different month
        Review review3 = new Review();
        review3.setPoints(1);
        review3.setBelongsTo(game);
        review3.setDateAdded(LocalDate.parse("2023-11-30"));
        reviewRepository.save(review3);

        var reviews = gameRepository.findReviewsByGameAndMonthAndYear(game.getId(), 12, 2023);

        assertEquals(2, reviews.size()); // Expecting 2 reviews
    }
}
