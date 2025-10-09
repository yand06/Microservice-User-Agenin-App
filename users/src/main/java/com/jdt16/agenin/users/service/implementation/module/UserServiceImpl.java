package com.jdt16.agenin.users.service.implementation.module;

import com.jdt16.agenin.users.components.generator.ReferralCodeGenerator;
import com.jdt16.agenin.users.dto.entity.UserEntityDTO;
import com.jdt16.agenin.users.dto.entity.UserReferralCodeEntityDTO;
import com.jdt16.agenin.users.dto.request.UserRequest;
import com.jdt16.agenin.users.dto.response.UserReferralCodeResponse;
import com.jdt16.agenin.users.dto.response.UserResponse;
import com.jdt16.agenin.users.model.repositories.MUserRepositories;
import com.jdt16.agenin.users.model.repositories.UserReferralCodeRepositories;
import com.jdt16.agenin.users.service.interfacing.module.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final ReferralCodeGenerator referralCodeGenerator = new ReferralCodeGenerator();

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
        UserReferralCodeEntityDTO userReferralCodeEntityDTO = new UserReferralCodeEntityDTO();
        userReferralCodeEntityDTO.setUserReferralEntityDTOId(UUID.randomUUID());
        userReferralCodeEntityDTO.setUserReferralEntityDTOUserId(userId);
        userReferralCodeEntityDTO.setUserReferralEntityDTOCode(referralCodeGenerator.generateReferralCode());

        userReferralCodeRepositories.save(userReferralCodeEntityDTO);
        UserReferralCodeResponse userReferralCodeResponse = new UserReferralCodeResponse();
        userReferralCodeResponse.setUserReferralEntityDTOCode(userReferralCodeEntityDTO.getUserReferralEntityDTOCode());
        return userReferralCodeResponse;
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
