package com.jdt16.agenin.users.service.interfacing.module;

import com.jdt16.agenin.users.dto.request.UserLoginRequest;
import com.jdt16.agenin.users.dto.request.UserRequest;
import com.jdt16.agenin.users.dto.request.UserStatusUpdateRequest;
import com.jdt16.agenin.users.dto.response.UserLoginResponse;
import com.jdt16.agenin.users.dto.response.UserProfileResponse;
import com.jdt16.agenin.users.dto.response.UserReferralCodeResponse;
import com.jdt16.agenin.users.dto.response.UserResponse;

import java.util.UUID;

public interface UserService {
    UserResponse saveUser(UserRequest userRequest);
    UserReferralCodeResponse generateReferralCode(UUID userID);
    UserLoginResponse login (UserLoginRequest userLoginRequest);
    UserProfileResponse getUserProfile (UUID userId);
    UserResponse updateUserStatus(UserStatusUpdateRequest userStatusUpdateRequest);
}
