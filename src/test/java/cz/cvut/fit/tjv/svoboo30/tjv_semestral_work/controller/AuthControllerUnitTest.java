package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class AuthControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testAccessForUserRole() throws Exception {
        mockMvc.perform(get("/api/game"))
                .andExpect(status().isOk());
    }

    @Test
    void testAccessDeniedForUnauthenticatedUsers() throws Exception {
        mockMvc.perform(post("/api/game"))
                .andExpect(status().isUnauthorized());
    }
}
