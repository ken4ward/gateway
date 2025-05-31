package com.christdoes.gateway.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the {@link com.christdoes.gateway.domain.UserProfile} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class UserProfileDTO implements Serializable {

    @NotNull(message = "must not be null")
    private UUID id;

    @NotNull(message = "must not be null")
    @Size(min = 3)
    private String username;

    @NotNull(message = "must not be null")
    @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    private String email;

    private Integer age;

    @NotNull(message = "must not be null")
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserProfileDTO)) {
            return false;
        }

        UserProfileDTO userProfileDTO = (UserProfileDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, userProfileDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "UserProfileDTO{" +
            "id='" + getId() + "'" +
            ", username='" + getUsername() + "'" +
            ", email='" + getEmail() + "'" +
            ", age=" + getAge() +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
