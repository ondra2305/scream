package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.controller;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.auth.JwtTokenUtil;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Game;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Player;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.GameService;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.PlayerService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.sql.Date;
import java.util.Optional;

//@WebMvcTest(controllers = GameController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class GameControllerApiTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @BeforeEach
    void setUpTest() {
        Player mockPlayer = new Player(1L, "mock");
        Mockito.when(playerService.isAdmin("mock")).thenReturn(true);
        Mockito.when(playerService.readById(1L)).thenReturn(Optional.of(mockPlayer));

        Claims mockClaims = Mockito.mock(Claims.class);
        Mockito.when(mockClaims.getSubject()).thenReturn("mock");
        Mockito.when(jwtTokenUtil.validateToken("mockToken")).thenReturn(mockClaims);
        Mockito.when(jwtTokenUtil.getUsernameFromToken("mockToken")).thenReturn("mock");

        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
    }

    @Test
    @WithMockUser(username = "mock", roles = {"ADMIN"})
    void create() throws Exception {
        Game mockGame = new Game();
        mockGame.setName("testGame");
        mockGame.setDateReleased(Date.valueOf("2023-01-01").toLocalDate());
        Mockito.when(gameService.create(Mockito.any(Game.class))).thenReturn(mockGame);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post("/api/game").header("Authorization", "Bearer mockToken")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\": \"test\", \"dateReleased\": \"2023-01-01\"}")
                ).andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is("testGame")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.dateReleased", Matchers.is("2023-01-01")))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void readById() throws Exception {
        Game mockGame = new Game();
        mockGame.setId(1L);
        mockGame.setName("testGame");
        mockGame.setDateReleased(Date.valueOf("2023-01-01").toLocalDate());

        Mockito.when(gameService.readById(1L)).thenReturn(Optional.of(mockGame));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/game/{id}", 1L))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(1)));
    }

    @Test
    @WithMockUser(username = "mock", roles = {"ADMIN"})
    void deleteById() throws Exception {
        Mockito.doNothing().when(gameService).deleteById(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/game/{id}", 1L).header("Authorization", "Bearer mockToken"))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(gameService, Mockito.times(1)).deleteById(1L);
    }
}
