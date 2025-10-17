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
    public ResponseEntity<RestApiResponse<?>> createUser(@Valid @RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(userService.saveUser(userRequest));
    }

    @PostMapping(RestApiPathUtility.API_PATH_MODULE_REFERRAL_CODE)
    public ResponseEntity<RestApiResponse<?>> generateReferralCode(@RequestHeader("X-USER-ID") UUID userId) {
        return ResponseEntity.ok(userService.generateReferralCode(userId));
    }

    @PostMapping(RestApiPathUtility.API_PATH_MODULE_LOGIN)
    public ResponseEntity<RestApiResponse<?>> login(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        return ResponseEntity.ok(userService.login(userLoginRequest));
    }

    @GetMapping(RestApiPathUtility.API_PATH_MODULE_PROFILE)
    public ResponseEntity<RestApiResponse<?>> getUserProfile(@RequestHeader("X-USER-ID") UUID userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @GetMapping(RestApiPathUtility.API_PATH_MODULE_DOWNLINE)
    public ResponseEntity<RestApiResponse<?>> getUserDownline(@RequestHeader("X-REFERENCE-USER-ID") UUID referenceUserId) {
        return ResponseEntity.ok(userService.getUserDownline(referenceUserId));
    }

    @GetMapping(RestApiPathUtility.API_PATH_MODULE_REFERRAL_CODE)
    public ResponseEntity<RestApiResponse<?>> getReferralCode(@RequestHeader("X-USER-ID") UUID userId) {
        return ResponseEntity.ok(userService.getReferralCode(userId));
    }
}
