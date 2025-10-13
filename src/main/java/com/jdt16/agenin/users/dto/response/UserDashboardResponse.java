package com.jdt16.agenin.users.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDashboardResponse {
    @JsonProperty("usersBalanceAmount")
    private BigDecimal userEntityDTOBalanceAmount;

    @JsonProperty("userReferralCode")
    private String userReferralEntityDTOCode;

}
