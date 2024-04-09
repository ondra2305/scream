package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerIntTest {

    @Autowired
    private MockMvc mockMvc;

    private static String jwtToken;

    @Test
    void testUserLoginWrongPassword() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"test\", \"password\": \"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(1)
    void testUserRegistration() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/player")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"test\", \"password\": \"password\"}"))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("test"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.password").doesNotExist());
    }

    @Test
    @Order(2)
    void testUserLogin() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"test\", \"password\": \"password\"}"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token",
                        matchesPattern("^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$")))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        jwtToken = JsonPath.read(responseContent, "$.token");
    }

    @Test
    @Order(3)
    void testAccessToRestricted() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/game/leaderboard/2023/12")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    void testAccessToRestrictedWrongToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/game/leaderboard/2023/12")
                        .header("Authorization", "Bearer " + "someWrongToken"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAccessToRestrictedNoToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/game/discount/2023/12/1"))
                .andExpect(status().isUnauthorized());
    }
}
