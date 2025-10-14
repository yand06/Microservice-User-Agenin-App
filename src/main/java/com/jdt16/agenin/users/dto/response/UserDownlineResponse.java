package com.jdt16.agenin.users.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDownlineResponse {

  @JsonProperty("userId")
  private UUID userEntityDTOId;

  @JsonProperty("userFullName")
  private String userEntityDTOFullName;

  @JsonProperty("userPhoneNumber")
  private String userEntityDTOPhoneNumber;

  @JsonProperty("userEmail")
  private String userEntityDTOEmail;

  @JsonProperty("commissionValue")
  private BigDecimal userEntityDTOCommissionValue;
}