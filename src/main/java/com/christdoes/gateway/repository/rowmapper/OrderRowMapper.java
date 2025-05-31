package com.christdoes.gateway.repository.rowmapper;

import com.christdoes.gateway.domain.Order;
import com.christdoes.gateway.domain.enumeration.OrderStatus;
import io.r2dbc.spi.Row;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Order}, with proper type conversions.
 */
@Service
public class OrderRowMapper implements BiFunction<Row, String, Order> {

    private final ColumnConverter converter;

    public OrderRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Order} stored in the database.
     */
    @Override
    public Order apply(Row row, String prefix) {
        Order entity = new Order();
        entity.setId(converter.fromRow(row, prefix + "_id", UUID.class));
        entity.setOrderNumber(converter.fromRow(row, prefix + "_order_number", String.class));
        entity.setAmount(converter.fromRow(row, prefix + "_amount", BigDecimal.class));
        entity.setStatus(converter.fromRow(row, prefix + "_status", OrderStatus.class));
        entity.setCreatedAt(converter.fromRow(row, prefix + "_created_at", Instant.class));
        entity.setUserProfileId(converter.fromRow(row, prefix + "_user_profile_id", UUID.class));
        return entity;
    }
}
