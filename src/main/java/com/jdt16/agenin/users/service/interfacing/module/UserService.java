package com.jdt16.agenin.users.service.interfacing.module;

import com.jdt16.agenin.users.dto.request.UserLoginRequest;
import com.jdt16.agenin.users.dto.request.UserRequest;
import com.jdt16.agenin.users.dto.response.*;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse saveUser(UserRequest userRequest);

    UserReferralCodeResponse generateReferralCode(UUID userID);

    UserLoginResponse login(UserLoginRequest userLoginRequest);

    UserProfileResponse getUserProfile(UUID userId);

    List<UserDownlineResponse> getUserDownline(UUID referenceUserId);

}
