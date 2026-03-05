package com.andreidombrovschi.tradefinance.traderequest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectTradeRequestDto {
    @NotBlank
    private String reason;
}