package com.christdoes.gateway.domain;

import com.christdoes.gateway.domain.enumeration.OrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A Order.
 */
@Table("jhi_order")
@JsonIgnoreProperties(value = { "new" })
@org.springframework.data.elasticsearch.annotations.Document(indexName = "order")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Order implements Serializable, Persistable<UUID> {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "must not be null")
    @Id
    @Column("id")
    private UUID id;

    @NotNull(message = "must not be null")
    @Column("order_number")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String orderNumber;

    @NotNull(message = "must not be null")
    @Column("amount")
    private BigDecimal amount;

    @NotNull(message = "must not be null")
    @Column("status")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Keyword)
    private OrderStatus status;

    @NotNull(message = "must not be null")
    @Column("created_at")
    private Instant createdAt;

    @org.springframework.data.annotation.Transient
    private boolean isPersisted;

    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "orders" }, allowSetters = true)
    private UserProfile userProfile;

    @Column("user_profile_id")
    private UUID userProfileId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public Order id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return this.orderNumber;
    }

    public Order orderNumber(String orderNumber) {
        this.setOrderNumber(orderNumber);
        return this;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public Order amount(BigDecimal amount) {
        this.setAmount(amount);
        return this;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount != null ? amount.stripTrailingZeros() : null;
    }

    public OrderStatus getStatus() {
        return this.status;
    }

    public Order status(OrderStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Order createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @org.springframework.data.annotation.Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public Order setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    public UserProfile getUserProfile() {
        return this.userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        this.userProfileId = userProfile != null ? userProfile.getId() : null;
    }

    public Order userProfile(UserProfile userProfile) {
        this.setUserProfile(userProfile);
        return this;
    }

    public UUID getUserProfileId() {
        return this.userProfileId;
    }

    public void setUserProfileId(UUID userProfile) {
        this.userProfileId = userProfile;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Order)) {
            return false;
        }
        return getId() != null && getId().equals(((Order) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Order{" +
            "id=" + getId() +
            ", orderNumber='" + getOrderNumber() + "'" +
            ", amount=" + getAmount() +
            ", status='" + getStatus() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
