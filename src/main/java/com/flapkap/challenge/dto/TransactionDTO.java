package com.flapkap.challenge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.flapkap.challenge.dto.product.ProductDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDTO {
    private int total;
    private int change;
    private ProductDTO product;
    private int amount;
}
