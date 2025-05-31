package com.christdoes.gateway.service.mapper;

import com.christdoes.gateway.domain.UserProfile;
import com.christdoes.gateway.service.dto.UserProfileDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link UserProfile} and its DTO {@link UserProfileDTO}.
 */
@Mapper(componentModel = "spring")
public interface UserProfileMapper extends EntityMapper<UserProfileDTO, UserProfile> {}
