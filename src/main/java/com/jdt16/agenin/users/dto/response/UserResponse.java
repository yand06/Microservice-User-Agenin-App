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
public class UserResponse {
    @JsonProperty("userId")
    private UUID userEntityDTOId;

    @JsonProperty("userFullName")
    private String userEntityDTOFullName;

    @JsonProperty("userEmail")
    private String userEntityDTOEmail;

    @JsonProperty("userPhoneNumber")
    private String userEntityDTOPhoneNumber;

    @JsonProperty("roleName")
    private String userEntityDTORoleName;

    @JsonProperty("userCreatedDate")
    private LocalDateTime userEntityDTOCreatedDate;
}
