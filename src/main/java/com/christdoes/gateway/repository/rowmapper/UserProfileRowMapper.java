package com.christdoes.gateway.repository.rowmapper;

import com.christdoes.gateway.domain.UserProfile;
import io.r2dbc.spi.Row;
import java.time.Instant;
import java.util.UUID;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link UserProfile}, with proper type conversions.
 */
@Service
public class UserProfileRowMapper implements BiFunction<Row, String, UserProfile> {

    private final ColumnConverter converter;

    public UserProfileRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link UserProfile} stored in the database.
     */
    @Override
    public UserProfile apply(Row row, String prefix) {
        UserProfile entity = new UserProfile();
        entity.setId(converter.fromRow(row, prefix + "_id", UUID.class));
        entity.setUsername(converter.fromRow(row, prefix + "_username", String.class));
        entity.setEmail(converter.fromRow(row, prefix + "_email", String.class));
        entity.setAge(converter.fromRow(row, prefix + "_age", Integer.class));
        entity.setCreatedAt(converter.fromRow(row, prefix + "_created_at", Instant.class));
        return entity;
    }
}
