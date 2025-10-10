package com.jdt16.agenin.users.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class UserStatusUpdateRequest {

  @NotBlank(message = "userId is required")
  @JsonProperty("userId")
  private UUID userId;

  @NotBlank(message = "userStatus is required")
  @JsonProperty("userStatus")
  private Boolean userStatus;
}
