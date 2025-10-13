package com.jdt16.agenin.users.controller.module;

import com.jdt16.agenin.users.dto.request.UserLoginRequest;
import com.jdt16.agenin.users.dto.request.UserRequest;
import com.jdt16.agenin.users.dto.response.*;
import com.jdt16.agenin.users.service.interfacing.module.UserService;
import com.jdt16.agenin.users.utility.RestApiPathUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(RestApiPathUtility.API_PATH + RestApiPathUtility.API_VERSION + RestApiPathUtility.API_PATH_USER)
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping(RestApiPathUtility.API_PATH_CREATE)
    public ResponseEntity<RestApiResponse<?>> createUser(
            @Valid @RequestBody UserRequest userRequest
    ) {
        UserResponse userResponse = userService.saveUser(userRequest);

        RestApiResponse<UserResponse> apiResponse = new RestApiResponse<>();
        apiResponse.setRestAPIResponseCode(HttpStatus.CREATED.value());
        apiResponse.setRestAPIResponseMessage("User successfully registered");
        apiResponse.setRestAPIResponseResults(userResponse);

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PostMapping(RestApiPathUtility.API_PATH_MODULE_REFERRAL_CODE + RestApiPathUtility.API_PATH_BY_ID)
    public ResponseEntity<RestApiResponse<?>> generateReferralCode(@PathVariable UUID id) {
        UserReferralCodeResponse userReferralCodeResponse = userService.generateReferralCode(id);

        RestApiResponse<UserReferralCodeResponse> apiResponse = new RestApiResponse<>();
        apiResponse.setRestAPIResponseCode((HttpStatus.OK.value()));
        apiResponse.setRestAPIResponseMessage("Referral code successful");
        apiResponse.setRestAPIResponseResults(userReferralCodeResponse);

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PostMapping(RestApiPathUtility.API_PATH_MODULE_LOGIN)
    public ResponseEntity<RestApiResponse<?>> login(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        UserLoginResponse userLoginResponse = userService.login(userLoginRequest);

        RestApiResponse<UserLoginResponse> apiResponse = new RestApiResponse<>();

        if (userLoginResponse == null) {
            apiResponse.setRestAPIResponseCode(HttpStatus.UNAUTHORIZED.value());
            apiResponse.setRestAPIResponseMessage("Invalid email/phone number or password or user not found");
            apiResponse.setRestAPIResponseResults(null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
        }

        apiResponse.setRestAPIResponseCode(HttpStatus.OK.value());
        apiResponse.setRestAPIResponseMessage("User login successful");
        apiResponse.setRestAPIResponseResults(userLoginResponse);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping(RestApiPathUtility.API_PATH_MODULE_PROFILE + RestApiPathUtility.API_PATH_BY_ID)
    public ResponseEntity<RestApiResponse<UserProfileResponse>> getUserProfile(
            @PathVariable UUID id) {

        UserProfileResponse userProfile = userService.getUserProfile(id);

        RestApiResponse<UserProfileResponse> response = new RestApiResponse<>();
        response.setRestAPIResponseCode(200);
        response.setRestAPIResponseMessage("Get user profile success");
        response.setRestAPIResponseResults(userProfile);

        return ResponseEntity.ok(response);
    }


}
