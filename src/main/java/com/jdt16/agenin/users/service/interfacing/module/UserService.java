package com.jdt16.agenin.users.service.interfacing.module;

import com.jdt16.agenin.users.dto.request.UserRequest;
import com.jdt16.agenin.users.dto.response.UserReferralCodeResponse;
import com.jdt16.agenin.users.dto.response.UserResponse;

import java.util.UUID;

public interface UserService {
    UserResponse saveUser(UserRequest userRequest);
    UserReferralCodeResponse generateReferralCode(UUID userID);
}
