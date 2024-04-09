package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Player;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.dtos.PlayerDTO;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.dtos.RegisterPlayerDTO;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.exception.EntityNotFoundException;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.exception.GameOwnershipException;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.repository.GameRepository;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.repository.PlayerRepository;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PlayerServiceImpl extends CrudServiceImpl<Player, Long> implements PlayerService {
    private final PlayerRepository playerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    protected CrudRepository<Player, Long> getRepository() {
        return playerRepository;
    }

    public Optional<Player> findByUsername(String username) {
        var player = playerRepository.findByUsername(username);

        if (player == null) {
            return Optional.empty();
        }

        return Optional.of(player);
    }

    @Transactional
    public void deleteByUsername(String username) {
        var player = playerRepository.findByUsername(username);

        if (player == null) {
            throw new EntityNotFoundException();
        } else {
            for (var game : player.getOwnedGames()) {
                game.getOwnedBy().remove(player);
                gameRepository.save(game);
            }

            for (var review : player.getWrittenReviews()) {
                var game = review.getBelongsTo();
                game.getGameReviews().remove(review);
                gameRepository.save(game);
                reviewRepository.deleteById(review.getId());
            }

            playerRepository.delete(player);
        }
    }

    @Transactional
    public void updateByUsername(String username, PlayerDTO playerData) {
        var foundPlayer = playerRepository.findByUsername(username);

        if (foundPlayer == null) {
            throw new EntityNotFoundException();
        }

        foundPlayer.setUsername(playerData.getUsername());
        foundPlayer.setHoursPlayed(playerData.getHoursPlayed());

        if(!validatePassword(playerData.getPassword(), foundPlayer.getPassword())) {
            foundPlayer.setPassword(passwordEncoder.encode(playerData.getPassword()));
        }

        playerRepository.save(foundPlayer);
    }

    public Player registerNewPlayer(RegisterPlayerDTO registerPlayerDTO) throws IllegalStateException {
        var username = registerPlayerDTO.getUsername();
        var rawPassword = registerPlayerDTO.getPassword();

        String encodedPassword = passwordEncoder.encode(rawPassword);

        if (playerRepository.findByUsername(username) != null) {
            throw new IllegalArgumentException();
        }

        return create(new Player(username, encodedPassword));
    }

    public void changePassword(String username, String rawPassword) {
        var player = playerRepository.findByUsername(username);
        if (player == null) {
            throw new IllegalArgumentException();
        }
        player.setPassword(passwordEncoder.encode(rawPassword));
        playerRepository.save(player);
    }

    public boolean validatePassword(String username, String rawPassword) {
        var player = playerRepository.findByUsername(username);
        if (player == null) {
            throw new IllegalArgumentException();
        }
        return passwordEncoder.matches(rawPassword, player.getPassword());
    }

    public void buyGame(String username, Long gameId) {
        var player = playerRepository.findByUsername(username);
        if (player == null) {
            throw new EntityNotFoundException();
        }
        var foundGame = gameRepository.findById(gameId).orElseThrow(EntityNotFoundException::new);

        if (player.getOwnedGames().contains(foundGame)) {
            throw new GameOwnershipException("Player already owns this game.");
        }

        player.addOwnedGame(foundGame);
        foundGame.getOwnedBy().add(player);

        gameRepository.save(foundGame);
        playerRepository.save(player);
    }

    public void setAdmin(String username, Boolean isAdmin) {
        var player = playerRepository.findByUsername(username);
        if (player == null) {
            throw new IllegalArgumentException();
        }
        player.setAdmin(isAdmin);
        playerRepository.save(player);
    }

    public boolean isAdmin(String username) {
        var player = playerRepository.findByUsername(username);
        if (player == null) {
            throw new IllegalArgumentException();
        }
        return player.getAdmin();
    }
}
