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

import java.util.List;
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

        RestApiResponse<UserResponse> userResponseRestApiResponse = RestApiResponse.<UserResponse>builder()
                .restAPIResponseCode(HttpStatus.CREATED.value())
                .restAPIResponseMessage("User successfully registered")
                .restAPIResponseResults(userResponse)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(userResponseRestApiResponse);

    }

    @PostMapping(RestApiPathUtility.API_PATH_MODULE_REFERRAL_CODE + RestApiPathUtility.API_PATH_BY_ID)
    public ResponseEntity<RestApiResponse<?>> generateReferralCode(@PathVariable UUID id) {
        UserReferralCodeResponse userReferralCodeResponse = userService.generateReferralCode(id);

        RestApiResponse<UserReferralCodeResponse> userReferralCodeResponseRestApiResponse = RestApiResponse.<UserReferralCodeResponse>builder()
                .restAPIResponseCode(HttpStatus.OK.value())
                .restAPIResponseMessage("Referral code successful")
                .restAPIResponseResults(userReferralCodeResponse)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(userReferralCodeResponseRestApiResponse);
    }

    @PostMapping(RestApiPathUtility.API_PATH_MODULE_LOGIN)
    public ResponseEntity<RestApiResponse<UserLoginResponse>> login(
            @Valid @RequestBody UserLoginRequest userLoginRequest
    ) {
        UserLoginResponse userLoginResponse = userService.login(userLoginRequest);

        RestApiResponse<UserLoginResponse> apiResponse = new RestApiResponse<>();
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

    @GetMapping(RestApiPathUtility.API_PATH_MODULE_DOWNLINE + RestApiPathUtility.API_PATH_BY_PARENT_ID)
    public ResponseEntity<RestApiResponse<List<UsersDownlineResponse>>> getUserDownline(
            @PathVariable("reference-user-id") UUID referenceUserId) {

        List<UsersDownlineResponse> userDownline = userService.getUserDownline(referenceUserId);

        RestApiResponse<List<UsersDownlineResponse>> userDownlineResponseRestApiResponse = RestApiResponse.<List<UsersDownlineResponse>>builder()
                .restAPIResponseCode(HttpStatus.OK.value())
                .restAPIResponseMessage("Get user downline success")
                .restAPIResponseResults(userDownline)
                .build();

        return ResponseEntity.ok(userDownlineResponseRestApiResponse);
    }
}
