package com.christdoes.gateway.web.rest;

import com.christdoes.gateway.repository.UserProfileRepository;
import com.christdoes.gateway.service.UserProfileService;
import com.christdoes.gateway.service.dto.UserProfileDTO;
import com.christdoes.gateway.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.ForwardedHeaderUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.christdoes.gateway.domain.UserProfile}.
 */
@RestController
@RequestMapping("/api/user-profiles")
public class UserProfileResource {

    private static final Logger LOG = LoggerFactory.getLogger(UserProfileResource.class);

    private static final String ENTITY_NAME = "userProfile";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final UserProfileService userProfileService;

    private final UserProfileRepository userProfileRepository;

    public UserProfileResource(UserProfileService userProfileService, UserProfileRepository userProfileRepository) {
        this.userProfileService = userProfileService;
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * {@code POST  /user-profiles} : Create a new userProfile.
     *
     * @param userProfileDTO the userProfileDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new userProfileDTO, or with status {@code 400 (Bad Request)} if the userProfile has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<UserProfileDTO>> createUserProfile(@Valid @RequestBody UserProfileDTO userProfileDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save UserProfile : {}", userProfileDTO);
        if (userProfileDTO.getId() != null) {
            throw new BadRequestAlertException("A new userProfile cannot already have an ID", ENTITY_NAME, "idexists");
        }
        userProfileDTO.setId(UUID.randomUUID());
        return userProfileService
            .save(userProfileDTO)
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/user-profiles/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /user-profiles/:id} : Updates an existing userProfile.
     *
     * @param id the id of the userProfileDTO to save.
     * @param userProfileDTO the userProfileDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated userProfileDTO,
     * or with status {@code 400 (Bad Request)} if the userProfileDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the userProfileDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<UserProfileDTO>> updateUserProfile(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody UserProfileDTO userProfileDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update UserProfile : {}, {}", id, userProfileDTO);
        if (userProfileDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, userProfileDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return userProfileRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return userProfileService
                    .update(userProfileDTO)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /user-profiles/:id} : Partial updates given fields of an existing userProfile, field will ignore if it is null
     *
     * @param id the id of the userProfileDTO to save.
     * @param userProfileDTO the userProfileDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated userProfileDTO,
     * or with status {@code 400 (Bad Request)} if the userProfileDTO is not valid,
     * or with status {@code 404 (Not Found)} if the userProfileDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the userProfileDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<UserProfileDTO>> partialUpdateUserProfile(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody UserProfileDTO userProfileDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update UserProfile partially : {}, {}", id, userProfileDTO);
        if (userProfileDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, userProfileDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return userProfileRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<UserProfileDTO> result = userProfileService.partialUpdate(userProfileDTO);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /user-profiles} : get all the userProfiles.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of userProfiles in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<UserProfileDTO>>> getAllUserProfiles(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        LOG.debug("REST request to get a page of UserProfiles");
        return userProfileService
            .countAll()
            .zipWith(userProfileService.findAll(pageable).collectList())
            .map(countWithEntities ->
                ResponseEntity.ok()
                    .headers(
                        PaginationUtil.generatePaginationHttpHeaders(
                            ForwardedHeaderUtils.adaptFromForwardedHeaders(request.getURI(), request.getHeaders()),
                            new PageImpl<>(countWithEntities.getT2(), pageable, countWithEntities.getT1())
                        )
                    )
                    .body(countWithEntities.getT2())
            );
    }

    /**
     * {@code GET  /user-profiles/:id} : get the "id" userProfile.
     *
     * @param id the id of the userProfileDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the userProfileDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<UserProfileDTO>> getUserProfile(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get UserProfile : {}", id);
        Mono<UserProfileDTO> userProfileDTO = userProfileService.findOne(id);
        return ResponseUtil.wrapOrNotFound(userProfileDTO);
    }

    /**
     * {@code DELETE  /user-profiles/:id} : delete the "id" userProfile.
     *
     * @param id the id of the userProfileDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteUserProfile(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete UserProfile : {}", id);
        return userProfileService
            .delete(id)
            .then(
                Mono.just(
                    ResponseEntity.noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                        .build()
                )
            );
    }

    /**
     * {@code SEARCH  /user-profiles/_search?query=:query} : search for the userProfile corresponding
     * to the query.
     *
     * @param query the query of the userProfile search.
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public Mono<ResponseEntity<Flux<UserProfileDTO>>> searchUserProfiles(
        @RequestParam("query") String query,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        LOG.debug("REST request to search for a page of UserProfiles for query {}", query);
        return userProfileService
            .searchCount()
            .map(total -> new PageImpl<>(new ArrayList<>(), pageable, total))
            .map(page ->
                PaginationUtil.generatePaginationHttpHeaders(
                    ForwardedHeaderUtils.adaptFromForwardedHeaders(request.getURI(), request.getHeaders()),
                    page
                )
            )
            .map(headers -> ResponseEntity.ok().headers(headers).body(userProfileService.search(query, pageable)));
    }
}
