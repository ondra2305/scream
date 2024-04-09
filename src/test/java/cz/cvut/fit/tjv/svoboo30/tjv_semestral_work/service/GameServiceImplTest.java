package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Game;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Player;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Review;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.dtos.GameLeaderboardDTO;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.repository.GameRepository;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.repository.PlayerRepository;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.repository.ReviewRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
public class GameServiceImplTest {
    @Autowired
    private GameServiceImpl gameService;

    @MockBean
    private GameRepository gameRepository;

    @MockBean
    private ReviewRepository reviewRepository;

    private List<GameLeaderboardDTO> mockLeaderboard = new ArrayList<>();

    @Test
    void calculateAverageRatingTest() {
        Game testGame = new Game(1L, "TestGame", LocalDate.parse("2023-11-01"));
        Player testPlayer = new Player("TestPlayer", "TestPassword");
        Review testReview1 = new Review(1L, LocalDate.parse("2023-12-30"), 10, testGame, testPlayer);
        Review testReview2 = new Review(2L, LocalDate.parse("2023-12-30"), 5, testGame, testPlayer);
        Review testReview3 = new Review(3L, LocalDate.parse("2023-11-01"), 8, testGame, testPlayer);
        var reviews = Arrays.asList(testReview1, testReview2, testReview3);
        testGame.setGameReviews(reviews);
        gameRepository.save(testGame);
        testReview1.setBelongsTo(testGame);
        testReview2.setBelongsTo(testGame);
        testReview3.setBelongsTo(testGame);
        reviewRepository.save(testReview1);
        reviewRepository.save(testReview2);
        reviewRepository.save(testReview3);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));
        when(gameRepository.findReviewsByGameAndMonthAndYear(1L, 12, 2023)).thenReturn(Arrays.asList(testReview1, testReview2));

        double averageRating = gameService.calculateAverageRating(testGame, 12, 2023);

        assertEquals(7.5, averageRating, 0.01);
    }

    @Test
    void calculateLeaderboardTest() {
        Player testPlayer = new Player("TestPlayer", "TestPassword");
        Game testGame1 = new Game(1L, "TestGame1", LocalDate.parse("2023-11-01"));
        Game testGame2 = new Game(2L, "TestGame2", LocalDate.parse("2023-11-01"));
        Review testReview1 = new Review(1L, LocalDate.parse("2023-12-30"), 8, testGame1, testPlayer);
        Review testReview2 = new Review(2L, LocalDate.parse("2023-12-30"), 2, testGame1, testPlayer);
        Review testReview3 = new Review(3L, LocalDate.parse("2023-11-01"), 1, testGame1, testPlayer);
        Review testReview4 = new Review(4L, LocalDate.parse("2023-12-30"), 7, testGame2, testPlayer);
        Review testReview5 = new Review(5L, LocalDate.parse("2023-12-30"), 1, testGame2, testPlayer);
        Review testReview6 = new Review(6L, LocalDate.parse("2023-11-01"), 10, testGame2, testPlayer);
        var reviews1 = Arrays.asList(testReview1, testReview2, testReview3);
        var reviews2 = Arrays.asList(testReview4, testReview5, testReview6);
        testGame1.setGameReviews(reviews1);
        testGame2.setGameReviews(reviews2);
        gameRepository.save(testGame1);
        gameRepository.save(testGame2);

        when(gameRepository.findAll()).thenReturn(Arrays.asList(testGame1, testGame2));
        when(gameRepository.findReviewsByGameAndMonthAndYear(1L, 12, 2023)).thenReturn(Arrays.asList(testReview1, testReview2));
        when(gameRepository.findReviewsByGameAndMonthAndYear(2L, 12, 2023)).thenReturn(Arrays.asList(testReview4, testReview5));

        mockLeaderboard = gameService.calculateLeaderboard(12, 2023);
        Assert.isTrue(mockLeaderboard.size() == 2, "Leaderboard should have 2 games");
        Assert.isTrue(mockLeaderboard.get(0).getGameName().equals("TestGame1"), "First game should be TestGame1");
        Assert.isTrue(mockLeaderboard.get(1).getGameName().equals("TestGame2"), "Second game should be TestGame2");
        Assert.isTrue(mockLeaderboard.get(0).getAverageRating() == 5, "TestGame1 should have average rating of 5");
        Assert.isTrue(mockLeaderboard.get(1).getAverageRating() == 4, "TestGame2 should have average rating of 4");
    }

    @Test
    void calculateLeaderboardOtherIntervalTest() {
        Player testPlayer = new Player("TestPlayer", "TestPassword");
        Game testGame1 = new Game(1L, "TestGame1", LocalDate.parse("2023-11-01"));
        Review testReview1 = new Review(1L, LocalDate.parse("2023-11-30"), 8, testGame1, testPlayer);
        testGame1.setGameReviews(List.of(testReview1));
        gameRepository.save(testGame1);

        when(gameRepository.findAll()).thenReturn(List.of(testGame1));
        when(gameRepository.findReviewsByGameAndMonthAndYear(1L, 12, 2023)).thenReturn(Collections.emptyList());

        var leaderboard = gameService.calculateLeaderboard(12, 2023);
        Assert.isTrue(leaderboard.isEmpty(), "Leaderboard should be no games because there are no reviews in the given month and year");
    }

    @Test
    void applyDiscountToTopGamesTest() {
        Player testPlayer = new Player("TestPlayer", "TestPassword");
        Game testGame1 = new Game(1L, "TestGame1", LocalDate.parse("2023-11-01"));
        Game testGame2 = new Game(2L, "TestGame2", LocalDate.parse("2023-11-01"));
        Review testReview1 = new Review(1L, LocalDate.parse("2023-12-30"), 8, testGame1, testPlayer);
        Review testReview2 = new Review(2L, LocalDate.parse("2023-12-30"), 2, testGame1, testPlayer);
        Review testReview3 = new Review(4L, LocalDate.parse("2023-12-30"), 7, testGame2, testPlayer);
        Review testReview4 = new Review(5L, LocalDate.parse("2023-12-30"), 1, testGame2, testPlayer);
        var reviews1 = Arrays.asList(testReview1, testReview2);
        var reviews2 = Arrays.asList(testReview3, testReview4);
        testGame1.setGameReviews(reviews1);
        testGame1.setPrice(100);
        testGame2.setGameReviews(reviews2);
        testGame2.setPrice(100);
        gameRepository.save(testGame1);
        gameRepository.save(testGame2);

        when(gameRepository.findAll()).thenReturn(Arrays.asList(testGame1, testGame2));
        when(gameRepository.findReviewsByGameAndMonthAndYear(1L, 12, 2023)).thenReturn(reviews1);
        when(gameRepository.findReviewsByGameAndMonthAndYear(2L, 12, 2023)).thenReturn(reviews2);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame1));
        when(gameRepository.findById(2L)).thenReturn(Optional.of(testGame2));

        mockLeaderboard = gameService.calculateLeaderboard(12, 2023);
        Assert.isTrue(mockLeaderboard.size() == 2, "Leaderboard should have 2 games");
        Assert.isTrue(mockLeaderboard.get(0).getGameName().equals("TestGame1"), "First game should be TestGame1");

        gameService.applyDiscountToTopGames(2023, 12, 1);
        Assert.isTrue(testGame1.getPrice() == 50, "TestGame1 should have price of 50");
        Assert.isTrue(testGame1.getDiscounted(), "TestGame1 should be discounted.");
        Assert.isTrue(testGame2.getPrice() == 100, "TestGame2 should have price of 100");
    }
}
