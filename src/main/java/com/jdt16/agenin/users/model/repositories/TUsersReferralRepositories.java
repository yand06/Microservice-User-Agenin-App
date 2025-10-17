package com.jdt16.agenin.users.model.repositories;

import com.jdt16.agenin.users.dto.entity.UsersReferralEntityDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TUsersReferralRepositories extends JpaRepository<UsersReferralEntityDTO, UUID> {
    List<UsersReferralEntityDTO> findAllByUsersReferralEntityDTOReferenceUserId(UUID referenceUserId);
}
