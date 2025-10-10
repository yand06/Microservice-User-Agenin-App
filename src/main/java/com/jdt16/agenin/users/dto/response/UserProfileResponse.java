package com.jdt16.agenin.users.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {

    @JsonProperty("userId")
    private UUID userEntityDTOId;

    @JsonProperty("userFullName")
    private String userEntityDTOFullName;

    @JsonProperty("userEmail")
    private String userEntityDTOEmail;

    @JsonProperty("userPhoneNumber")
    private String userEntityDTOPhoneNumber;

    @JsonProperty("userTransactionTotalAmount")
    private Integer userTransactionTotalAmount;

    @JsonProperty("userTransactionDate")
    private LocalDateTime userTransactionDate;

    @JsonProperty("userCreatedDate")
    private LocalDateTime userEntityDTOCreatedDate;
}
