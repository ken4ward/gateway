package com.christdoes.gateway.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.christdoes.gateway.web.rest.TestUtil;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(OrderDTO.class);
        OrderDTO orderDTO1 = new OrderDTO();
        orderDTO1.setId(UUID.randomUUID());
        OrderDTO orderDTO2 = new OrderDTO();
        assertThat(orderDTO1).isNotEqualTo(orderDTO2);
        orderDTO2.setId(orderDTO1.getId());
        assertThat(orderDTO1).isEqualTo(orderDTO2);
        orderDTO2.setId(UUID.randomUUID());
        assertThat(orderDTO1).isNotEqualTo(orderDTO2);
        orderDTO1.setId(null);
        assertThat(orderDTO1).isNotEqualTo(orderDTO2);
    }
}
