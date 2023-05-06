package com.flapkap.challenge;

import com.flapkap.challenge.entities.User;
import com.flapkap.challenge.entities.enums.UserRole;
import com.flapkap.challenge.repositories.UserRepository;
import com.flapkap.challenge.services.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthControllerTest {

    private static MockHttpServletRequest request;
    @PersistenceContext
    private EntityManager entityMgr;
    @Mock
    private UserService userServiceMock;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setupDatabase() {
        User user = new User();
        user.setUsername("testAdmin");
        user.setPassword(passwordEncoder.encode("testpassword"));
        user.setRole(UserRole.ROLE_ADMIN);
        userRepository.save(user);
    }

    @Test
    public void loginTest_withCorrectUsernameAndCorrectPassword() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType("application/json")
                .content("{\"username\":\"testAdmin\",\"password\":\"testpassword\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.user.username").value("testAdmin"))
                .andExpect(jsonPath("$.user.role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    public void loginTest_withCorrectUsernameAndWrongPassword() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"testAdmin\",\"password\":\"testpasswordx\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Bad credentials"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    public void loginTest_withWrongUsernameAndWrongPassword() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"testAdminx\",\"password\":\"testpasswordx\"}"))
                .andExpect(status().isBadRequest());
    }

    @AfterEach
    public void tearDown() {
        jdbc.execute("DELETE FROM users WHERE username = 'testAdmin'");
    }
}
