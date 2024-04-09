package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Game;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.dtos.GameLeaderboardDTO;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.repository.GameRepository;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.repository.PlayerRepository;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class GameServiceImpl extends CrudServiceImpl<Game, Long> implements GameService {
    private final GameRepository gameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    public GameServiceImpl(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    protected CrudRepository<Game, Long> getRepository() {
        return gameRepository;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        var game = gameRepository.findById(id).orElseThrow(IllegalArgumentException::new);

        for (var p : game.getOwnedBy()) {
            p.getOwnedGames().remove(game);
            playerRepository.save(p);
        }

        for (var review : game.getGameReviews()) {
            var player = review.getWrittenBy();
            player.getWrittenReviews().remove(review);
            playerRepository.save(player);
            reviewRepository.deleteById(review.getId());
        }

        gameRepository.deleteById(id);
    }

    public List<GameLeaderboardDTO> calculateLeaderboard(int month, int year) {
        var games = gameRepository.findAll();
        List<GameLeaderboardDTO> leaderboard = new ArrayList<>();

        for (Game game : games) {
            double averageRating = calculateAverageRating(game, month, year);
            // Skip games with no reviews in the given month and year
            if (averageRating == -1) {
                continue;
            }
            leaderboard.add(new GameLeaderboardDTO(game.getId(), game.getName(), averageRating));
        }

        leaderboard.sort(Comparator.comparingDouble(GameLeaderboardDTO::getAverageRating).reversed());

        return leaderboard;
    }

    @Transactional
    public double calculateAverageRating(Game game, int month, int year) {
        // This does not work for weird reasons
        //var reviews = gameRepository.findReviewsByGameAndMonthAndYear(game.getId(), month, year);
        var reviews = game.getGameReviews();
        double reviewSum = 0;
        int reviewCnt = 0;

        for (var review : reviews) {
            if (review.getDateAdded().getMonthValue() == month && review.getDateAdded().getYear() == year) {
                reviewSum += review.getPoints();
                reviewCnt++;
            }
        }

        // Return -1 if there are no reviews in the given month and year
        if (reviewCnt == 0) {
            return -1;
        }

        return reviewSum / reviewCnt;
    }

    @Transactional
    public List<Game> applyDiscountToTopGames(int year, int month, int topN) {
        var leaderboard = calculateLeaderboard(month, year);
        List<Game> discounted = new ArrayList<>();

        if (topN > leaderboard.size()) {
            throw new IllegalArgumentException("Top N cannot be greater than the number of games");
        }

        for (int i = 0; i < topN; i++) {
            var gameId = leaderboard.get(i).getGameId();
            var game = gameRepository.findById(gameId).orElseThrow(IllegalArgumentException::new);

            game.setOldPrice(game.getPrice());
            game.setDiscounted(true);
            double newPrice = game.getPrice() * 0.5;

            game.setPrice((int) newPrice);
            gameRepository.save(game);
            discounted.add(game);
        }

        return discounted;
    }
}
