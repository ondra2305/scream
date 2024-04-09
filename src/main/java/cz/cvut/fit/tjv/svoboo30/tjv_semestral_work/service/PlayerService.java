package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Player;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.dtos.PlayerDTO;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.dtos.RegisterPlayerDTO;

import java.util.Optional;

public interface PlayerService extends CrudService<Player, Long> {
    Optional<Player> findByUsername(String username);

    void deleteByUsername(String username);

    void updateByUsername(String username, PlayerDTO playerData);

    Player registerNewPlayer(RegisterPlayerDTO registerPlayerDTO);

    void changePassword(String username, String rawPassword);

    boolean validatePassword(String username, String rawPassword);

    void buyGame(String username, Long gameId);

    void setAdmin(String username, Boolean isAdmin);

    boolean isAdmin(String username);
}
