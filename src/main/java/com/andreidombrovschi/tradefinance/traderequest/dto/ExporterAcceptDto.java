package com.andreidombrovschi.tradefinance.traderequest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExporterAcceptDto {

    @NotNull
    @Min(0)
    private Long price; // sau BigDecimal dacă vrei mai corect

    @NotBlank
    private String deliveryDetails;
}
