package com.christdoes.gateway.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class UserProfileTestSamples {

    private static final Random random = new Random();
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static UserProfile getUserProfileSample1() {
        return new UserProfile().id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa")).username("username1").email("email1").age(1);
    }

    public static UserProfile getUserProfileSample2() {
        return new UserProfile().id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367")).username("username2").email("email2").age(2);
    }

    public static UserProfile getUserProfileRandomSampleGenerator() {
        return new UserProfile()
            .id(UUID.randomUUID())
            .username(UUID.randomUUID().toString())
            .email(UUID.randomUUID().toString())
            .age(intCount.incrementAndGet());
    }
}
