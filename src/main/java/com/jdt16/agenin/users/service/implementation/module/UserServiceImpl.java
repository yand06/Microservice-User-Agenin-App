package com.jdt16.agenin.users.service.implementation.module;

import com.jdt16.agenin.users.components.generator.ReferralCodeGenerator;
import com.jdt16.agenin.users.components.handler.UserAuthJWT;
import com.jdt16.agenin.users.dto.entity.UserEntityDTO;
import com.jdt16.agenin.users.dto.entity.UserReferralCodeEntityDTO;
import com.jdt16.agenin.users.dto.entity.UserRoleEntityDTO;
import com.jdt16.agenin.users.dto.entity.UsersReferralEntityDTO;
import com.jdt16.agenin.users.dto.exception.CoreThrowHandlerException;
import com.jdt16.agenin.users.dto.request.UserLoginRequest;
import com.jdt16.agenin.users.dto.request.UserRequest;
import com.jdt16.agenin.users.dto.response.UserLoginResponse;
import com.jdt16.agenin.users.dto.response.UserProfileResponse;
import com.jdt16.agenin.users.dto.response.UserReferralCodeResponse;
import com.jdt16.agenin.users.dto.response.UserResponse;
import com.jdt16.agenin.users.model.repositories.MUserRepositories;
import com.jdt16.agenin.users.model.repositories.MUserRoleRepositories;
import com.jdt16.agenin.users.model.repositories.UserReferralCodeRepositories;
import com.jdt16.agenin.users.model.repositories.UsersReferralRepositories;
import com.jdt16.agenin.users.service.interfacing.module.UserService;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final MUserRepositories userRepositories;
    private final UserReferralCodeRepositories userReferralCodeRepositories;
    private final MUserRoleRepositories userRoleRepositories;
    private final UsersReferralRepositories usersReferralRepositories;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final ReferralCodeGenerator referralCodeGenerator = new ReferralCodeGenerator();
    private final UserAuthJWT userAuthJWT;
    private final UserEntityDTO userEntityDTO = new UserEntityDTO();
    private UserReferralCodeEntityDTO userReferralCodeEntityDTO = new UserReferralCodeEntityDTO();

    @Override
    public UserResponse saveUser(UserRequest userRequest) {
        userRepositories.findByUserEntityDTOEmailIgnoreCase(userRequest.getUserEntityDTOEmail())
                .ifPresent(u -> {
                    throw new CoreThrowHandlerException("Pengguna sudah ada dengan alamat email: " + userRequest.getUserEntityDTOEmail());
                });
        userRepositories.findByUserEntityDTOPhoneNumber(userRequest.getUserEntityDTOPhoneNumber())
                .ifPresent(u -> {
                    throw new CoreThrowHandlerException("Pengguna sudah ada dengan nomor telepon: " + userRequest.getUserEntityDTOPhoneNumber());
                });

        referralCodeValidation(userRequest);
        UserRoleEntityDTO userRoleEntityDTO = findRoleForRegistration(userRequest.getUserEntityDTOReferralCode());

        UserEntityDTO newUser = new UserEntityDTO();
        newUser.setUserEntityDTOId(UUID.randomUUID());
        newUser.setUserEntityDTOFullName(userRequest.getUserEntityDTOFullName());
        newUser.setUserEntityDTOEmail(userRequest.getUserEntityDTOEmail());
        newUser.setUserEntityDTOPhoneNumber(userRequest.getUserEntityDTOPhoneNumber());
        newUser.setUserEntityDTOPassword(passwordEncoder.encode(userRequest.getUserEntityDTOPassword()));
        newUser.setUserEntityDTORoleId(userRoleEntityDTO.getUserRoleEntityDTOId());
        newUser.setUserEntityDTORoleName(userRoleEntityDTO.getUserRoleEntityDTOName());
        newUser.setUserEntityDTOCreatedDate(LocalDateTime.now());
        newUser.setUserEntityDTOUpdatedDate(LocalDateTime.now());

        userRepositories.save(newUser);

        userEntityDTO.setUserEntityDTOId(newUser.getUserEntityDTOId());
        userEntityDTO.setUserEntityDTOFullName(newUser.getUserEntityDTOFullName());
        userEntityDTO.setUserEntityDTOEmail(newUser.getUserEntityDTOEmail());
        userEntityDTO.setUserEntityDTOPhoneNumber(newUser.getUserEntityDTOPhoneNumber());

        saveUsersReferral();

        return getUserResponse(newUser);
    }


    private UserRoleEntityDTO findRoleForRegistration(@Nullable String referralCode) {
        final boolean hasReferral = referralCode != null && !referralCode.isBlank();
        final String roleName = hasReferral ? "AGEN BAWAHAN" : "AGEN TUNGGAL";
        return userRoleRepositories.findByUserRoleEntityDTONameIgnoreCase(roleName)
                .orElseThrow(() -> new CoreThrowHandlerException(
                        "Role '%s' belum disiapkan, mohon seed data M_ROLE terlebih dahulu".formatted(roleName)));
    }

    private void referralCodeValidation(UserRequest userRequest) {
        String referralCode = trimToNull(userRequest.getUserEntityDTOReferralCode());
        if (referralCode != null) {
            if (!userReferralCodeRepositories.existsByUserReferralEntityDTOCodeIgnoreCase(referralCode)) {
                throw new CoreThrowHandlerException("Kode referal tidak ditemukan");
            }
            userReferralCodeEntityDTO = userReferralCodeRepositories
                    .findByUserReferralEntityDTOCodeIgnoreCase(referralCode)
                    .orElseThrow(() -> new CoreThrowHandlerException("Kode referal tidak ditemukan"));
        } else {
            userReferralCodeEntityDTO = null;
        }
    }


    private void saveUsersReferral() {
        if (userReferralCodeEntityDTO == null) {
            return;
        }
        UserEntityDTO referenceUser = userRepositories.findById(userReferralCodeEntityDTO.getUserReferralEntityDTOUserId())
                .orElseThrow(() -> new CoreThrowHandlerException("Pemilik kode referal tidak ditemukan"));

        UsersReferralEntityDTO usersReferralEntityDTO = new UsersReferralEntityDTO();
        usersReferralEntityDTO.setUsersReferralEntityDTOId(UUID.randomUUID());
        usersReferralEntityDTO.setUsersReferralEntityDTOInviteeUserId(userEntityDTO.getUserEntityDTOId());
        usersReferralEntityDTO.setUsersReferralEntityDTOInviteeUserFullName(userEntityDTO.getUserEntityDTOFullName());
        usersReferralEntityDTO.setUsersReferralEntityDTOInviteeUserPhoneNumber(userEntityDTO.getUserEntityDTOPhoneNumber());
        usersReferralEntityDTO.setUsersReferralEntityDTOInviteeUserEmail(userEntityDTO.getUserEntityDTOEmail());
        usersReferralEntityDTO.setUsersReferralEntityDTOReferenceUserId(referenceUser.getUserEntityDTOId());
        usersReferralEntityDTO.setUsersReferralEntityDTOReferenceUserFullName(referenceUser.getUserEntityDTOFullName());
        usersReferralEntityDTO.setUsersReferralEntityDTOReferenceUserPhoneNumber(referenceUser.getUserEntityDTOPhoneNumber());
        usersReferralEntityDTO.setUsersReferralEntityDTOReferenceUserEmail(referenceUser.getUserEntityDTOEmail());
        usersReferralEntityDTO.setUsersReferralEntityDTOReferralCode(userReferralCodeEntityDTO.getUserReferralEntityDTOCode());
        usersReferralRepositories.save(usersReferralEntityDTO);
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
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
        return null;
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


    private boolean isEmailLike(String input) {
        return input != null && input.contains("@");
    }

    private static UserResponse getUserResponse(UserEntityDTO userEntityDTO) {
        UserResponse userResponse = new UserResponse();
        userResponse.setUserEntityDTOId(userEntityDTO.getUserEntityDTOId());
        userResponse.setUserEntityDTOFullName(userEntityDTO.getUserEntityDTOFullName());
        userResponse.setUserEntityDTOEmail(userEntityDTO.getUserEntityDTOEmail());
        userResponse.setUserEntityDTOPhoneNumber(userEntityDTO.getUserEntityDTOPhoneNumber());
        userResponse.setUserEntityDTORoleName(userEntityDTO.getUserEntityDTORoleName());
        userResponse.setUserEntityDTOCreatedDate(userEntityDTO.getUserEntityDTOCreatedDate());
        return userResponse;
    }

}


