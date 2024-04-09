package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Game;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.dtos.GameLeaderboardDTO;

import java.util.List;

public interface GameService extends CrudService<Game, Long> {
    List<GameLeaderboardDTO> calculateLeaderboard(int month, int year);

    double calculateAverageRating(Game game, int month, int year);

    void deleteById(Long id);

    List<Game> applyDiscountToTopGames(int year, int month, int topN);
}
