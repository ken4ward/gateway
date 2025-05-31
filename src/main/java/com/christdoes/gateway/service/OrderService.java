package com.christdoes.gateway.service;

import com.christdoes.gateway.repository.OrderRepository;
import com.christdoes.gateway.repository.search.OrderSearchRepository;
import com.christdoes.gateway.service.dto.OrderDTO;
import com.christdoes.gateway.service.mapper.OrderMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.christdoes.gateway.domain.Order}.
 */
@Service
@Transactional
public class OrderService {

    private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;

    private final OrderMapper orderMapper;

    private final OrderSearchRepository orderSearchRepository;

    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper, OrderSearchRepository orderSearchRepository) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.orderSearchRepository = orderSearchRepository;
    }

    /**
     * Save a order.
     *
     * @param orderDTO the entity to save.
     * @return the persisted entity.
     */
    public Mono<OrderDTO> save(OrderDTO orderDTO) {
        LOG.debug("Request to save Order : {}", orderDTO);
        return orderRepository.save(orderMapper.toEntity(orderDTO)).flatMap(orderSearchRepository::save).map(orderMapper::toDto);
    }

    /**
     * Update a order.
     *
     * @param orderDTO the entity to save.
     * @return the persisted entity.
     */
    public Mono<OrderDTO> update(OrderDTO orderDTO) {
        LOG.debug("Request to update Order : {}", orderDTO);
        return orderRepository
            .save(orderMapper.toEntity(orderDTO).setIsPersisted())
            .flatMap(orderSearchRepository::save)
            .map(orderMapper::toDto);
    }

    /**
     * Partially update a order.
     *
     * @param orderDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<OrderDTO> partialUpdate(OrderDTO orderDTO) {
        LOG.debug("Request to partially update Order : {}", orderDTO);

        return orderRepository
            .findById(orderDTO.getId())
            .map(existingOrder -> {
                orderMapper.partialUpdate(existingOrder, orderDTO);

                return existingOrder;
            })
            .flatMap(orderRepository::save)
            .flatMap(savedOrder -> {
                orderSearchRepository.save(savedOrder);
                return Mono.just(savedOrder);
            })
            .map(orderMapper::toDto);
    }

    /**
     * Get all the orders.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<OrderDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Orders");
        return orderRepository.findAllBy(pageable).map(orderMapper::toDto);
    }

    /**
     * Get all the orders with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Flux<OrderDTO> findAllWithEagerRelationships(Pageable pageable) {
        return orderRepository.findAllWithEagerRelationships(pageable).map(orderMapper::toDto);
    }

    /**
     * Returns the number of orders available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return orderRepository.count();
    }

    /**
     * Returns the number of orders available in search repository.
     *
     */
    public Mono<Long> searchCount() {
        return orderSearchRepository.count();
    }

    /**
     * Get one order by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<OrderDTO> findOne(UUID id) {
        LOG.debug("Request to get Order : {}", id);
        return orderRepository.findOneWithEagerRelationships(id).map(orderMapper::toDto);
    }

    /**
     * Delete the order by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(UUID id) {
        LOG.debug("Request to delete Order : {}", id);
        return orderRepository.deleteById(id).then(orderSearchRepository.deleteById(id));
    }

    /**
     * Search for the order corresponding to the query.
     *
     * @param query the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<OrderDTO> search(String query, Pageable pageable) {
        LOG.debug("Request to search for a page of Orders for query {}", query);
        return orderSearchRepository.search(query, pageable).map(orderMapper::toDto);
    }
}
