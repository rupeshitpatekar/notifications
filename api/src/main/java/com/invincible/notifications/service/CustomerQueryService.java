package com.invincible.notifications.service;

import java.util.List;

import javax.persistence.criteria.JoinType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.jhipster.service.QueryService;

import com.invincible.notifications.domain.Customer;
import com.invincible.notifications.domain.*; // for static metamodels
import com.invincible.notifications.repository.CustomerRepository;
import com.invincible.notifications.repository.search.CustomerSearchRepository;
import com.invincible.notifications.service.dto.CustomerCriteria;
import com.invincible.notifications.service.dto.CustomerDTO;
import com.invincible.notifications.service.mapper.CustomerMapper;

/**
 * Service for executing complex queries for {@link Customer} entities in the database.
 * The main input is a {@link CustomerCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link CustomerDTO} or a {@link Page} of {@link CustomerDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CustomerQueryService extends QueryService<Customer> {

    private final Logger log = LoggerFactory.getLogger(CustomerQueryService.class);

    private final CustomerRepository customerRepository;

    private final CustomerMapper customerMapper;

    private final CustomerSearchRepository customerSearchRepository;

    public CustomerQueryService(CustomerRepository customerRepository, CustomerMapper customerMapper, CustomerSearchRepository customerSearchRepository) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.customerSearchRepository = customerSearchRepository;
    }

    /**
     * Return a {@link List} of {@link CustomerDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<CustomerDTO> findByCriteria(CustomerCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Customer> specification = createSpecification(criteria);
        return customerMapper.toDto(customerRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link CustomerDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CustomerDTO> findByCriteria(CustomerCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Customer> specification = createSpecification(criteria);
        return customerRepository.findAll(specification, page)
            .map(customerMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CustomerCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Customer> specification = createSpecification(criteria);
        return customerRepository.count(specification);
    }

    /**
     * Function to convert {@link CustomerCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Customer> createSpecification(CustomerCriteria criteria) {
        Specification<Customer> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Customer_.id));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), Customer_.name));
            }
            if (criteria.getMobile() != null) {
                specification = specification.and(buildStringSpecification(criteria.getMobile(), Customer_.mobile));
            }
            if (criteria.getEmail() != null) {
                specification = specification.and(buildStringSpecification(criteria.getEmail(), Customer_.email));
            }
            if (criteria.getAddress() != null) {
                specification = specification.and(buildStringSpecification(criteria.getAddress(), Customer_.address));
            }
            if (criteria.getCity() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCity(), Customer_.city));
            }
            if (criteria.getFirm_name() != null) {
                specification = specification.and(buildStringSpecification(criteria.getFirm_name(), Customer_.firm_name));
            }
            if (criteria.getBirthday() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getBirthday(), Customer_.birthday));
            }
            if (criteria.getAniversary() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAniversary(), Customer_.aniversary));
            }
        }
        return specification;
    }
}
