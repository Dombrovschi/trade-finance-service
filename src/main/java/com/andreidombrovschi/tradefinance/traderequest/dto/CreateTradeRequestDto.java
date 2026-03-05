package com.andreidombrovschi.tradefinance.traderequest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTradeRequestDto {

    @NotNull
    private Long importerId;

    @NotNull
    private Long bankId;

    @NotNull
    private Long exporterId;

    @NotBlank
    @Size(max = 500)
    private String goodsDescription;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;
}
