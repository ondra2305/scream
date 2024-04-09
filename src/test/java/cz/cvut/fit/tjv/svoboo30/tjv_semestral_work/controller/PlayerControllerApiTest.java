package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.controller;

import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.auth.JwtTokenUtil;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain.Player;
import cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.dtos.RegisterPlayerDTO;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;


//@WebMvcTest(ReviewController.class)
@SpringBootTest
@AutoConfigureMockMvc
class PlayerControllerApiTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;


    @BeforeEach
    void setUpTest() {
        Claims mockClaims = Mockito.mock(Claims.class);
        Mockito.when(mockClaims.getSubject()).thenReturn("mock");
        Mockito.when(jwtTokenUtil.validateToken("mockToken")).thenReturn(mockClaims);
        Mockito.when(jwtTokenUtil.getUsernameFromToken("mockToken")).thenReturn("mock");

        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
    }

    @Test
    void registerNewPlayer() throws Exception {
        Player mockPlayer = new Player();
        mockPlayer.setId(1L);
        mockPlayer.setUsername("mock");
        mockPlayer.setPassword("mock");

        Mockito.when(playerService.registerNewPlayer(new RegisterPlayerDTO("mock", "mock"))).thenReturn(mockPlayer);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/api/player")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"mock\", \"password\": \"mock\"}")
        ).andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    void findByUsername() throws Exception {
        String username = "test";
        Player mockPlayer = new Player();
        mockPlayer.setId(1L);
        mockPlayer.setUsername(username);

        Mockito.when(playerService.findByUsername(username)).thenReturn(Optional.of(mockPlayer));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/player/{username}", username))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username", Matchers.is(username)));
    }

    @Test
    @WithMockUser(username = "mock", roles = {"ADMIN"})
    void deleteByUsername() throws Exception {
        String username = "mock";

        Mockito.doNothing().when(playerService).deleteByUsername(username);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/player/{username}", username).header("Authorization", "Bearer mockToken"))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(playerService, Mockito.times(1)).deleteByUsername(username);
    }
}
