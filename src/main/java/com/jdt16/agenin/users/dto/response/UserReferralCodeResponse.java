package com.jdt16.agenin.users.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class UserReferralCodeResponse {

    @JsonProperty("userReferralId")
    private UUID userReferralEntityDTOId;

    @JsonProperty("userReferralCode")
    private String userReferralEntityDTOCode;

    @JsonProperty("userReferralCreatedAt")
    private LocalDateTime userReferralEntityDTOCreatedAt;

}
