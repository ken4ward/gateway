package com.christdoes.gateway.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.christdoes.gateway.domain.Order;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

/**
 * Spring Data Elasticsearch repository for the {@link Order} entity.
 */
public interface OrderSearchRepository extends ReactiveElasticsearchRepository<Order, UUID>, OrderSearchRepositoryInternal {}

interface OrderSearchRepositoryInternal {
    Flux<Order> search(String query, Pageable pageable);

    Flux<Order> search(Query query);
}

class OrderSearchRepositoryInternalImpl implements OrderSearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    OrderSearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<Order> search(String query, Pageable pageable) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        nativeQuery.setPageable(pageable);
        return search(nativeQuery);
    }

    @Override
    public Flux<Order> search(Query query) {
        return reactiveElasticsearchTemplate.search(query, Order.class).map(SearchHit::getContent);
    }
}
