package com.andreidombrovschi.tradefinance.traderequest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExporterDeclineDto {
    @NotBlank
    private String reason;
}
