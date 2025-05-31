package com.christdoes.gateway.service.mapper;

import com.christdoes.gateway.domain.Order;
import com.christdoes.gateway.domain.UserProfile;
import com.christdoes.gateway.service.dto.OrderDTO;
import com.christdoes.gateway.service.dto.UserProfileDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Order} and its DTO {@link OrderDTO}.
 */
@Mapper(componentModel = "spring")
public interface OrderMapper extends EntityMapper<OrderDTO, Order> {
    @Mapping(target = "userProfile", source = "userProfile", qualifiedByName = "userProfileUsername")
    OrderDTO toDto(Order s);

    @Named("userProfileUsername")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    UserProfileDTO toDtoUserProfileUsername(UserProfile userProfile);
}
