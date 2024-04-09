package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Game;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Player;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Review;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.dtos.ReviewDTO;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.repository.GameRepository;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.repository.PlayerRepository;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ReviewServiceImplTest {
    @Autowired
    private ReviewServiceImpl reviewService;

    @MockBean
    private GameRepository gameRepository;

    @MockBean
    private ReviewRepository reviewRepository;

    @MockBean
    private PlayerRepository playerRepository;

    @Test
    void writeReviewTest() {
        Game testGame = new Game(1L, "TestGame", LocalDate.parse("2023-11-01"));
        gameRepository.save(testGame);

        Player testPlayer = new Player("TestPlayer", "TestPassword");
        playerRepository.save(testPlayer);

        Review testReview = new Review(1L, LocalDate.parse("2023-12-30"), 10, testGame, testPlayer);

        Mockito.when(playerRepository.findByUsername("TestPlayer")).thenReturn(testPlayer);
        Mockito.when(gameRepository.findById(1L)).thenReturn(java.util.Optional.of(testGame));

        ReviewDTO testReviewDTO = new ReviewDTO(10, "TestReview", testPlayer.getUsername(), 1L, LocalDate.parse("2023-12-30"));

        reviewService.writeReview(testReviewDTO);

        assertEquals(1, testPlayer.getWrittenReviews().size());
        assertEquals(1, testGame.getGameReviews().size());

        var playerReviews = testPlayer.getWrittenReviews();

        assertEquals(testPlayer.getWrittenReviews(), testGame.getGameReviews());
        assertEquals(testPlayer.getWrittenReviews(), playerReviews);
    }
}
