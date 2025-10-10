package com.jdt16.agenin.users.service.implementation.module;

import com.jdt16.agenin.users.components.generator.ReferralCodeGenerator;
import com.jdt16.agenin.users.components.handler.UserAuthJWT;
import com.jdt16.agenin.users.dto.entity.UserEntityDTO;
import com.jdt16.agenin.users.dto.entity.UserReferralCodeEntityDTO;
import com.jdt16.agenin.users.dto.request.UserLoginRequest;
import com.jdt16.agenin.users.dto.request.UserRequest;
import com.jdt16.agenin.users.dto.request.UserStatusUpdateRequest;
import com.jdt16.agenin.users.dto.response.UserLoginResponse;
import com.jdt16.agenin.users.dto.response.UserProfileResponse;
import com.jdt16.agenin.users.dto.response.UserReferralCodeResponse;
import com.jdt16.agenin.users.dto.response.UserResponse;
import com.jdt16.agenin.users.model.repositories.MUserRepositories;
import com.jdt16.agenin.users.model.repositories.UserReferralCodeRepositories;
import com.jdt16.agenin.users.service.interfacing.module.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final MUserRepositories userRepositories;
    private final UserReferralCodeRepositories userReferralCodeRepositories;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final ReferralCodeGenerator referralCodeGenerator = new ReferralCodeGenerator();
    private final UserAuthJWT userAuthJWT;
    private final UserEntityDTO userEntityDTO = new UserEntityDTO();

    @Override
    public UserResponse saveUser(UserRequest userRequest) {
        UserEntityDTO userEntityDTO = new UserEntityDTO();
        userEntityDTO.setUserEntityDTOId(UUID.randomUUID());
        userEntityDTO.setUserEntityDTOFullName(userRequest.getUserEntityDTOFullName());
        userEntityDTO.setUserEntityDTOEmail(userRequest.getUserEntityDTOEmail());
        userEntityDTO.setUserEntityDTOPhoneNumber(userRequest.getUserEntityDTOPhoneNumber());
        userEntityDTO.setUserEntityDTOPassword(passwordEncoder.encode(userRequest.getUserEntityDTOPassword()));
        userEntityDTO.setUserEntityDTOIsAdmin(false);
        userEntityDTO.setUserEntityDTOCreatedDate(LocalDateTime.now());
        userEntityDTO.setUserEntityDTOIsParent(true);
        userEntityDTO.setUserEntityDTOUpdatedDate(LocalDateTime.now());
        userEntityDTO.setUserEntityDTOStatus(true);

        userRepositories.save(userEntityDTO);

        return getUserResponse(userEntityDTO);
    }

    @Override
    public UserReferralCodeResponse generateReferralCode(UUID userId) {

        if (!userRepositories.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        if (userReferralCodeRepositories.existsByUserReferralEntityDTOUserId(userId)) {
            throw new IllegalStateException("Pengguna sudah memiliki kode referensi.");
        }

        UserReferralCodeEntityDTO referralCodeEntityDTO = new UserReferralCodeEntityDTO();
        referralCodeEntityDTO.setUserReferralEntityDTOId(UUID.randomUUID());
        referralCodeEntityDTO.setUserReferralEntityDTOUserId(userId);
        referralCodeEntityDTO.setUserReferralEntityDTOCreatedAt(LocalDateTime.now());

        String code = referralCodeGenerator.generateReferralCode();
        if (code == null || code.isEmpty()) {
            throw new IllegalStateException("Referral code generation failed");
        }
        referralCodeEntityDTO.setUserReferralEntityDTOCode(code);

        userReferralCodeRepositories.save(referralCodeEntityDTO);

        return UserReferralCodeResponse.builder()
                .userReferralEntityDTOCode(code)
                .userReferralEntityDTOCreatedAt(LocalDateTime.now())
                .build();
    }


    @Override
    public UserLoginResponse login(UserLoginRequest userLoginRequest) {
        String identifier = userLoginRequest.getUserIdentifier();
        UserLoginResponse userLoginResponse = new UserLoginResponse();

        Optional<UserEntityDTO> userOpt = isEmailLike(identifier)
                ? userRepositories.findByUserEntityDTOEmailIgnoreCase(identifier)
                : userRepositories.findByUserEntityDTOPhoneNumber(identifier);

        if (userOpt.isEmpty()) {
            log.warn("Login failed: user not found for identifier {}", identifier);
            return null;
        }

        UserEntityDTO user = userOpt.get();

        boolean ok = passwordEncoder.matches(
                userLoginRequest.getUserPassword(),
                user.getUserEntityDTOPassword()
        );

        String accessToken = userAuthJWT.generateAuthToken(user, 3600);
        userLoginResponse.setUserLoginResponseToken(accessToken);

        if (!ok) {
            log.warn("Login failed: invalid password for user {}", identifier);
            return null;
        }

        userLoginResponse.setUserEntityDTOId(user.getUserEntityDTOId());
        userLoginResponse.setUserEntityDTOFullName(user.getUserEntityDTOFullName());
        userLoginResponse.setUserEntityDTOEmail(user.getUserEntityDTOEmail());
        userLoginResponse.setUserEntityDTOPhoneNumber(user.getUserEntityDTOPhoneNumber());
        userLoginResponse.setUserEntityDTOIsAdmin(Boolean.TRUE.equals(user.getUserEntityDTOIsAdmin()));

        return userLoginResponse;
    }

    @Override
    public UserProfileResponse getUserProfile(UUID userId) {
        // Cari user berdasarkan ID
        UserEntityDTO user = userRepositories.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Misal kita ambil total transaksi user dari tabel transaksi (sementara static dulu)
        int totalTransactionAmount = 200000; // TODO: ganti dengan query ke service transaksi

        // Buat response
        UserProfileResponse response = new UserProfileResponse();
        response.setUserEntityDTOId(user.getUserEntityDTOId());
        response.setUserEntityDTOFullName(user.getUserEntityDTOFullName());
        response.setUserEntityDTOEmail(user.getUserEntityDTOEmail());
        response.setUserEntityDTOPhoneNumber(user.getUserEntityDTOPhoneNumber());
        response.setUserTransactionTotalAmount(totalTransactionAmount);
        response.setUserTransactionDate(LocalDateTime.now()); // sementara dummy
        response.setUserEntityDTOCreatedDate(user.getUserEntityDTOCreatedDate());

        return response;
    }

    @Override
    public UserResponse updateUserStatus(UserStatusUpdateRequest userStatusUpdateRequest) {
        UUID userId = userStatusUpdateRequest.getUserId();

        UserEntityDTO user = userRepositories.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));

        user.setUserEntityDTOStatus(userStatusUpdateRequest.getUserStatus());
        user.setUserEntityDTOUpdatedDate(LocalDateTime.now());

        UserEntityDTO saved = userRepositories.save(user);

        return getUserResponse(saved);
    }



    private boolean isEmailLike(String input) {
        return input != null && input.contains("@");
    }

    private static UserResponse getUserResponse(UserEntityDTO userEntityDTO) {
        UserResponse userResponse = new UserResponse();
        userResponse.setUserEntityDTOId(userEntityDTO.getUserEntityDTOId());
        userResponse.setUserEntityDTOFullName(userEntityDTO.getUserEntityDTOFullName());
        userResponse.setUserEntityDTOEmail(userEntityDTO.getUserEntityDTOEmail());
        userResponse.setUserEntityDTOPhoneNumber(userEntityDTO.getUserEntityDTOPhoneNumber());
//        userResponse.setUserEntityDTOIsAdmin(userEntityDTO.getUserEntityDTOIsAdmin());
//        userResponse.setUserEntityDTOIsParent(userEntityDTO.getUserEntityDTOIsParent());
//        userResponse.setUserEntityDTOStatus(userEntityDTO.getUserEntityDTOStatus());
        userResponse.setUserEntityDTOCreatedDate(userEntityDTO.getUserEntityDTOCreatedDate());
//        userResponse.setUserEntityDTOUpdatedDate(userEntityDTO.getUserEntityDTOUpdatedDate());
        return userResponse;
    }

}


