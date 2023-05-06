package com.flapkap.challenge.services.product;

import com.flapkap.challenge.dto.TransactionDTO;
import com.flapkap.challenge.dto.product.ProductDTO;
import com.flapkap.challenge.entities.Product;
import com.flapkap.challenge.entities.User;
import com.flapkap.challenge.exceptions.BadRequestException;
import com.flapkap.challenge.exceptions.EntityNotFoundException;
import com.flapkap.challenge.repositories.ProductRepository;
import com.flapkap.challenge.services.user.UserService;
import com.flapkap.challenge.utils.AllowedPrices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final UserService userService;

    @Override
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(Product::toDTO);
    }

    @Override
    public ProductDTO getProductById(Long id) throws EntityNotFoundException {
        return productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Product not found")).toDTO();
    }

    @Override
    public Page<ProductDTO> getMyProducts(Pageable pageable) {
        User user = userService.getCurrentUser();
        return productRepository.findBySellerId(user.getId(), pageable).map(Product::toDTO);
    }

    @Override
    public ProductDTO createProduct(Product product) throws EntityNotFoundException, BadRequestException {
        User user = userService.getCurrentUser();
        product.setSeller(user);

        // check if the product already exists
        if (productRepository.findByProductName(product.getProductName()).isPresent()) {
            throw new BadRequestException("Product already exists");
        }

        // check if the cost is negative
        if (product.getCost() < 0) {
            throw new BadRequestException("Cost cannot be negative");
        }

        // check the product cost is allowed
        if (!AllowedPrices.isAllowedPrice(product.getCost())) {
            throw new BadRequestException("Invalid cost. Allowed prices are 5, 10, 20, 50 or 100 cent coins");
        }

        // check if the amount is negative or zero
        if (product.getAmountAvailable() <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }

        return productRepository.save(product).toDTO();
    }

    @Override
    public ProductDTO updateProduct(Long id, Product product) throws EntityNotFoundException, BadRequestException {
        // get the product by id
        Product productToUpdate = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        // get the current user
        User user = userService.getCurrentUser();

        // check if the current user is the seller of the product
        if (!Objects.equals(productToUpdate.getSeller().getId(), user.getId())) {
            throw new EntityNotFoundException("Product not found");
        }

        // check if the product name already exists except for the current product
        if (productRepository.findByProductNameAndIdNot(product.getProductName(), id).isPresent()) {
            throw new BadRequestException("Product already exists");
        }

        // check if the cost is negative
        if (product.getCost() < 0) {
            throw new BadRequestException("Cost cannot be negative");
        }

        // check the product cost is allowed
        if (!AllowedPrices.isAllowedPrice(product.getCost())) {
            throw new BadRequestException("Invalid cost. Allowed prices are 5, 10, 20, 50 or 100 cent coins");
        }

        // check if the amount is negative or zero
        if (product.getAmountAvailable() <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }

        // update the product
        productToUpdate.setProductName(product.getProductName());
        productToUpdate.setCost(product.getCost());
        productToUpdate.setAmountAvailable(product.getAmountAvailable());

        // save the product
        return productRepository.save(productToUpdate).toDTO();
    }

    @Override
    public void deleteById(Long id) throws EntityNotFoundException, BadRequestException {
        // get the product by id
        Product product = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Product not found"));

        // get the current user
        User user = userService.getCurrentUser();

        // check if the current user is the seller of the product
        if (!Objects.equals(product.getSeller().getId(), user.getId())) {
            throw new EntityNotFoundException("Product not found");
        }

        // delete the product
        productRepository.deleteById(id);
    }

    @Override
    public TransactionDTO buyProduct(Long id, Integer productAmount) throws EntityNotFoundException, BadRequestException {
        // get the product by id
        Product product = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Product not found"));

        // check if the amount is negative
        if (productAmount < 0) {
            throw new BadRequestException("Amount cannot be negative");
        }

        // check if the amount is zero
        if (productAmount == 0) {
            throw new BadRequestException("Amount cannot be zero");
        }

        // check if the amount is greater than the amount available
        if (productAmount > product.getAmountAvailable()) {
            throw new BadRequestException("There are not enough products available");
        }

        // get the current user
        User user = userService.getCurrentUser();

        // check if the current user has enough money
        if (user.getDeposit() < product.getCost() * productAmount) {
            throw new BadRequestException("Not sufficient funds. Please deposit more money");
        }

        // update the product
        product.setAmountAvailable(product.getAmountAvailable() - productAmount);

        // update the user balance
        user.setDeposit(user.getDeposit() - product.getCost() * productAmount);

        // save the product
        productRepository.save(product);

        // save the user
        userService.updateUser(user.getId(), user);

        // return the transaction
        return TransactionDTO.builder()
                .total(product.getCost() * productAmount)
                .change(user.getDeposit())
                .product(product.toDTO())
                .amount(productAmount)
                .build();
    }

}
