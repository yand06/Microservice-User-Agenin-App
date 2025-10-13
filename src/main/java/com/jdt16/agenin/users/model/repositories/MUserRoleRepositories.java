package com.jdt16.agenin.users.model.repositories;

import com.jdt16.agenin.users.dto.entity.UserRoleEntityDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MUserRoleRepositories extends JpaRepository<UserRoleEntityDTO, UUID> {
    Optional<UserRoleEntityDTO> findByUserRoleEntityDTONameIgnoreCase(String roleName);
}
