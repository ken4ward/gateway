package com.christdoes.gateway.domain;

import static com.christdoes.gateway.domain.OrderTestSamples.*;
import static com.christdoes.gateway.domain.UserProfileTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.christdoes.gateway.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UserProfileTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(UserProfile.class);
        UserProfile userProfile1 = getUserProfileSample1();
        UserProfile userProfile2 = new UserProfile();
        assertThat(userProfile1).isNotEqualTo(userProfile2);

        userProfile2.setId(userProfile1.getId());
        assertThat(userProfile1).isEqualTo(userProfile2);

        userProfile2 = getUserProfileSample2();
        assertThat(userProfile1).isNotEqualTo(userProfile2);
    }

    @Test
    void ordersTest() {
        UserProfile userProfile = getUserProfileRandomSampleGenerator();
        Order orderBack = getOrderRandomSampleGenerator();

        userProfile.addOrders(orderBack);
        assertThat(userProfile.getOrders()).containsOnly(orderBack);
        assertThat(orderBack.getUserProfile()).isEqualTo(userProfile);

        userProfile.removeOrders(orderBack);
        assertThat(userProfile.getOrders()).doesNotContain(orderBack);
        assertThat(orderBack.getUserProfile()).isNull();

        userProfile.orders(new HashSet<>(Set.of(orderBack)));
        assertThat(userProfile.getOrders()).containsOnly(orderBack);
        assertThat(orderBack.getUserProfile()).isEqualTo(userProfile);

        userProfile.setOrders(new HashSet<>());
        assertThat(userProfile.getOrders()).doesNotContain(orderBack);
        assertThat(orderBack.getUserProfile()).isNull();
    }
}
