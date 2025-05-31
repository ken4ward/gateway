package com.christdoes.gateway.repository;

import com.christdoes.gateway.domain.UserProfile;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the UserProfile entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UserProfileRepository extends ReactiveCrudRepository<UserProfile, UUID>, UserProfileRepositoryInternal {
    Flux<UserProfile> findAllBy(Pageable pageable);

    @Override
    <S extends UserProfile> Mono<S> save(S entity);

    @Override
    Flux<UserProfile> findAll();

    @Override
    Mono<UserProfile> findById(UUID id);

    @Override
    Mono<Void> deleteById(UUID id);
}

interface UserProfileRepositoryInternal {
    <S extends UserProfile> Mono<S> save(S entity);

    Flux<UserProfile> findAllBy(Pageable pageable);

    Flux<UserProfile> findAll();

    Mono<UserProfile> findById(UUID id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<UserProfile> findAllBy(Pageable pageable, Criteria criteria);
}
