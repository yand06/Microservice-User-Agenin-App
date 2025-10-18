package com.jdt16.agenin.users.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserAdminUpdateCommissionsRequest {
    @JsonProperty("commissionsValue")
    private BigDecimal commissionsEntityDTOValue;
}
