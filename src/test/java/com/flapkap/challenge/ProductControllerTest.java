package com.flapkap.challenge;

import com.flapkap.challenge.entities.Product;
import com.flapkap.challenge.entities.User;
import com.flapkap.challenge.entities.enums.UserRole;
import com.flapkap.challenge.repositories.ProductRepository;
import com.flapkap.challenge.repositories.UserRepository;
import com.flapkap.challenge.services.product.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductControllerTest {

    @Mock
    private ProductService productServiceMock;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private ProductRepository productRepository;
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
        seller = userRepository.save(seller);

        User buyer = new User();
        buyer.setUsername("testBuyer");
        buyer.setPassword(passwordEncoder.encode("testpassword"));
        buyer.setRole(UserRole.ROLE_BUYER);
        userRepository.save(buyer);

        jdbc.execute("INSERT INTO products (id, product_name, cost, amount_available, seller_id) VALUES (100, 'testProduct1', 50, 10, " + seller.getId() + ")");
        jdbc.execute("INSERT INTO products (id, product_name, cost, amount_available, seller_id) VALUES (101, 'testProduct2', 100, 20, " + seller.getId() + ")");

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
    public void getAllProductsTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    public void getAllProductsTest_withPageNumberOne() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products/?page=1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    public void getAllProductsTest_withPageNumberOneSizeOne() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products/?page=1&size=1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    public void getProductByIdTest_withCorrectProductId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products/100"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.productName").value("testProduct1"));
    }

    @Test
    public void getProductByIdTest_withIncorrectProductId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products/3"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getMyProductsTest_withAdminToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products/my-products")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getMyProductsTest_withSellerToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products/my-products")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    public void getMyProductsTest_withBuyerToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products/my-products")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void createProductTest_withAdminToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products/")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("{\"productName\":\"testProduct3\",\"cost\":50,\"amountAvailable\":10}"))
                .andExpect(status().isForbidden());

        assertEquals(2, productRepository.count());
    }

    @Test
    public void createProductTest_withSellerToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products/")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType("application/json")
                        .content("{\"productName\":\"testProduct3\",\"cost\":50,\"amountAvailable\":10}"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("Product has been created successfully"))
                .andExpect(jsonPath("$.data.productName").value("testProduct3"));

        assertEquals(3, productRepository.count());
    }

    @Test
    public void createProductTest_withSellerTokenAndNegativeCost() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products/")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType("application/json")
                        .content("{\"productName\":\"testProduct3\",\"cost\":-50,\"amountAvailable\":10}"))
                .andExpect(status().isBadRequest());

        assertEquals(2, productRepository.count());
    }

    @Test
    public void createProductTest_withSellerTokenAndZeroCost() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products/")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType("application/json")
                        .content("{\"productName\":\"testProduct3\",\"cost\":0,\"amountAvailable\":10}"))
                .andExpect(status().isBadRequest());

        assertEquals(2, productRepository.count());
    }

    @Test
    public void createProductTest_withSellerTokenAndNegativeAmountAvailable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products/")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType("application/json")
                        .content("{\"productName\":\"testProduct3\",\"cost\":50,\"amountAvailable\":-10}"))
                .andExpect(status().isBadRequest());

        assertEquals(2, productRepository.count());
    }

    @Test
    public void createProductTest_withSellerTokenAndZeroAmountAvailable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products/")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType("application/json")
                        .content("{\"productName\":\"testProduct3\",\"cost\":50,\"amountAvailable\":0}"))
                .andExpect(status().isBadRequest());

        assertEquals(2, productRepository.count());
    }

    @Test
    public void createProductTest_withSellerTokenAndIncorrectCost() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products/")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType("application/json")
                        .content("{\"productName\":\"testProduct3\",\"cost\":15,\"amountAvailable\":10}"))
                .andExpect(status().isBadRequest());

        assertEquals(2, productRepository.count());
    }

    @Test
    public void createProductTest_withSellerTokenAndExistingProductName() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products/")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType("application/json")
                        .content("{\"productName\":\"testProduct2\",\"cost\":50,\"amountAvailable\":10}"))
                .andExpect(status().isBadRequest());

        assertEquals(2, productRepository.count());
    }

    @Test
    public void createProductTest_withBuyerToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products/")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType("application/json")
                        .content("{\"productName\":\"testProduct3\",\"cost\":50,\"amountAvailable\":10}"))
                .andExpect(status().isForbidden());

        assertEquals(2, productRepository.count());
    }

    @Test
    public void updateProductTest_withAdminToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/products/100")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("{\"productName\":\"testProduct1\",\"cost\":50,\"amountAvailable\":10}"))
                .andExpect(status().isForbidden());

        assertEquals(2, productRepository.count());
    }

    @Test
    public void updateProductTest_withSellerToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/products/100")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType("application/json")
                        .content("{\"productName\":\"testProduct1\",\"cost\":50,\"amountAvailable\":10}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("Product has been updated successfully"))
                .andExpect(jsonPath("$.data.productName").value("testProduct1"));

        assertEquals(2, productRepository.count());
    }

    @Test
    public void updateProductTest_withSellerTokenAndNegativeCost() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/products/100")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType("application/json")
                        .content("{\"productName\":\"testProduct1\",\"cost\":-50,\"amountAvailable\":10}"))
                .andExpect(status().isBadRequest());

        assertEquals(2, productRepository.count());
    }

    @Test
    public void updateProductTest_withSellerTokenAndZeroCost() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/products/100")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType("application/json")
                        .content("{\"productName\":\"testProduct1\",\"cost\":0,\"amountAvailable\":10}"))
                .andExpect(status().isBadRequest());

        assertEquals(2, productRepository.count());
    }

    @Test
    public void updateProductTest_withSellerTokenAndNegativeAmountAvailable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/products/100")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType("application/json")
                        .content("{\"productName\":\"testProduct1\",\"cost\":50,\"amountAvailable\":-10}"))
                .andExpect(status().isBadRequest());

        assertEquals(2, productRepository.count());
    }

    @Test
    public void updateProductTest_withSellerTokenAndZeroAmountAvailable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/products/100")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType("application/json")
                        .content("{\"productName\":\"testProduct1\",\"cost\":50,\"amountAvailable\":0}"))
                .andExpect(status().isBadRequest());

        assertEquals(2, productRepository.count());
    }

    @Test
    public void updateProductTest_withSellerTokenAndIncorrectCost() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/products/100")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType("application/json")
                        .content("{\"productName\":\"testProduct1\",\"cost\":15,\"amountAvailable\":10}"))
                .andExpect(status().isBadRequest());

        assertEquals(2, productRepository.count());
    }

    @Test
    public void updateProductTest_withSellerTokenAndExistingProductName() throws Exception {

        assertTrue(productRepository.findByProductName("testProduct1").isPresent());
        assertTrue(productRepository.findByProductName("testProduct2").isPresent());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/products/100")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType("application/json")
                        .content("{\"productName\":\"testProduct2\",\"cost\":50,\"amountAvailable\":10}"))
                .andExpect(status().isBadRequest());

        assertEquals(2, productRepository.count());
    }

    @Test
    public void updateProductTest_withSellerTokenAndNonExistingProductId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/products/3")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType("application/json")
                        .content("{\"productName\":\"testProduct1\",\"cost\":50,\"amountAvailable\":10}"))
                .andExpect(status().isNotFound());

        assertEquals(2, productRepository.count());
    }

    @Test
    public void updateProductTest_withSellerTokenAndAnotherSellerProductId() throws Exception {
        // create another seller
        User seller2 = new User();
        seller2.setUsername("testSeller2");
        seller2.setPassword("testSeller2");
        seller2.setRole(UserRole.ROLE_SELLER);
        userRepository.save(seller2);

        assertTrue(userRepository.findByUsername("testBuyer").isPresent());

        // create another seller product
        Product product3 = new Product();
        product3.setProductName("testProduct3");
        product3.setCost(50);
        product3.setAmountAvailable(10);
        product3.setSeller(seller2);
        productRepository.save(product3);

        assertTrue(productRepository.findByProductName("testProduct3").isPresent());

        assertEquals(3, productRepository.count());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/products/3")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType("application/json")
                        .content("{\"productName\":\"testProduct4\",\"cost\":50,\"amountAvailable\":10}"))
                .andExpect(status().isNotFound());

        assertEquals(3, productRepository.count());
    }

    @Test
    public void updateProductTest_withBuyerToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/products/100")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType("application/json")
                        .content("{\"productName\":\"testProduct1\",\"cost\":50,\"amountAvailable\":10}"))
                .andExpect(status().isForbidden());

        assertEquals(2, productRepository.count());
    }

    @Test
    public void deleteProductByIdTest_withAdminToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/products/100")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());

        assertEquals(2, productRepository.count());
    }

    @Test
    public void deleteProductByIdTest_withSellerToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/products/100")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("Product has been deleted successfully"));

        assertEquals(1, productRepository.count());
    }

    @Test
    public void deleteProductByIdTest_withSellerTokenAndNonExistingProductId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/products/3")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isNotFound());

        assertEquals(2, productRepository.count());
    }

    // check if seller can delete another seller product
    @Test
    public void deleteProductByIdTest_withSellerTokenAndAnotherSellerProductId() throws Exception {
        // create another seller
        User seller2 = new User();
        seller2.setUsername("testSeller2");
        seller2.setPassword("testSeller2");
        seller2.setRole(UserRole.ROLE_SELLER);
        userRepository.save(seller2);

        assertTrue(userRepository.findByUsername("testBuyer").isPresent());

        // create another seller product
        jdbc.execute("INSERT INTO products (id, product_name, cost, amount_available, seller_id) VALUES (102, 'testProduct3', 100, 20, " + seller2.getId() + ")");

        assertTrue(productRepository.findByProductName("testProduct3").isPresent());

        assertEquals(3, productRepository.count());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/products/102")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isNotFound());

        assertEquals(3, productRepository.count());
    }

    @Test
    public void deleteProductByIdTest_withBuyerToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/products/100")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isForbidden());

        assertEquals(2, productRepository.count());
    }

    @Test
    public void buyProductTest_withAdminToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products/100/buy")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount", "5"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void buyProductTest_withSellerToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products/100/buy")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount", "5"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void buyProductTest_withBuyerToken() throws Exception {

        assertTrue(userRepository.findByUsername("testBuyer").isPresent());

        User buyer = userRepository.findByUsername("testBuyer").get();
        buyer.setDeposit(120);
        userRepository.save(buyer);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products/100/buy")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("Product has been bought successfully"))
                .andExpect(jsonPath("$.data.total").value(100))
                .andExpect(jsonPath("$.data.change").value(20))
                .andExpect(jsonPath("$.data.product.productName").value("testProduct1"))
                .andExpect(jsonPath("$.data.product.cost").value(50))
                .andExpect(jsonPath("$.data.amount").value(2));


        assertTrue(productRepository.findByProductName("testProduct1").isPresent());
        assertEquals(8, productRepository.findByProductName("testProduct1").get().getAmountAvailable());
    }

    @Test
    public void buyProductTest_withBuyerTokenAndWithNotEnoughBalance() throws Exception {

        assertTrue(userRepository.findByUsername("testBuyer").isPresent());

        User buyer = userRepository.findByUsername("testBuyer").get();
        buyer.setDeposit(10);
        userRepository.save(buyer);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products/100/buy")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount", "2"))
                .andExpect(status().isBadRequest());

        assertTrue(productRepository.findByProductName("testProduct1").isPresent());
        assertEquals(10, productRepository.findByProductName("testProduct1").get().getAmountAvailable());
    }

    @Test
    public void buyProductTest_withBuyerTokenAndAmountGreaterThanTheAvailableAmount() throws Exception {

        assertTrue(userRepository.findByUsername("testBuyer").isPresent());

        User buyer = userRepository.findByUsername("testBuyer").get();
        buyer.setDeposit(120);
        userRepository.save(buyer);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products/100/buy")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount", "20"))
                .andExpect(status().isBadRequest());

        assertTrue(productRepository.findByProductName("testProduct1").isPresent());
        assertEquals(10, productRepository.findByProductName("testProduct1").get().getAmountAvailable());
    }

    @Test
    public void buyProductTest_withBuyerTokenAndZeroAmount() throws Exception {

            assertTrue(userRepository.findByUsername("testBuyer").isPresent());

            User buyer = userRepository.findByUsername("testBuyer").get();
            buyer.setDeposit(120);
            userRepository.save(buyer);

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products/100/buy")
                            .header("Authorization", "Bearer " + buyerToken)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("amount", "0"))
                    .andExpect(status().isBadRequest());

            assertTrue(productRepository.findByProductName("testProduct1").isPresent());
            assertEquals(10, productRepository.findByProductName("testProduct1").get().getAmountAvailable());
    }

    @Test
    public void buyProductTest_withBuyerTokenAndNegativeAmount() throws Exception {
            assertTrue(userRepository.findByUsername("testBuyer").isPresent());

            User buyer = userRepository.findByUsername("testBuyer").get();
            buyer.setDeposit(120);
            userRepository.save(buyer);

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products/100/buy")
                            .header("Authorization", "Bearer " + buyerToken)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("amount", "-1"))
                    .andExpect(status().isBadRequest());

            assertTrue(productRepository.findByProductName("testProduct1").isPresent());
            assertEquals(10, productRepository.findByProductName("testProduct1").get().getAmountAvailable());
    }

    @Test
    public void buyProductTest_withBuyerTokenAndNonExistingProductId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products/3/buy")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount", "2"))
                .andExpect(status().isNotFound());
    }

    @AfterEach
    public void tearDown() {
        jdbc.execute("DELETE FROM products WHERE product_name = 'testProduct1'");
        jdbc.execute("DELETE FROM products WHERE product_name = 'testProduct2'");
        jdbc.execute("DELETE FROM products WHERE product_name = 'testProduct3'");

        jdbc.execute("DELETE FROM users WHERE username = 'testAdmin'");
        jdbc.execute("DELETE FROM users WHERE username = 'testSeller'");
        jdbc.execute("DELETE FROM users WHERE username = 'testBuyer'");
        jdbc.execute("DELETE FROM users WHERE username = 'testSeller2'");
    }
}
