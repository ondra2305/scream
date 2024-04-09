package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.dtos;

public class GameLeaderboardDTO {
    private Long gameId;
    private String gameName;
    private double averageRating;

    public GameLeaderboardDTO(Long gameId, String gameName, double averageRating) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.averageRating = averageRating;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }
}
