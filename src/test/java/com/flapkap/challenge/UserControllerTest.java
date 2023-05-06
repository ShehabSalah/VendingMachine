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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserControllerTest {

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

    private String adminToken;
    private String sellerToken;
    private String buyerToken;


    @BeforeEach
    public void setupDatabase() throws Exception {
        User admin = new User();
        admin.setUsername("testAdmin");
        admin.setPassword(passwordEncoder.encode("testpassword"));
        admin.setRole(UserRole.ROLE_ADMIN);
        userRepository.save(admin);

        User seller = new User();
        seller.setUsername("testSeller");
        seller.setPassword(passwordEncoder.encode("testpassword"));
        seller.setRole(UserRole.ROLE_SELLER);
        userRepository.save(seller);

        User buyer = new User();
        buyer.setUsername("testBuyer");
        buyer.setPassword(passwordEncoder.encode("testpassword"));
        buyer.setRole(UserRole.ROLE_BUYER);
        userRepository.save(buyer);

        String response = null;

        // login with the admin user
        response = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"testAdmin\",\"password\":\"testpassword\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // parse the response and get the token
        adminToken = response.substring(response.indexOf("token") + 8, response.indexOf("type") - 3);

        // login with the seller user
        response = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"testSeller\",\"password\":\"testpassword\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // parse the response and get the token
        sellerToken = response.substring(response.indexOf("token") + 8, response.indexOf("type") - 3);

        // login with the buyer user
        response = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"testBuyer\",\"password\":\"testpassword\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // parse the response and get the token
        buyerToken = response.substring(response.indexOf("token") + 8, response.indexOf("type") - 3);
    }

    @Test
    public void getAllUsersTest_withAdminAuthorizationToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.content.length()").value(4))
                .andExpect(jsonPath("$.content[1].username").value("testAdmin"))
                .andExpect(jsonPath("$.content[2].username").value("testSeller"))
                .andExpect(jsonPath("$.content[3].username").value("testBuyer"));
    }

    @Test
    public void getAllUsersTest_withSellerAuthorizationToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getAllUsersTest_withBuyerAuthorizationToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getAllUsersTest_withNoAuthorizationToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Full authentication is required to access this resource"));
    }

    @Test
    public void getAllUsersTest_withPageNumberOne() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/?page=1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    public void getAllUsersTest_withPageTwoAndSizeTwo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/?page=1&size=2")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].username").value("testSeller"))
                .andExpect(jsonPath("$.content[1].username").value("testBuyer"));
    }

    @Test
    public void createUserTest_withCorrectUserInfo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/")
                        .contentType("application/json")
                        .content("{\"username\":\"testBuyer_update\",\"password\":\"testpassword123\",\"role\":\"ROLE_ADMIN\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("User has been created successfully"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testBuyer_update"))
                .andExpect(jsonPath("$.data.role").value("ROLE_ADMIN"));

        assertEquals(5, userRepository.count());
    }

    @Test
    public void createUserTest_withExistingUsername() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/")
                        .contentType("application/json")
                        .content("{\"username\":\"testAdmin\",\"password\":\"testpassword\",\"role\":\"ROLE_BUYER\"}"))
                .andExpect(status().isBadRequest());

        assertEquals(4, userRepository.count());
    }

    @Test
    public void createUserTest_withNoUsername() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/")
                        .contentType("application/json")
                        .content("{\"password\":\"testpassword\",\"role\":\"ROLE_BUYER\"}"))
                .andExpect(status().isBadRequest());

        assertEquals(4, userRepository.count());
    }

    @Test
    public void createUserTest_withNoPassword() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/")
                        .contentType("application/json")
                        .content("{\"username\":\"testUser\",\"role\":\"ROLE_BUYER\"}"))
                .andExpect(status().isBadRequest());

        assertEquals(4, userRepository.count());
    }

    @Test
    public void createUserTest_withNoRole() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/")
                        .contentType("application/json")
                        .content("{\"username\":\"testUser\",\"password\":\"testpassword\"}"))
                .andExpect(status().isBadRequest());

        assertEquals(4, userRepository.count());
    }

    @Test
    public void createUserTest_withInvalidRole() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/")
                        .contentType("application/json")
                        .content("{\"username\":\"testUser\",\"password\":\"testpassword\",\"role\":\"ROLE_TEST\"}"))
                .andExpect(status().isBadRequest());

        assertEquals(4, userRepository.count());
    }

    @Test
    public void updateUserTest_withCorrectUserInfo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/1")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"username\":\"testUser\",\"password\":\"testpassword\",\"role\":\"ROLE_BUYER\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("User has been updated successfully"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testUser"))
                .andExpect(jsonPath("$.data.role").value("ROLE_BUYER"));

        assertEquals(4, userRepository.count());
    }

    @Test
    public void updateUserTest_withSellerToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/1")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + sellerToken)
                        .content("{\"username\":\"testAdmin\",\"password\":\"testpassword\",\"role\":\"ROLE_BUYER\"}"))
                .andExpect(status().isForbidden());

        assertEquals(4, userRepository.count());
    }

    @Test
    public void updateUserTest_withBuyerToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/1")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + buyerToken)
                        .content("{\"username\":\"testAdmin\",\"password\":\"testpassword\",\"role\":\"ROLE_BUYER\"}"))
                .andExpect(status().isForbidden());

        assertEquals(4, userRepository.count());
    }

    @Test
    public void updateUserTest_withNonExistingUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/5")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"username\":\"testAdmin\",\"password\":\"testpassword\",\"role\":\"ROLE_BUYER\"}"))
                .andExpect(status().isNotFound());

        assertEquals(4, userRepository.count());
    }

    @Test
    public void updateUserTest_withExistingUsername() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/1")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"username\":\"testSeller\",\"password\":\"testpassword\",\"role\":\"ROLE_BUYER\"}"))
                .andExpect(status().isBadRequest());

        assertEquals(4, userRepository.count());
    }

    @Test
    public void updateUserTest_withNoUsername() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/1")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"password\":\"testpassword\",\"role\":\"ROLE_BUYER\"}"))
                .andExpect(status().isBadRequest());

        assertEquals(4, userRepository.count());
    }

    @Test
    public void updateUserTest_withNoPassword() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/1")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"username\":\"testUser\",\"role\":\"ROLE_BUYER\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("User has been updated successfully"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testUser"))
                .andExpect(jsonPath("$.data.role").value("ROLE_BUYER"));

        assertEquals(4, userRepository.count());
    }

    @Test
    public void updateUserTest_withNoRole() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/1")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"username\":\"testUser\",\"password\":\"testpassword\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("User has been updated successfully"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testUser"))
                .andExpect(jsonPath("$.data.role").value("ROLE_ADMIN"));

        assertEquals(4, userRepository.count());
    }

    @Test
    public void updateUserTest_withInvalidRole() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/1")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"username\":\"testUser\",\"password\":\"testpassword\",\"role\":\"ROLE_TEST\"}"))
                .andExpect(status().isBadRequest());

        assertEquals(4, userRepository.count());
    }

    @Test
    public void updateUserTest_withOnlyUsername() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/1")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"username\":\"testUser\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("User has been updated successfully"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testUser"))
                .andExpect(jsonPath("$.data.role").value("ROLE_ADMIN"));

        assertEquals(4, userRepository.count());
    }

    @Test
    public void deleteUserTest_withCorrectUserId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/users/1")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("User has been deleted successfully"))
                .andExpect(jsonPath("$.success").value(true));

        assertEquals(3, userRepository.count());
    }

    @Test
    public void deleteUserTest_withSellerToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/users/1")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isForbidden());

        assertEquals(4, userRepository.count());
    }

    @Test
    public void deleteUserTest_withBuyerToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/users/1")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isForbidden());

        assertEquals(4, userRepository.count());
    }

    @Test
    public void deleteUserTest_withNonExistingUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/users/5")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());

        assertEquals(4, userRepository.count());
    }

    @Test
    public void deleteUserTest_withInvalidUserId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/users/abc")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());

        assertEquals(4, userRepository.count());
    }

    @Test
    public void deleteUserTest_withNoUserId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/users/")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isMethodNotAllowed());

        assertEquals(4, userRepository.count());
    }

    @Test
    public void deleteUserTest_withNoToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/users/1")
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Full authentication is required to access this resource"));

        assertEquals(4, userRepository.count());
    }

    @Test
    public void getUserByIdTest_withCorrectUserId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/1")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));

        assertEquals(4, userRepository.count());
    }

    @Test
    public void getUserByIdTest_withSellerToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/2")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isForbidden());

        assertEquals(4, userRepository.count());
    }

    @Test
    public void getUserByIdTest_withBuyerToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/2")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isForbidden());

        assertEquals(4, userRepository.count());
    }

    @Test
    public void getUserByIdTest_withNonExistingUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/5")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());

        assertEquals(4, userRepository.count());
    }

    @Test
    public void getUserByIdTest_withInvalidUserId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/abc")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());

        assertEquals(4, userRepository.count());
    }

    @Test
    public void getUserByIdTest_withNoToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/2")
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Full authentication is required to access this resource"));

        assertEquals(4, userRepository.count());
    }

    @Test
    public void getUserInfoTest_withAdminToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/profile")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.username").value("testAdmin"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));

        assertEquals(4, userRepository.count());
    }

    @Test
    public void getUserInfoTest_withSellerToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/profile")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.username").value("testSeller"))
                .andExpect(jsonPath("$.role").value("ROLE_SELLER"));

        assertEquals(4, userRepository.count());
    }

    @Test
    public void getUserInfoTest_withBuyerToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/profile")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.username").value("testBuyer"))
                .andExpect(jsonPath("$.role").value("ROLE_BUYER"));

        assertEquals(4, userRepository.count());
    }

    @Test
    public void getUserInfoTest_withNoToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/profile")
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Full authentication is required to access this resource"));

        assertEquals(4, userRepository.count());
    }

    @Test
    public void getUserInfoTest_withInvalidToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/profile")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + "invalidToken"))

                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Full authentication is required to access this resource"));

        assertEquals(4, userRepository.count());
    }

    @Test
    public void updateUserInfoTest_withAdminToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/profile")
                        .contentType("application/json")
                        .content("{\"username\": \"testUser\", \"password\": \"testAdmin\"}")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data.username").value("testUser"))
                .andExpect(jsonPath("$.data.role").value("ROLE_ADMIN"));

        assertEquals(4, userRepository.count());
    }

    @Test
    public void updateUserInfoTest_withSellerToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/profile")
                        .contentType("application/json")
                        .content("{\"username\": \"testUser\", \"password\": \"testSeller\"}")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data.username").value("testUser"))
                .andExpect(jsonPath("$.data.role").value("ROLE_SELLER"));

        assertEquals(4, userRepository.count());
    }

    @Test
    public void updateUserInfoTest_withBuyerToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/profile")
                        .contentType("application/json")
                        .content("{\"username\": \"testUser\", \"password\": \"testBuyer\"}")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data.username").value("testUser"))
                .andExpect(jsonPath("$.data.role").value("ROLE_BUYER"));

        assertEquals(4, userRepository.count());
    }

    @Test
    public void updateUserInfoTest_withOnlyUsername() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/profile")
                        .contentType("application/json")
                        .content("{\"username\": \"testUser\"}")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data.username").value("testUser"))
                .andExpect(jsonPath("$.data.role").value("ROLE_ADMIN"));

        assertEquals(4, userRepository.count());
    }

    @Test
    public void updateUserInfoTest_withOnlyPassword() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/profile")
                        .contentType("application/json")
                        .content("{\"password\": \"testUser\"}")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());

        assertEquals(4, userRepository.count());
    }

    @Test
    public void updateUserInfoTest_withOnlyRole() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/profile")
                        .contentType("application/json")
                        .content("{\"role\": \"ROLE_ADMIN\"}")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());

        assertEquals(4, userRepository.count());
    }

    @Test
    public void updateUserInfoTest_withDifferentRole() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/profile")
                        .contentType("application/json")
                        .content("{\"username\": \"testUser\", \"role\": \"ROLE_SELLER\"}")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data.username").value("testUser"))
                .andExpect(jsonPath("$.data.role").value("ROLE_ADMIN"));

        assertEquals(4, userRepository.count());
    }

    @Test
    public void updateUserInfoTest_withNoToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/profile")
                        .contentType("application/json")
                        .content("{\"username\": \"testUser\", \"password\": \"testAdmin\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Full authentication is required to access this resource"));

        assertEquals(4, userRepository.count());
    }

    @Test
    public void depositMoneyTest_withAdminToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/deposit/50")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void depositMoneyTest_withSellerToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/deposit/50")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void depositMoneyTest_withValidAmountAndBuyerToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/deposit/50")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("Money has been deposited successfully"))
                .andExpect(jsonPath("$.success").value(true));

        assertTrue(userRepository.findByUsername("testBuyer").isPresent());
        assertEquals(50, userRepository.findByUsername("testBuyer").get().getDeposit());
    }

    @Test
    public void depositMoneyTest_withInValidAmountAndBuyerToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/deposit/15")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isBadRequest());

        assertTrue(userRepository.findByUsername("testBuyer").isPresent());
        assertEquals(0, userRepository.findByUsername("testBuyer").get().getDeposit());
    }

    @Test
    public void depositMoneyTest_withNoToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/deposit/15")
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Full authentication is required to access this resource"));
    }

    @Test
    public void resetDepositTest_withAdminToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/reset")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void resetDepositTest_withSellerToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/reset")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void resetDepositTest_withBuyerToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/reset")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk());

        assertTrue(userRepository.findByUsername("testBuyer").isPresent());
        assertEquals(0, userRepository.findByUsername("testBuyer").get().getDeposit());
    }

    @Test
    public void resetDepositTest_withNoToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/reset")
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Full authentication is required to access this resource"));
    }

    @AfterEach
    public void tearDown() {
        jdbc.execute("DELETE FROM users WHERE username = 'testAdmin'");
        jdbc.execute("DELETE FROM users WHERE username = 'testSeller'");
        jdbc.execute("DELETE FROM users WHERE username = 'testBuyer'");
    }
}
