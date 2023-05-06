package com.flapkap.challenge.entities;

import com.flapkap.challenge.dto.product.ProductDTO;
import com.flapkap.challenge.entities.base.BaseEntityAudit;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Table(name="products")
public class Product extends BaseEntityAudit {
    @Column(nullable = false)
    @NotBlank(message = "Product name cannot be blank")
    @Size(min = 3, max = 50, message = "Product name must be between 3 and 20 characters")
    private String productName;
    @Column(nullable = false)
    @Min(value = 5, message = "Cost cannot be less than 5 cents")
    @Max(value = 100, message = "Cost cannot be more than 100 cents")
    private int cost; // cost is int because I'm assuming we're dealing with cents, if we're dealing with dollars, then it should be double
    @Column(nullable = false)
    @Min(value = 0, message = "Amount available cannot be less than 0")
    @Max(value = 20, message = "Amount available cannot be more than 20")
    private int amountAvailable;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    public ProductDTO toDTO() {
        return ProductDTO.builder()
                .id(id)
                .productName(productName)
                .cost(cost)
                .amountAvailable(amountAvailable)
                .build();
    }
}
