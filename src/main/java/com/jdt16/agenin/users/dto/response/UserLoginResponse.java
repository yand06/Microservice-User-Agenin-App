package com.jdt16.agenin.users.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginResponse {
    @JsonProperty("userId")
    private UUID userEntityDTOId;

    @JsonProperty("token")
    private String userLoginResponseToken;

    @JsonProperty("userFullName")
    private String userEntityDTOFullName;

    @JsonProperty("userEmail")
    private String userEntityDTOEmail;

    @JsonProperty("userPhoneNumber")
    private String userEntityDTOPhoneNumber;

    @JsonProperty("roleName")
    private String userEntityDTORoleName;
}
