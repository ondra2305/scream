package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.controller;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.auth.JwtTokenUtil;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Game;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Player;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Review;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.dtos.ReviewDTO;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.GameService;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.PlayerService;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service.ReviewService;
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

import java.time.LocalDate;
import java.util.Optional;

//@WebMvcTest(ReviewController.class)
@SpringBootTest
@AutoConfigureMockMvc
class ReviewControllerApiTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private GameService gameService;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    private Game mockGame;
    private Player mockPlayer;

    @BeforeEach
    void setUpTest() {
        mockGame = new Game(1L, "TestGame", LocalDate.parse("2023-11-01"));
        Mockito.when(gameService.readById(1L)).thenReturn(Optional.of(mockGame));
        mockPlayer = new Player(1L, "TestPlayer");
        Mockito.when(playerService.readById(1L)).thenReturn(Optional.of(mockPlayer));

        Claims mockClaims = Mockito.mock(Claims.class);
        Mockito.when(mockClaims.getSubject()).thenReturn("TestPlayer");
        Mockito.when(jwtTokenUtil.validateToken("mockToken")).thenReturn(mockClaims);
        Mockito.when(jwtTokenUtil.getUsernameFromToken("mockToken")).thenReturn("TestPlayer");

        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
    }

    @Test
    @WithMockUser(username = "TestPlayer", roles = {"ADMIN"})
    void create() throws Exception {
        Review mockReview = new Review(1L, LocalDate.parse("2023-12-30"), 10, mockGame, mockPlayer);
        mockReview.setBelongsTo(mockGame);
        mockReview.setWrittenBy(mockPlayer);
        Mockito.when(reviewService.writeReview(Mockito.any(ReviewDTO.class))).thenReturn(mockReview);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post("/api/review")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"points\": 10, \"dateAdded\": \"2023-12-30\", \"belongsTo\": 1, \"writtenBy\": 1}")
                ).andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.points", Matchers.is(10)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.dateAdded", Matchers.is("2023-12-30")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.belongsTo", Matchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.writtenBy", Matchers.is(1)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void readById() throws Exception {
        Review mockReview = new Review(1L, LocalDate.parse("2023-12-30"), 10, mockGame, mockPlayer);
        mockReview.setBelongsTo(mockGame);
        mockReview.setWrittenBy(mockPlayer);

        Mockito.when(reviewService.readById(1L)).thenReturn(Optional.of(mockReview));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/review/{id}", 1L))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.points", Matchers.is(10)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.dateAdded", Matchers.is("2023-12-30")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.belongsTo", Matchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.writtenBy", Matchers.is(1)));
    }

    @Test
    @WithMockUser(username = "TestPlayer", roles = {"ADMIN"})
    void deleteById() throws Exception {
        Review mockReview = new Review(1L, LocalDate.parse("2023-12-30"), 10, mockGame, mockPlayer);
        mockReview.setBelongsTo(mockGame);
        mockReview.setWrittenBy(mockPlayer);

        Mockito.when(reviewService.readById(1L)).thenReturn(Optional.of(mockReview));
        Mockito.doNothing().when(reviewService).deleteById(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/review/{id}", 1L).header("Authorization", "Bearer mockToken"))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(reviewService, Mockito.times(1)).deleteById(1L);
    }
}
