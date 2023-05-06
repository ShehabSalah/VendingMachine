package com.flapkap.challenge.controllers;

import com.flapkap.challenge.dto.ResponseDTO;
import com.flapkap.challenge.dto.TransactionDTO;
import com.flapkap.challenge.dto.product.ProductDTO;
import com.flapkap.challenge.entities.Product;
import com.flapkap.challenge.exceptions.BadRequestException;
import com.flapkap.challenge.exceptions.EntityNotFoundException;
import com.flapkap.challenge.services.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * Get all products
     *
     * @return the list of products {@link ProductDTO} with pagination
     * */
    @GetMapping("/")
    public ResponseEntity<?> getAllProducts(Pageable page) {
        log.info("API ---> (/api/v1/products) has been called.");
        log.info("Method Location: {}", this.getClass().getName() + ".getAllProducts()");
        return ResponseEntity.ok(productService.getAllProducts(page));
    }

    /**
     * Find a product by id
     *
     * @param id the product id
     * @return the product {@link ProductDTO}
     * @throws EntityNotFoundException if the product doesn't exist
     * */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) throws EntityNotFoundException {
        log.info("API ---> (/api/v1/products/{id}) has been called.");
        log.info("Method Location: {}", this.getClass().getName() + ".getProductById()");
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * Find my products
     *
     * @return the list of products {@link ProductDTO} with pagination
     * */
    @GetMapping("/my-products")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<?> getMyProducts(Pageable page) {
        log.info("API ---> (/api/v1/products/my-products) has been called.");
        log.info("Method Location: {}", this.getClass().getName() + ".getMyProducts()");
        return ResponseEntity.ok(productService.getMyProducts(page));
    }

    /**
     * Create a new product
     *
     * @param product the product information to be created
     * @return the created product {@link ProductDTO}
     * @throws EntityNotFoundException if the product doesn't exist
     * @throws BadRequestException if the product already exists
     * */
    @PostMapping("/")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<?> createProduct(@RequestBody Product product) throws EntityNotFoundException, BadRequestException {
        log.info("API ---> (/api/v1/products) has been called.");
        log.info("Method Location: {}", this.getClass().getName() + ".createProduct()");
        log.info("Request body: {}", product);
        ProductDTO productDTO = productService.createProduct(product);
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/products/" + productDTO.getId()).toUriString());
        return ResponseEntity.created(uri).body(
                ResponseDTO.builder()
                        .message("Product has been created successfully")
                        .data(productDTO)
                        .build()
        );
    }

    /**
     * Update a product
     *
     * @param id the product id
     * @param product the product information to be updated
     * @return the updated product {@link ProductDTO}
     * @throws EntityNotFoundException if the product doesn't exist
     * */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product product) throws EntityNotFoundException, BadRequestException {
        log.info("API ---> (/api/v1/products/{id}) has been called.");
        log.info("Method Location: {}", this.getClass().getName() + ".updateProduct()");
        log.info("Request body: {}", product);
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message("Product has been updated successfully")
                        .data(productService.updateProduct(id, product))
                        .build()
        );
    }

    /**
     * Delete a product by id
     *
     * @param id the product id
     * @throws EntityNotFoundException if the product doesn't exist
     * @throws BadRequestException if the user try to delete a product that doesn't belong to him
     * */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<?> deleteProductById(@PathVariable Long id) throws EntityNotFoundException, BadRequestException {
        log.info("API ---> (/api/v1/products/{id}) has been called.");
        log.info("Method Location: {}", this.getClass().getName() + ".deleteProductById()");
        productService.deleteById(id);
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message("Product has been deleted successfully")
                        .build()
        );
    }

    /**
     * Buy a product by id and product amount
     *
     * @param id the product id
     * @param amount the quantity to buy
     * @return the transaction details {@link TransactionDTO}
     * @throws EntityNotFoundException if the product doesn't exist
     * @throws BadRequestException if the product amount is less than the amount to buy
     * */
    @PostMapping("/{id}/buy")
    @PreAuthorize("hasRole('ROLE_BUYER')")
    public ResponseEntity<?> buyProduct(@PathVariable Long id, @RequestParam Integer amount) throws EntityNotFoundException, BadRequestException {
        log.info("API ---> (/api/v1/products/{id}/buy) has been called.");
        log.info("Method Location: {}", this.getClass().getName() + ".buyProduct()");
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message("Product has been bought successfully")
                        .data(productService.buyProduct(id, amount))
                        .build()
        );
    }
}
