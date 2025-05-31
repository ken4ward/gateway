package com.christdoes.gateway.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.christdoes.gateway.domain.UserProfile;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

/**
 * Spring Data Elasticsearch repository for the {@link UserProfile} entity.
 */
public interface UserProfileSearchRepository
    extends ReactiveElasticsearchRepository<UserProfile, UUID>, UserProfileSearchRepositoryInternal {}

interface UserProfileSearchRepositoryInternal {
    Flux<UserProfile> search(String query, Pageable pageable);

    Flux<UserProfile> search(Query query);
}

class UserProfileSearchRepositoryInternalImpl implements UserProfileSearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    UserProfileSearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<UserProfile> search(String query, Pageable pageable) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        nativeQuery.setPageable(pageable);
        return search(nativeQuery);
    }

    @Override
    public Flux<UserProfile> search(Query query) {
        return reactiveElasticsearchTemplate.search(query, UserProfile.class).map(SearchHit::getContent);
    }
}
