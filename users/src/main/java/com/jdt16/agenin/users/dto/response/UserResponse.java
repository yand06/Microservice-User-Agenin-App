package com.jdt16.agenin.users.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UserResponse {
    @JsonProperty("userId")
    private UUID userEntityDTOId;

    @JsonProperty("userFullName")
    private String userEntityDTOFullName;

    @JsonProperty("userEmail")
    private String userEntityDTOEmail;

    @JsonProperty("userPhoneNumber")
    private String userEntityDTOPhoneNumber;

    @JsonProperty("userIsAdmin")
    private Boolean userEntityDTOIsAdmin;

    @JsonProperty("userIsParent")
    private Boolean userEntityDTOIsParent;

    @JsonProperty("userCreatedDate")
    private LocalDateTime userEntityDTOCreatedDate;

    @JsonProperty("userUpdatedDate")
    private LocalDateTime userEntityDTOUpdatedDate;

    @JsonProperty("userStatus")
    private Boolean userEntityDTOStatus;
}
