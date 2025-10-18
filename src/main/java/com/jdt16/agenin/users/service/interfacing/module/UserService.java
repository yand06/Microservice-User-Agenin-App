package com.jdt16.agenin.users.service.interfacing.module;

import com.jdt16.agenin.users.dto.request.UserAdminUpdateCommissionsRequest;
import com.jdt16.agenin.users.dto.request.UserLoginRequest;
import com.jdt16.agenin.users.dto.request.UserRequest;
import com.jdt16.agenin.users.dto.response.*;

import java.util.UUID;

public interface UserService {
    RestApiResponse<Object> saveUser(UserRequest userRequest);

    RestApiResponse<Object> generateReferralCode(UUID userID);

    RestApiResponse<Object> login(UserLoginRequest userLoginRequest);

    RestApiResponse<Object> getUserProfile(UUID userId);

    RestApiResponse<Object> getUserDownline(UUID referenceUserId);

    RestApiResponse<Object> getReferralCode(UUID userId);

    RestApiResponse<Object> updateCommissions(UUID commissionsId, UserAdminUpdateCommissionsRequest adminUpdateCommissionsRequest);
}