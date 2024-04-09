package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Game;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Player;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class PlayerServiceImplTest {
    @Autowired
    private PlayerServiceImpl playerService;

    @MockBean
    private GameRepository gameRepository;

    @MockBean
    private PlayerRepository playerRepository;

    @Test
    void buyGameTest() {
        Game testGame = new Game(1L, "TestGame", LocalDate.parse("2023-11-01"));
        gameRepository.save(testGame);

        Player testPlayer = new Player("TestPlayer", "TestPassword");
        playerRepository.save(testPlayer);

        Mockito.when(playerRepository.findByUsername("TestPlayer")).thenReturn(testPlayer);
        Mockito.when(gameRepository.findById(1L)).thenReturn(java.util.Optional.of(testGame));

        playerService.buyGame(testPlayer.getUsername(), testGame.getId());
        assertEquals(1, testPlayer.getOwnedGames().size());
        assertTrue(testPlayer.getOwnedGames().contains(testGame));
        assertEquals(1, testGame.getOwnedBy().size());
        assertTrue(testGame.getOwnedBy().contains(testPlayer));
    }
}
