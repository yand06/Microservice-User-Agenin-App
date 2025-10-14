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
public class UsersDownlineResponse {

    @JsonProperty("inviteeUserId")
    private UUID usersReferralEntityDTOInviteeUserId;

    @JsonProperty("inviteeUserFullName")
    private String usersReferralEntityDTOInviteeUserFullName;

    @JsonProperty("inviteeUserPhoneNumber")
    private String usersReferralEntityDTOInviteeUserPhoneNumber;

    @JsonProperty("inviteeUserEmail")
    private String usersReferralEntityDTOInviteeUserEmail;

    @JsonProperty("inviteeCommissionValue")
    private BigDecimal usersReferralEntityDTOInviteeCommissionValue;
}