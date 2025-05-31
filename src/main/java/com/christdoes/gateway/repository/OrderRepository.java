package com.christdoes.gateway.repository;

import com.christdoes.gateway.domain.Order;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Order entity.
 */
@SuppressWarnings("unused")
@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, UUID>, OrderRepositoryInternal {
    Flux<Order> findAllBy(Pageable pageable);

    @Override
    Mono<Order> findOneWithEagerRelationships(UUID id);

    @Override
    Flux<Order> findAllWithEagerRelationships();

    @Override
    Flux<Order> findAllWithEagerRelationships(Pageable page);

    @Query("SELECT * FROM jhi_order entity WHERE entity.user_profile_id = :id")
    Flux<Order> findByUserProfile(UUID id);

    @Query("SELECT * FROM jhi_order entity WHERE entity.user_profile_id IS NULL")
    Flux<Order> findAllWhereUserProfileIsNull();

    @Override
    <S extends Order> Mono<S> save(S entity);

    @Override
    Flux<Order> findAll();

    @Override
    Mono<Order> findById(UUID id);

    @Override
    Mono<Void> deleteById(UUID id);
}

interface OrderRepositoryInternal {
    <S extends Order> Mono<S> save(S entity);

    Flux<Order> findAllBy(Pageable pageable);

    Flux<Order> findAll();

    Mono<Order> findById(UUID id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Order> findAllBy(Pageable pageable, Criteria criteria);

    Mono<Order> findOneWithEagerRelationships(UUID id);

    Flux<Order> findAllWithEagerRelationships();

    Flux<Order> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(UUID id);
}
