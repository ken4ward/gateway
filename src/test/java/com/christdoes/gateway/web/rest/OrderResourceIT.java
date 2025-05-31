package com.christdoes.gateway.web.rest;

import static com.christdoes.gateway.domain.OrderAsserts.*;
import static com.christdoes.gateway.web.rest.TestUtil.createUpdateProxyForBean;
import static com.christdoes.gateway.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.christdoes.gateway.IntegrationTest;
import com.christdoes.gateway.domain.Order;
import com.christdoes.gateway.domain.enumeration.OrderStatus;
import com.christdoes.gateway.repository.EntityManager;
import com.christdoes.gateway.repository.OrderRepository;
import com.christdoes.gateway.repository.search.OrderSearchRepository;
import com.christdoes.gateway.service.OrderService;
import com.christdoes.gateway.service.dto.OrderDTO;
import com.christdoes.gateway.service.mapper.OrderMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.data.util.Streamable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

/**
 * Integration tests for the {@link OrderResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class OrderResourceIT {

    private static final String DEFAULT_ORDER_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_ORDER_NUMBER = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_AMOUNT = new BigDecimal(2);

    private static final OrderStatus DEFAULT_STATUS = OrderStatus.PENDING;
    private static final OrderStatus UPDATED_STATUS = OrderStatus.PROCESSING;

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/orders";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/orders/_search";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private OrderRepository orderRepository;

    @Mock
    private OrderRepository orderRepositoryMock;

    @Autowired
    private OrderMapper orderMapper;

    @Mock
    private OrderService orderServiceMock;

    @Autowired
    private OrderSearchRepository orderSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Order order;

    private Order insertedOrder;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Order createEntity() {
        return new Order()
            .id(UUID.randomUUID())
            .orderNumber(DEFAULT_ORDER_NUMBER)
            .amount(DEFAULT_AMOUNT)
            .status(DEFAULT_STATUS)
            .createdAt(DEFAULT_CREATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Order createUpdatedEntity() {
        return new Order()
            .id(UUID.randomUUID())
            .orderNumber(UPDATED_ORDER_NUMBER)
            .amount(UPDATED_AMOUNT)
            .status(UPDATED_STATUS)
            .createdAt(UPDATED_CREATED_AT);
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Order.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    void setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @BeforeEach
    void initTest() {
        order = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedOrder != null) {
            orderRepository.delete(insertedOrder).block();
            orderSearchRepository.delete(insertedOrder).block();
            insertedOrder = null;
        }
        deleteEntities(em);
    }

    @Test
    void createOrder() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
        order.setId(null);
        // Create the Order
        OrderDTO orderDTO = orderMapper.toDto(order);
        var returnedOrderDTO = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(orderDTO))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(OrderDTO.class)
            .returnResult()
            .getResponseBody();

        // Validate the Order in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedOrder = orderMapper.toEntity(returnedOrderDTO);
        assertOrderUpdatableFieldsEquals(returnedOrder, getPersistedOrder(returnedOrder));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedOrder = returnedOrder;
    }

    @Test
    void createOrderWithExistingId() throws Exception {
        // Create the Order with an existing ID
        insertedOrder = orderRepository.save(order).block();
        OrderDTO orderDTO = orderMapper.toDto(order);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(orderDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Order in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkOrderNumberIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
        // set the field null
        order.setOrderNumber(null);

        // Create the Order, which fails.
        OrderDTO orderDTO = orderMapper.toDto(order);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(orderDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
        // set the field null
        order.setAmount(null);

        // Create the Order, which fails.
        OrderDTO orderDTO = orderMapper.toDto(order);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(orderDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
        // set the field null
        order.setStatus(null);

        // Create the Order, which fails.
        OrderDTO orderDTO = orderMapper.toDto(order);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(orderDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
        // set the field null
        order.setCreatedAt(null);

        // Create the Order, which fails.
        OrderDTO orderDTO = orderMapper.toDto(order);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(orderDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void getAllOrders() {
        // Initialize the database
        order.setId(UUID.randomUUID());
        insertedOrder = orderRepository.save(order).block();

        // Get all the orderList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(order.getId().toString()))
            .jsonPath("$.[*].orderNumber")
            .value(hasItem(DEFAULT_ORDER_NUMBER))
            .jsonPath("$.[*].amount")
            .value(hasItem(sameNumber(DEFAULT_AMOUNT)))
            .jsonPath("$.[*].status")
            .value(hasItem(DEFAULT_STATUS.toString()))
            .jsonPath("$.[*].createdAt")
            .value(hasItem(DEFAULT_CREATED_AT.toString()));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllOrdersWithEagerRelationshipsIsEnabled() {
        when(orderServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(orderServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllOrdersWithEagerRelationshipsIsNotEnabled() {
        when(orderServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=false").exchange().expectStatus().isOk();
        verify(orderRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getOrder() {
        // Initialize the database
        order.setId(UUID.randomUUID());
        insertedOrder = orderRepository.save(order).block();

        // Get the order
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, order.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(order.getId().toString()))
            .jsonPath("$.orderNumber")
            .value(is(DEFAULT_ORDER_NUMBER))
            .jsonPath("$.amount")
            .value(is(sameNumber(DEFAULT_AMOUNT)))
            .jsonPath("$.status")
            .value(is(DEFAULT_STATUS.toString()))
            .jsonPath("$.createdAt")
            .value(is(DEFAULT_CREATED_AT.toString()));
    }

    @Test
    void getNonExistingOrder() {
        // Get the order
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingOrder() throws Exception {
        // Initialize the database
        order.setId(UUID.randomUUID());
        insertedOrder = orderRepository.save(order).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();
        orderSearchRepository.save(order).block();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());

        // Update the order
        Order updatedOrder = orderRepository.findById(order.getId()).block();
        updatedOrder.orderNumber(UPDATED_ORDER_NUMBER).amount(UPDATED_AMOUNT).status(UPDATED_STATUS).createdAt(UPDATED_CREATED_AT);
        OrderDTO orderDTO = orderMapper.toDto(updatedOrder);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, orderDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(orderDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Order in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedOrderToMatchAllProperties(updatedOrder);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Order> orderSearchList = Streamable.of(orderSearchRepository.findAll().collectList().block()).toList();
                Order testOrderSearch = orderSearchList.get(searchDatabaseSizeAfter - 1);

                // Test fails because reactive api returns an empty object instead of null
                // assertOrderAllPropertiesEquals(testOrderSearch, updatedOrder);
                assertOrderUpdatableFieldsEquals(testOrderSearch, updatedOrder);
            });
    }

    @Test
    void putNonExistingOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
        order.setId(UUID.randomUUID());

        // Create the Order
        OrderDTO orderDTO = orderMapper.toDto(order);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, orderDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(orderDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Order in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithIdMismatchOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
        order.setId(UUID.randomUUID());

        // Create the Order
        OrderDTO orderDTO = orderMapper.toDto(order);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(orderDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Order in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithMissingIdPathParamOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
        order.setId(UUID.randomUUID());

        // Create the Order
        OrderDTO orderDTO = orderMapper.toDto(order);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(orderDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Order in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void partialUpdateOrderWithPatch() throws Exception {
        // Initialize the database
        order.setId(UUID.randomUUID());
        insertedOrder = orderRepository.save(order).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the order using partial update
        Order partialUpdatedOrder = new Order();
        partialUpdatedOrder.setId(order.getId());

        partialUpdatedOrder.orderNumber(UPDATED_ORDER_NUMBER).amount(UPDATED_AMOUNT).createdAt(UPDATED_CREATED_AT);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedOrder.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedOrder))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Order in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertOrderUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedOrder, order), getPersistedOrder(order));
    }

    @Test
    void fullUpdateOrderWithPatch() throws Exception {
        // Initialize the database
        order.setId(UUID.randomUUID());
        insertedOrder = orderRepository.save(order).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the order using partial update
        Order partialUpdatedOrder = new Order();
        partialUpdatedOrder.setId(order.getId());

        partialUpdatedOrder.orderNumber(UPDATED_ORDER_NUMBER).amount(UPDATED_AMOUNT).status(UPDATED_STATUS).createdAt(UPDATED_CREATED_AT);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedOrder.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedOrder))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Order in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertOrderUpdatableFieldsEquals(partialUpdatedOrder, getPersistedOrder(partialUpdatedOrder));
    }

    @Test
    void patchNonExistingOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
        order.setId(UUID.randomUUID());

        // Create the Order
        OrderDTO orderDTO = orderMapper.toDto(order);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, orderDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(orderDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Order in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithIdMismatchOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
        order.setId(UUID.randomUUID());

        // Create the Order
        OrderDTO orderDTO = orderMapper.toDto(order);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(orderDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Order in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithMissingIdPathParamOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
        order.setId(UUID.randomUUID());

        // Create the Order
        OrderDTO orderDTO = orderMapper.toDto(order);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(orderDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Order in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void deleteOrder() {
        // Initialize the database
        order.setId(UUID.randomUUID());
        insertedOrder = orderRepository.save(order).block();
        orderRepository.save(order).block();
        orderSearchRepository.save(order).block();

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the order
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, order.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(orderSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    void searchOrder() {
        // Initialize the database
        order.setId(UUID.randomUUID());
        insertedOrder = orderRepository.save(order).block();
        orderSearchRepository.save(order).block();

        // Search the order
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + order.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(order.getId().toString()))
            .jsonPath("$.[*].orderNumber")
            .value(hasItem(DEFAULT_ORDER_NUMBER))
            .jsonPath("$.[*].amount")
            .value(hasItem(sameNumber(DEFAULT_AMOUNT)))
            .jsonPath("$.[*].status")
            .value(hasItem(DEFAULT_STATUS.toString()))
            .jsonPath("$.[*].createdAt")
            .value(hasItem(DEFAULT_CREATED_AT.toString()));
    }

    protected long getRepositoryCount() {
        return orderRepository.count().block();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Order getPersistedOrder(Order order) {
        return orderRepository.findById(order.getId()).block();
    }

    protected void assertPersistedOrderToMatchAllProperties(Order expectedOrder) {
        // Test fails because reactive api returns an empty object instead of null
        // assertOrderAllPropertiesEquals(expectedOrder, getPersistedOrder(expectedOrder));
        assertOrderUpdatableFieldsEquals(expectedOrder, getPersistedOrder(expectedOrder));
    }

    protected void assertPersistedOrderToMatchUpdatableProperties(Order expectedOrder) {
        // Test fails because reactive api returns an empty object instead of null
        // assertOrderAllUpdatablePropertiesEquals(expectedOrder, getPersistedOrder(expectedOrder));
        assertOrderUpdatableFieldsEquals(expectedOrder, getPersistedOrder(expectedOrder));
    }
}
