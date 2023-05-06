package com.flapkap.challenge.services.product;

import com.flapkap.challenge.dto.TransactionDTO;
import com.flapkap.challenge.dto.product.ProductDTO;
import com.flapkap.challenge.entities.Product;
import com.flapkap.challenge.exceptions.BadRequestException;
import com.flapkap.challenge.exceptions.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    /**
     * Return all products in the database with pagination
     *
     * @return a list of all products {@link ProductDTO}
     * */
    Page<ProductDTO> getAllProducts(Pageable pageable);

    /**
     * Find product by id
     *
     * @param id product id
     * @return product the product {@link ProductDTO}
     * @throws EntityNotFoundException if the product does not exist
     * */
    ProductDTO getProductById(Long id) throws EntityNotFoundException;

    /**
     * Return all products of a seller that authenticated with pagination
     *
     * @return a list of all products {@link ProductDTO}
     * */
    Page<ProductDTO> getMyProducts(Pageable pageable);

    /**
     * Create a new product
     *
     * @param product object that contains the product information
     * @return the created product {@link ProductDTO}
     * @throws EntityNotFoundException if the seller does not exist
     * @throws BadRequestException if the product already exists
     * */
    ProductDTO createProduct(Product product) throws EntityNotFoundException, BadRequestException;

    /**
     * Update a product
     *
     * @param id the product id
     * @param product object that contains the product information
     * @return the updated product {@link ProductDTO}
     * @throws EntityNotFoundException if the product does not exist
     * @throws BadRequestException if trying to update the product name to an existing product name
     * */
    ProductDTO updateProduct(Long id, Product product) throws EntityNotFoundException, BadRequestException;

    /**
     * Delete a product by id
     *
     * @param id the product id
     * @throws EntityNotFoundException if the product does not exist
     * @throws BadRequestException if the user try to delete a product that doesn't belong to him
     * */
    void deleteById(Long id) throws EntityNotFoundException, BadRequestException;

    /**
     * Buy a product by id and the amount of the product
     *
     * @param id the product id
     * @param productAmount the amount of the product
     * @return the transaction {@link TransactionDTO}
     * @throws EntityNotFoundException if the product does not exist
     * @throws BadRequestException if the amount is greater than the available amount
     * @throws BadRequestException if the user not have enough money to buy the product
     * */
    TransactionDTO buyProduct(Long id, Integer productAmount) throws EntityNotFoundException, BadRequestException;

}
