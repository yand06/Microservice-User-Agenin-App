package com.jdt16.agenin.users.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class UserAdminUpdateCommissionsResponse {
    @JsonProperty("roleName")
    private String updateCommissionsEntityDTORoleName;

    @JsonProperty("userFullName")
    private String updateCommissionsEntityDTOUserFullName;

    @JsonProperty("productName")
    private String updateCommissionsEntityDTOProductName;

    @JsonProperty("commissionsValue")
    private BigDecimal updateCommissionsEntityDTOValue;

    @JsonProperty("commissionsCreatedDate")
    private LocalDateTime updateCommissionsEntityDTOCreatedDate;

    @JsonProperty("commissionsUpdatedDate")
    private LocalDateTime updateCommissionsEntityDTOUpdatedDate;
}
