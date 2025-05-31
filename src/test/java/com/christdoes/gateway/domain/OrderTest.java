package com.christdoes.gateway.domain;

import static com.christdoes.gateway.domain.OrderTestSamples.*;
import static com.christdoes.gateway.domain.UserProfileTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.christdoes.gateway.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class OrderTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Order.class);
        Order order1 = getOrderSample1();
        Order order2 = new Order();
        assertThat(order1).isNotEqualTo(order2);

        order2.setId(order1.getId());
        assertThat(order1).isEqualTo(order2);

        order2 = getOrderSample2();
        assertThat(order1).isNotEqualTo(order2);
    }

    @Test
    void userProfileTest() {
        Order order = getOrderRandomSampleGenerator();
        UserProfile userProfileBack = getUserProfileRandomSampleGenerator();

        order.setUserProfile(userProfileBack);
        assertThat(order.getUserProfile()).isEqualTo(userProfileBack);

        order.userProfile(null);
        assertThat(order.getUserProfile()).isNull();
    }
}
