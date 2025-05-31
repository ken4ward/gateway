package com.christdoes.gateway.repository;

import com.christdoes.gateway.domain.Order;
import com.christdoes.gateway.repository.rowmapper.OrderRowMapper;
import com.christdoes.gateway.repository.rowmapper.UserProfileRowMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Comparison;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC custom repository implementation for the Order entity.
 */
@SuppressWarnings("unused")
class OrderRepositoryInternalImpl extends SimpleR2dbcRepository<Order, UUID> implements OrderRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final UserProfileRowMapper userprofileMapper;
    private final OrderRowMapper orderMapper;

    private static final Table entityTable = Table.aliased("jhi_order", EntityManager.ENTITY_ALIAS);
    private static final Table userProfileTable = Table.aliased("user_profile", "userProfile");

    public OrderRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        UserProfileRowMapper userprofileMapper,
        OrderRowMapper orderMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Order.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.userprofileMapper = userprofileMapper;
        this.orderMapper = orderMapper;
    }

    @Override
    public Flux<Order> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Order> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = OrderSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(UserProfileSqlHelper.getColumns(userProfileTable, "userProfile"));
        SelectFromAndJoinCondition selectFrom = Select.builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(userProfileTable)
            .on(Column.create("user_profile_id", entityTable))
            .equals(Column.create("id", userProfileTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Order.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Order> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Order> findById(UUID id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(StringUtils.wrap(id.toString(), "'")));
        return createQuery(null, whereClause).one();
    }

    @Override
    public Mono<Order> findOneWithEagerRelationships(UUID id) {
        return findById(id);
    }

    @Override
    public Flux<Order> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    public Flux<Order> findAllWithEagerRelationships(Pageable page) {
        return findAllBy(page);
    }

    private Order process(Row row, RowMetadata metadata) {
        Order entity = orderMapper.apply(row, "e");
        entity.setUserProfile(userprofileMapper.apply(row, "userProfile"));
        return entity;
    }

    @Override
    public <S extends Order> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
