package com.jdt16.agenin.users.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class UserUpdateResponse {
    @JsonProperty("userId")
    private UUID userEntityDTOId;

    @JsonProperty("userFullName")
    private String userEntityDTOFullName;

    @JsonProperty("userEmail")
    private String userEntityDTOEmail;

    @JsonProperty("userPhoneNumber")
    private String userEntityDTOPhoneNumber;

    @JsonProperty("userUpdatedDate")
    private LocalDateTime userEntityDTOUpdatedDate;

    @JsonProperty("userPassword")
    private String userEntityDTOPassword;
}
