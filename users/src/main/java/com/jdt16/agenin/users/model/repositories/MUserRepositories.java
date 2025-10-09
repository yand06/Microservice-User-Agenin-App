package com.jdt16.agenin.users.model.repositories;

import com.jdt16.agenin.users.dto.entity.UserEntityDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MUserRepositories extends JpaRepository<UserEntityDTO, UUID> {

    Optional<UserEntityDTO> findByUserEntityDTOId(UUID userId);

    Optional<UserEntityDTO> findByUserEntityDTOEmail(String userEntityDTOEmail);

}
