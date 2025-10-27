package com.jdt16.agenin.users.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserAdminUpdateCommissionsRequest {
    @NotNull(message = "commissionsValue is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "commissionsValue must be > 0")
    @JsonProperty("commissionsValue")
    private BigDecimal commissionsEntityDTOValue;
}
