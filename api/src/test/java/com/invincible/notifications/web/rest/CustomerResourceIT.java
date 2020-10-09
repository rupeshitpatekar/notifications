package com.invincible.notifications.web.rest;

import com.invincible.notifications.ApiApp;
import com.invincible.notifications.domain.Customer;
import com.invincible.notifications.repository.CustomerRepository;
import com.invincible.notifications.repository.search.CustomerSearchRepository;
import com.invincible.notifications.service.CustomerService;
import com.invincible.notifications.service.dto.CustomerDTO;
import com.invincible.notifications.service.mapper.CustomerMapper;
import com.invincible.notifications.service.dto.CustomerCriteria;
import com.invincible.notifications.service.CustomerQueryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link CustomerResource} REST controller.
 */
@SpringBootTest(classes = ApiApp.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
public class CustomerResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_MOBILE = "123456789";
    private static final String UPDATED_MOBILE = "12345678";
    private static final String SMALLER_MOBILE = "123456789";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final String DEFAULT_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_ADDRESS = "BBBBBBBBBB";

    private static final String DEFAULT_CITY = "AAAAAAAAAA";
    private static final String UPDATED_CITY = "BBBBBBBBBB";

    private static final String DEFAULT_FIRM_NAME = "AAAAAAAAAA";
    private static final String UPDATED_FIRM_NAME = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_BIRTHDAY = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_BIRTHDAY = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_BIRTHDAY = LocalDate.ofEpochDay(-1L);

    private static final LocalDate DEFAULT_ANIVERSARY = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_ANIVERSARY = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_ANIVERSARY = LocalDate.ofEpochDay(-1L);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private CustomerService customerService;

    /**
     * This repository is mocked in the com.invincible.notifications.repository.search test package.
     *
     * @see com.invincible.notifications.repository.search.CustomerSearchRepositoryMockConfiguration
     */
    @Autowired
    private CustomerSearchRepository mockCustomerSearchRepository;

    @Autowired
    private CustomerQueryService customerQueryService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCustomerMockMvc;

    private Customer customer;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Customer createEntity(EntityManager em) {
        Customer customer = new Customer()
            .name(DEFAULT_NAME)
            .mobile(DEFAULT_MOBILE)
            .email(DEFAULT_EMAIL)
            .address(DEFAULT_ADDRESS)
            .city(DEFAULT_CITY)
            .firm_name(DEFAULT_FIRM_NAME)
            .birthday(DEFAULT_BIRTHDAY)
            .aniversary(DEFAULT_ANIVERSARY);
        return customer;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Customer createUpdatedEntity(EntityManager em) {
        Customer customer = new Customer()
            .name(UPDATED_NAME)
            .mobile(UPDATED_MOBILE)
            .email(UPDATED_EMAIL)
            .address(UPDATED_ADDRESS)
            .city(UPDATED_CITY)
            .firm_name(UPDATED_FIRM_NAME)
            .birthday(UPDATED_BIRTHDAY)
            .aniversary(UPDATED_ANIVERSARY);
        return customer;
    }

    @BeforeEach
    public void initTest() {
        customer = createEntity(em);
    }

    @Test
    @Transactional
    public void createCustomer() throws Exception {
        int databaseSizeBeforeCreate = customerRepository.findAll().size();
        // Create the Customer
        CustomerDTO customerDTO = customerMapper.toDto(customer);
        restCustomerMockMvc.perform(post("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(customerDTO)))
            .andExpect(status().isCreated());

        // Validate the Customer in the database
        List<Customer> customerList = customerRepository.findAll();
        assertThat(customerList).hasSize(databaseSizeBeforeCreate + 1);
        Customer testCustomer = customerList.get(customerList.size() - 1);
        assertThat(testCustomer.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testCustomer.getMobile()).isEqualTo(DEFAULT_MOBILE);
        assertThat(testCustomer.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testCustomer.getAddress()).isEqualTo(DEFAULT_ADDRESS);
        assertThat(testCustomer.getCity()).isEqualTo(DEFAULT_CITY);
        assertThat(testCustomer.getFirm_name()).isEqualTo(DEFAULT_FIRM_NAME);
        assertThat(testCustomer.getBirthday()).isEqualTo(DEFAULT_BIRTHDAY);
        assertThat(testCustomer.getAniversary()).isEqualTo(DEFAULT_ANIVERSARY);

        // Validate the Customer in Elasticsearch
        verify(mockCustomerSearchRepository, times(1)).save(testCustomer);
    }

    @Test
    @Transactional
    public void createCustomerWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = customerRepository.findAll().size();

        // Create the Customer with an existing ID
        customer.setId(1L);
        CustomerDTO customerDTO = customerMapper.toDto(customer);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCustomerMockMvc.perform(post("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(customerDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Customer in the database
        List<Customer> customerList = customerRepository.findAll();
        assertThat(customerList).hasSize(databaseSizeBeforeCreate);

        // Validate the Customer in Elasticsearch
        verify(mockCustomerSearchRepository, times(0)).save(customer);
    }


    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = customerRepository.findAll().size();
        // set the field null
        customer.setName(null);

        // Create the Customer, which fails.
        CustomerDTO customerDTO = customerMapper.toDto(customer);


        restCustomerMockMvc.perform(post("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(customerDTO)))
            .andExpect(status().isBadRequest());

        List<Customer> customerList = customerRepository.findAll();
        assertThat(customerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkMobileIsRequired() throws Exception {
        int databaseSizeBeforeTest = customerRepository.findAll().size();
        // set the field null
        customer.setMobile(null);

        // Create the Customer, which fails.
        CustomerDTO customerDTO = customerMapper.toDto(customer);


        restCustomerMockMvc.perform(post("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(customerDTO)))
            .andExpect(status().isBadRequest());

        List<Customer> customerList = customerRepository.findAll();
        assertThat(customerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkEmailIsRequired() throws Exception {
        int databaseSizeBeforeTest = customerRepository.findAll().size();
        // set the field null
        customer.setEmail(null);

        // Create the Customer, which fails.
        CustomerDTO customerDTO = customerMapper.toDto(customer);


        restCustomerMockMvc.perform(post("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(customerDTO)))
            .andExpect(status().isBadRequest());

        List<Customer> customerList = customerRepository.findAll();
        assertThat(customerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkAddressIsRequired() throws Exception {
        int databaseSizeBeforeTest = customerRepository.findAll().size();
        // set the field null
        customer.setAddress(null);

        // Create the Customer, which fails.
        CustomerDTO customerDTO = customerMapper.toDto(customer);


        restCustomerMockMvc.perform(post("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(customerDTO)))
            .andExpect(status().isBadRequest());

        List<Customer> customerList = customerRepository.findAll();
        assertThat(customerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCityIsRequired() throws Exception {
        int databaseSizeBeforeTest = customerRepository.findAll().size();
        // set the field null
        customer.setCity(null);

        // Create the Customer, which fails.
        CustomerDTO customerDTO = customerMapper.toDto(customer);


        restCustomerMockMvc.perform(post("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(customerDTO)))
            .andExpect(status().isBadRequest());

        List<Customer> customerList = customerRepository.findAll();
        assertThat(customerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkFirm_nameIsRequired() throws Exception {
        int databaseSizeBeforeTest = customerRepository.findAll().size();
        // set the field null
        customer.setFirm_name(null);

        // Create the Customer, which fails.
        CustomerDTO customerDTO = customerMapper.toDto(customer);


        restCustomerMockMvc.perform(post("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(customerDTO)))
            .andExpect(status().isBadRequest());

        List<Customer> customerList = customerRepository.findAll();
        assertThat(customerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllCustomers() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList
        restCustomerMockMvc.perform(get("/api/customers?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(customer.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].mobile").value(hasItem(DEFAULT_MOBILE)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].city").value(hasItem(DEFAULT_CITY)))
            .andExpect(jsonPath("$.[*].firm_name").value(hasItem(DEFAULT_FIRM_NAME)))
            .andExpect(jsonPath("$.[*].birthday").value(hasItem(DEFAULT_BIRTHDAY.toString())))
            .andExpect(jsonPath("$.[*].aniversary").value(hasItem(DEFAULT_ANIVERSARY.toString())));
    }

    @Test
    @Transactional
    public void getCustomer() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get the customer
        restCustomerMockMvc.perform(get("/api/customers/{id}", customer.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(customer.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.mobile").value(DEFAULT_MOBILE))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.address").value(DEFAULT_ADDRESS))
            .andExpect(jsonPath("$.city").value(DEFAULT_CITY))
            .andExpect(jsonPath("$.firm_name").value(DEFAULT_FIRM_NAME))
            .andExpect(jsonPath("$.birthday").value(DEFAULT_BIRTHDAY.toString()))
            .andExpect(jsonPath("$.aniversary").value(DEFAULT_ANIVERSARY.toString()));
    }


    @Test
    @Transactional
    public void getCustomersByIdFiltering() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        Long id = customer.getId();

        defaultCustomerShouldBeFound("id.equals=" + id);
        defaultCustomerShouldNotBeFound("id.notEquals=" + id);

        defaultCustomerShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultCustomerShouldNotBeFound("id.greaterThan=" + id);

        defaultCustomerShouldBeFound("id.lessThanOrEqual=" + id);
        defaultCustomerShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllCustomersByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where name equals to DEFAULT_NAME
        defaultCustomerShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the customerList where name equals to UPDATED_NAME
        defaultCustomerShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllCustomersByNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where name not equals to DEFAULT_NAME
        defaultCustomerShouldNotBeFound("name.notEquals=" + DEFAULT_NAME);

        // Get all the customerList where name not equals to UPDATED_NAME
        defaultCustomerShouldBeFound("name.notEquals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllCustomersByNameIsInShouldWork() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where name in DEFAULT_NAME or UPDATED_NAME
        defaultCustomerShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the customerList where name equals to UPDATED_NAME
        defaultCustomerShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllCustomersByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where name is not null
        defaultCustomerShouldBeFound("name.specified=true");

        // Get all the customerList where name is null
        defaultCustomerShouldNotBeFound("name.specified=false");
    }
                @Test
    @Transactional
    public void getAllCustomersByNameContainsSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where name contains DEFAULT_NAME
        defaultCustomerShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the customerList where name contains UPDATED_NAME
        defaultCustomerShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllCustomersByNameNotContainsSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where name does not contain DEFAULT_NAME
        defaultCustomerShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the customerList where name does not contain UPDATED_NAME
        defaultCustomerShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }


    @Test
    @Transactional
    public void getAllCustomersByMobileIsEqualToSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where mobile equals to DEFAULT_MOBILE
        defaultCustomerShouldBeFound("mobile.equals=" + DEFAULT_MOBILE);

        // Get all the customerList where mobile equals to UPDATED_MOBILE
        defaultCustomerShouldNotBeFound("mobile.equals=" + UPDATED_MOBILE);
    }

    @Test
    @Transactional
    public void getAllCustomersByMobileIsNotEqualToSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where mobile not equals to DEFAULT_MOBILE
        defaultCustomerShouldNotBeFound("mobile.notEquals=" + DEFAULT_MOBILE);

        // Get all the customerList where mobile not equals to UPDATED_MOBILE
        defaultCustomerShouldBeFound("mobile.notEquals=" + UPDATED_MOBILE);
    }

    @Test
    @Transactional
    public void getAllCustomersByMobileIsInShouldWork() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where mobile in DEFAULT_MOBILE or UPDATED_MOBILE
        defaultCustomerShouldBeFound("mobile.in=" + DEFAULT_MOBILE + "," + UPDATED_MOBILE);

        // Get all the customerList where mobile equals to UPDATED_MOBILE
        defaultCustomerShouldNotBeFound("mobile.in=" + UPDATED_MOBILE);
    }

    @Test
    @Transactional
    public void getAllCustomersByMobileIsNullOrNotNull() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where mobile is not null
        defaultCustomerShouldBeFound("mobile.specified=true");

        // Get all the customerList where mobile is null
        defaultCustomerShouldNotBeFound("mobile.specified=false");
    }

    @Test
    @Transactional
    public void getAllCustomersByMobileIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where mobile is greater than or equal to DEFAULT_MOBILE
        defaultCustomerShouldBeFound("mobile.greaterThanOrEqual=" + DEFAULT_MOBILE);

        // Get all the customerList where mobile is greater than or equal to UPDATED_MOBILE
        defaultCustomerShouldNotBeFound("mobile.greaterThanOrEqual=" + UPDATED_MOBILE);
    }

    @Test
    @Transactional
    public void getAllCustomersByMobileIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where mobile is less than or equal to DEFAULT_MOBILE
        defaultCustomerShouldBeFound("mobile.lessThanOrEqual=" + DEFAULT_MOBILE);

        // Get all the customerList where mobile is less than or equal to SMALLER_MOBILE
        defaultCustomerShouldNotBeFound("mobile.lessThanOrEqual=" + SMALLER_MOBILE);
    }

    @Test
    @Transactional
    public void getAllCustomersByMobileIsLessThanSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where mobile is less than DEFAULT_MOBILE
        defaultCustomerShouldNotBeFound("mobile.lessThan=" + DEFAULT_MOBILE);

        // Get all the customerList where mobile is less than UPDATED_MOBILE
        defaultCustomerShouldBeFound("mobile.lessThan=" + UPDATED_MOBILE);
    }

    @Test
    @Transactional
    public void getAllCustomersByMobileIsGreaterThanSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where mobile is greater than DEFAULT_MOBILE
        defaultCustomerShouldNotBeFound("mobile.greaterThan=" + DEFAULT_MOBILE);

        // Get all the customerList where mobile is greater than SMALLER_MOBILE
        defaultCustomerShouldBeFound("mobile.greaterThan=" + SMALLER_MOBILE);
    }


    @Test
    @Transactional
    public void getAllCustomersByEmailIsEqualToSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where email equals to DEFAULT_EMAIL
        defaultCustomerShouldBeFound("email.equals=" + DEFAULT_EMAIL);

        // Get all the customerList where email equals to UPDATED_EMAIL
        defaultCustomerShouldNotBeFound("email.equals=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    public void getAllCustomersByEmailIsNotEqualToSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where email not equals to DEFAULT_EMAIL
        defaultCustomerShouldNotBeFound("email.notEquals=" + DEFAULT_EMAIL);

        // Get all the customerList where email not equals to UPDATED_EMAIL
        defaultCustomerShouldBeFound("email.notEquals=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    public void getAllCustomersByEmailIsInShouldWork() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where email in DEFAULT_EMAIL or UPDATED_EMAIL
        defaultCustomerShouldBeFound("email.in=" + DEFAULT_EMAIL + "," + UPDATED_EMAIL);

        // Get all the customerList where email equals to UPDATED_EMAIL
        defaultCustomerShouldNotBeFound("email.in=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    public void getAllCustomersByEmailIsNullOrNotNull() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where email is not null
        defaultCustomerShouldBeFound("email.specified=true");

        // Get all the customerList where email is null
        defaultCustomerShouldNotBeFound("email.specified=false");
    }
                @Test
    @Transactional
    public void getAllCustomersByEmailContainsSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where email contains DEFAULT_EMAIL
        defaultCustomerShouldBeFound("email.contains=" + DEFAULT_EMAIL);

        // Get all the customerList where email contains UPDATED_EMAIL
        defaultCustomerShouldNotBeFound("email.contains=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    public void getAllCustomersByEmailNotContainsSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where email does not contain DEFAULT_EMAIL
        defaultCustomerShouldNotBeFound("email.doesNotContain=" + DEFAULT_EMAIL);

        // Get all the customerList where email does not contain UPDATED_EMAIL
        defaultCustomerShouldBeFound("email.doesNotContain=" + UPDATED_EMAIL);
    }


    @Test
    @Transactional
    public void getAllCustomersByAddressIsEqualToSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where address equals to DEFAULT_ADDRESS
        defaultCustomerShouldBeFound("address.equals=" + DEFAULT_ADDRESS);

        // Get all the customerList where address equals to UPDATED_ADDRESS
        defaultCustomerShouldNotBeFound("address.equals=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    public void getAllCustomersByAddressIsNotEqualToSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where address not equals to DEFAULT_ADDRESS
        defaultCustomerShouldNotBeFound("address.notEquals=" + DEFAULT_ADDRESS);

        // Get all the customerList where address not equals to UPDATED_ADDRESS
        defaultCustomerShouldBeFound("address.notEquals=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    public void getAllCustomersByAddressIsInShouldWork() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where address in DEFAULT_ADDRESS or UPDATED_ADDRESS
        defaultCustomerShouldBeFound("address.in=" + DEFAULT_ADDRESS + "," + UPDATED_ADDRESS);

        // Get all the customerList where address equals to UPDATED_ADDRESS
        defaultCustomerShouldNotBeFound("address.in=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    public void getAllCustomersByAddressIsNullOrNotNull() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where address is not null
        defaultCustomerShouldBeFound("address.specified=true");

        // Get all the customerList where address is null
        defaultCustomerShouldNotBeFound("address.specified=false");
    }
                @Test
    @Transactional
    public void getAllCustomersByAddressContainsSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where address contains DEFAULT_ADDRESS
        defaultCustomerShouldBeFound("address.contains=" + DEFAULT_ADDRESS);

        // Get all the customerList where address contains UPDATED_ADDRESS
        defaultCustomerShouldNotBeFound("address.contains=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    public void getAllCustomersByAddressNotContainsSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where address does not contain DEFAULT_ADDRESS
        defaultCustomerShouldNotBeFound("address.doesNotContain=" + DEFAULT_ADDRESS);

        // Get all the customerList where address does not contain UPDATED_ADDRESS
        defaultCustomerShouldBeFound("address.doesNotContain=" + UPDATED_ADDRESS);
    }


    @Test
    @Transactional
    public void getAllCustomersByCityIsEqualToSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where city equals to DEFAULT_CITY
        defaultCustomerShouldBeFound("city.equals=" + DEFAULT_CITY);

        // Get all the customerList where city equals to UPDATED_CITY
        defaultCustomerShouldNotBeFound("city.equals=" + UPDATED_CITY);
    }

    @Test
    @Transactional
    public void getAllCustomersByCityIsNotEqualToSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where city not equals to DEFAULT_CITY
        defaultCustomerShouldNotBeFound("city.notEquals=" + DEFAULT_CITY);

        // Get all the customerList where city not equals to UPDATED_CITY
        defaultCustomerShouldBeFound("city.notEquals=" + UPDATED_CITY);
    }

    @Test
    @Transactional
    public void getAllCustomersByCityIsInShouldWork() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where city in DEFAULT_CITY or UPDATED_CITY
        defaultCustomerShouldBeFound("city.in=" + DEFAULT_CITY + "," + UPDATED_CITY);

        // Get all the customerList where city equals to UPDATED_CITY
        defaultCustomerShouldNotBeFound("city.in=" + UPDATED_CITY);
    }

    @Test
    @Transactional
    public void getAllCustomersByCityIsNullOrNotNull() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where city is not null
        defaultCustomerShouldBeFound("city.specified=true");

        // Get all the customerList where city is null
        defaultCustomerShouldNotBeFound("city.specified=false");
    }
                @Test
    @Transactional
    public void getAllCustomersByCityContainsSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where city contains DEFAULT_CITY
        defaultCustomerShouldBeFound("city.contains=" + DEFAULT_CITY);

        // Get all the customerList where city contains UPDATED_CITY
        defaultCustomerShouldNotBeFound("city.contains=" + UPDATED_CITY);
    }

    @Test
    @Transactional
    public void getAllCustomersByCityNotContainsSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where city does not contain DEFAULT_CITY
        defaultCustomerShouldNotBeFound("city.doesNotContain=" + DEFAULT_CITY);

        // Get all the customerList where city does not contain UPDATED_CITY
        defaultCustomerShouldBeFound("city.doesNotContain=" + UPDATED_CITY);
    }


    @Test
    @Transactional
    public void getAllCustomersByFirm_nameIsEqualToSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where firm_name equals to DEFAULT_FIRM_NAME
        defaultCustomerShouldBeFound("firm_name.equals=" + DEFAULT_FIRM_NAME);

        // Get all the customerList where firm_name equals to UPDATED_FIRM_NAME
        defaultCustomerShouldNotBeFound("firm_name.equals=" + UPDATED_FIRM_NAME);
    }

    @Test
    @Transactional
    public void getAllCustomersByFirm_nameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where firm_name not equals to DEFAULT_FIRM_NAME
        defaultCustomerShouldNotBeFound("firm_name.notEquals=" + DEFAULT_FIRM_NAME);

        // Get all the customerList where firm_name not equals to UPDATED_FIRM_NAME
        defaultCustomerShouldBeFound("firm_name.notEquals=" + UPDATED_FIRM_NAME);
    }

    @Test
    @Transactional
    public void getAllCustomersByFirm_nameIsInShouldWork() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where firm_name in DEFAULT_FIRM_NAME or UPDATED_FIRM_NAME
        defaultCustomerShouldBeFound("firm_name.in=" + DEFAULT_FIRM_NAME + "," + UPDATED_FIRM_NAME);

        // Get all the customerList where firm_name equals to UPDATED_FIRM_NAME
        defaultCustomerShouldNotBeFound("firm_name.in=" + UPDATED_FIRM_NAME);
    }

    @Test
    @Transactional
    public void getAllCustomersByFirm_nameIsNullOrNotNull() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where firm_name is not null
        defaultCustomerShouldBeFound("firm_name.specified=true");

        // Get all the customerList where firm_name is null
        defaultCustomerShouldNotBeFound("firm_name.specified=false");
    }
                @Test
    @Transactional
    public void getAllCustomersByFirm_nameContainsSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where firm_name contains DEFAULT_FIRM_NAME
        defaultCustomerShouldBeFound("firm_name.contains=" + DEFAULT_FIRM_NAME);

        // Get all the customerList where firm_name contains UPDATED_FIRM_NAME
        defaultCustomerShouldNotBeFound("firm_name.contains=" + UPDATED_FIRM_NAME);
    }

    @Test
    @Transactional
    public void getAllCustomersByFirm_nameNotContainsSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where firm_name does not contain DEFAULT_FIRM_NAME
        defaultCustomerShouldNotBeFound("firm_name.doesNotContain=" + DEFAULT_FIRM_NAME);

        // Get all the customerList where firm_name does not contain UPDATED_FIRM_NAME
        defaultCustomerShouldBeFound("firm_name.doesNotContain=" + UPDATED_FIRM_NAME);
    }


    @Test
    @Transactional
    public void getAllCustomersByBirthdayIsEqualToSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where birthday equals to DEFAULT_BIRTHDAY
        defaultCustomerShouldBeFound("birthday.equals=" + DEFAULT_BIRTHDAY);

        // Get all the customerList where birthday equals to UPDATED_BIRTHDAY
        defaultCustomerShouldNotBeFound("birthday.equals=" + UPDATED_BIRTHDAY);
    }

    @Test
    @Transactional
    public void getAllCustomersByBirthdayIsNotEqualToSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where birthday not equals to DEFAULT_BIRTHDAY
        defaultCustomerShouldNotBeFound("birthday.notEquals=" + DEFAULT_BIRTHDAY);

        // Get all the customerList where birthday not equals to UPDATED_BIRTHDAY
        defaultCustomerShouldBeFound("birthday.notEquals=" + UPDATED_BIRTHDAY);
    }

    @Test
    @Transactional
    public void getAllCustomersByBirthdayIsInShouldWork() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where birthday in DEFAULT_BIRTHDAY or UPDATED_BIRTHDAY
        defaultCustomerShouldBeFound("birthday.in=" + DEFAULT_BIRTHDAY + "," + UPDATED_BIRTHDAY);

        // Get all the customerList where birthday equals to UPDATED_BIRTHDAY
        defaultCustomerShouldNotBeFound("birthday.in=" + UPDATED_BIRTHDAY);
    }

    @Test
    @Transactional
    public void getAllCustomersByBirthdayIsNullOrNotNull() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where birthday is not null
        defaultCustomerShouldBeFound("birthday.specified=true");

        // Get all the customerList where birthday is null
        defaultCustomerShouldNotBeFound("birthday.specified=false");
    }

    @Test
    @Transactional
    public void getAllCustomersByBirthdayIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where birthday is greater than or equal to DEFAULT_BIRTHDAY
        defaultCustomerShouldBeFound("birthday.greaterThanOrEqual=" + DEFAULT_BIRTHDAY);

        // Get all the customerList where birthday is greater than or equal to UPDATED_BIRTHDAY
        defaultCustomerShouldNotBeFound("birthday.greaterThanOrEqual=" + UPDATED_BIRTHDAY);
    }

    @Test
    @Transactional
    public void getAllCustomersByBirthdayIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where birthday is less than or equal to DEFAULT_BIRTHDAY
        defaultCustomerShouldBeFound("birthday.lessThanOrEqual=" + DEFAULT_BIRTHDAY);

        // Get all the customerList where birthday is less than or equal to SMALLER_BIRTHDAY
        defaultCustomerShouldNotBeFound("birthday.lessThanOrEqual=" + SMALLER_BIRTHDAY);
    }

    @Test
    @Transactional
    public void getAllCustomersByBirthdayIsLessThanSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where birthday is less than DEFAULT_BIRTHDAY
        defaultCustomerShouldNotBeFound("birthday.lessThan=" + DEFAULT_BIRTHDAY);

        // Get all the customerList where birthday is less than UPDATED_BIRTHDAY
        defaultCustomerShouldBeFound("birthday.lessThan=" + UPDATED_BIRTHDAY);
    }

    @Test
    @Transactional
    public void getAllCustomersByBirthdayIsGreaterThanSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where birthday is greater than DEFAULT_BIRTHDAY
        defaultCustomerShouldNotBeFound("birthday.greaterThan=" + DEFAULT_BIRTHDAY);

        // Get all the customerList where birthday is greater than SMALLER_BIRTHDAY
        defaultCustomerShouldBeFound("birthday.greaterThan=" + SMALLER_BIRTHDAY);
    }


    @Test
    @Transactional
    public void getAllCustomersByAniversaryIsEqualToSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where aniversary equals to DEFAULT_ANIVERSARY
        defaultCustomerShouldBeFound("aniversary.equals=" + DEFAULT_ANIVERSARY);

        // Get all the customerList where aniversary equals to UPDATED_ANIVERSARY
        defaultCustomerShouldNotBeFound("aniversary.equals=" + UPDATED_ANIVERSARY);
    }

    @Test
    @Transactional
    public void getAllCustomersByAniversaryIsNotEqualToSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where aniversary not equals to DEFAULT_ANIVERSARY
        defaultCustomerShouldNotBeFound("aniversary.notEquals=" + DEFAULT_ANIVERSARY);

        // Get all the customerList where aniversary not equals to UPDATED_ANIVERSARY
        defaultCustomerShouldBeFound("aniversary.notEquals=" + UPDATED_ANIVERSARY);
    }

    @Test
    @Transactional
    public void getAllCustomersByAniversaryIsInShouldWork() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where aniversary in DEFAULT_ANIVERSARY or UPDATED_ANIVERSARY
        defaultCustomerShouldBeFound("aniversary.in=" + DEFAULT_ANIVERSARY + "," + UPDATED_ANIVERSARY);

        // Get all the customerList where aniversary equals to UPDATED_ANIVERSARY
        defaultCustomerShouldNotBeFound("aniversary.in=" + UPDATED_ANIVERSARY);
    }

    @Test
    @Transactional
    public void getAllCustomersByAniversaryIsNullOrNotNull() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where aniversary is not null
        defaultCustomerShouldBeFound("aniversary.specified=true");

        // Get all the customerList where aniversary is null
        defaultCustomerShouldNotBeFound("aniversary.specified=false");
    }

    @Test
    @Transactional
    public void getAllCustomersByAniversaryIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where aniversary is greater than or equal to DEFAULT_ANIVERSARY
        defaultCustomerShouldBeFound("aniversary.greaterThanOrEqual=" + DEFAULT_ANIVERSARY);

        // Get all the customerList where aniversary is greater than or equal to UPDATED_ANIVERSARY
        defaultCustomerShouldNotBeFound("aniversary.greaterThanOrEqual=" + UPDATED_ANIVERSARY);
    }

    @Test
    @Transactional
    public void getAllCustomersByAniversaryIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where aniversary is less than or equal to DEFAULT_ANIVERSARY
        defaultCustomerShouldBeFound("aniversary.lessThanOrEqual=" + DEFAULT_ANIVERSARY);

        // Get all the customerList where aniversary is less than or equal to SMALLER_ANIVERSARY
        defaultCustomerShouldNotBeFound("aniversary.lessThanOrEqual=" + SMALLER_ANIVERSARY);
    }

    @Test
    @Transactional
    public void getAllCustomersByAniversaryIsLessThanSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where aniversary is less than DEFAULT_ANIVERSARY
        defaultCustomerShouldNotBeFound("aniversary.lessThan=" + DEFAULT_ANIVERSARY);

        // Get all the customerList where aniversary is less than UPDATED_ANIVERSARY
        defaultCustomerShouldBeFound("aniversary.lessThan=" + UPDATED_ANIVERSARY);
    }

    @Test
    @Transactional
    public void getAllCustomersByAniversaryIsGreaterThanSomething() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList where aniversary is greater than DEFAULT_ANIVERSARY
        defaultCustomerShouldNotBeFound("aniversary.greaterThan=" + DEFAULT_ANIVERSARY);

        // Get all the customerList where aniversary is greater than SMALLER_ANIVERSARY
        defaultCustomerShouldBeFound("aniversary.greaterThan=" + SMALLER_ANIVERSARY);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCustomerShouldBeFound(String filter) throws Exception {
        restCustomerMockMvc.perform(get("/api/customers?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(customer.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].mobile").value(hasItem(DEFAULT_MOBILE)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].city").value(hasItem(DEFAULT_CITY)))
            .andExpect(jsonPath("$.[*].firm_name").value(hasItem(DEFAULT_FIRM_NAME)))
            .andExpect(jsonPath("$.[*].birthday").value(hasItem(DEFAULT_BIRTHDAY.toString())))
            .andExpect(jsonPath("$.[*].aniversary").value(hasItem(DEFAULT_ANIVERSARY.toString())));

        // Check, that the count call also returns 1
        restCustomerMockMvc.perform(get("/api/customers/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCustomerShouldNotBeFound(String filter) throws Exception {
        restCustomerMockMvc.perform(get("/api/customers?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCustomerMockMvc.perform(get("/api/customers/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingCustomer() throws Exception {
        // Get the customer
        restCustomerMockMvc.perform(get("/api/customers/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCustomer() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        int databaseSizeBeforeUpdate = customerRepository.findAll().size();

        // Update the customer
        Customer updatedCustomer = customerRepository.findById(customer.getId()).get();
        // Disconnect from session so that the updates on updatedCustomer are not directly saved in db
        em.detach(updatedCustomer);
        updatedCustomer
            .name(UPDATED_NAME)
            .mobile(UPDATED_MOBILE)
            .email(UPDATED_EMAIL)
            .address(UPDATED_ADDRESS)
            .city(UPDATED_CITY)
            .firm_name(UPDATED_FIRM_NAME)
            .birthday(UPDATED_BIRTHDAY)
            .aniversary(UPDATED_ANIVERSARY);
        CustomerDTO customerDTO = customerMapper.toDto(updatedCustomer);

        restCustomerMockMvc.perform(put("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(customerDTO)))
            .andExpect(status().isOk());

        // Validate the Customer in the database
        List<Customer> customerList = customerRepository.findAll();
        assertThat(customerList).hasSize(databaseSizeBeforeUpdate);
        Customer testCustomer = customerList.get(customerList.size() - 1);
        assertThat(testCustomer.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testCustomer.getMobile()).isEqualTo(UPDATED_MOBILE);
        assertThat(testCustomer.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testCustomer.getAddress()).isEqualTo(UPDATED_ADDRESS);
        assertThat(testCustomer.getCity()).isEqualTo(UPDATED_CITY);
        assertThat(testCustomer.getFirm_name()).isEqualTo(UPDATED_FIRM_NAME);
        assertThat(testCustomer.getBirthday()).isEqualTo(UPDATED_BIRTHDAY);
        assertThat(testCustomer.getAniversary()).isEqualTo(UPDATED_ANIVERSARY);

        // Validate the Customer in Elasticsearch
        verify(mockCustomerSearchRepository, times(1)).save(testCustomer);
    }

    @Test
    @Transactional
    public void updateNonExistingCustomer() throws Exception {
        int databaseSizeBeforeUpdate = customerRepository.findAll().size();

        // Create the Customer
        CustomerDTO customerDTO = customerMapper.toDto(customer);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCustomerMockMvc.perform(put("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(customerDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Customer in the database
        List<Customer> customerList = customerRepository.findAll();
        assertThat(customerList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Customer in Elasticsearch
        verify(mockCustomerSearchRepository, times(0)).save(customer);
    }

    @Test
    @Transactional
    public void deleteCustomer() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        int databaseSizeBeforeDelete = customerRepository.findAll().size();

        // Delete the customer
        restCustomerMockMvc.perform(delete("/api/customers/{id}", customer.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Customer> customerList = customerRepository.findAll();
        assertThat(customerList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Customer in Elasticsearch
        verify(mockCustomerSearchRepository, times(1)).deleteById(customer.getId());
    }

    @Test
    @Transactional
    public void searchCustomer() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        customerRepository.saveAndFlush(customer);
        when(mockCustomerSearchRepository.search(queryStringQuery("id:" + customer.getId()), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(customer), PageRequest.of(0, 1), 1));

        // Search the customer
        restCustomerMockMvc.perform(get("/api/_search/customers?query=id:" + customer.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(customer.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].mobile").value(hasItem(DEFAULT_MOBILE)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].city").value(hasItem(DEFAULT_CITY)))
            .andExpect(jsonPath("$.[*].firm_name").value(hasItem(DEFAULT_FIRM_NAME)))
            .andExpect(jsonPath("$.[*].birthday").value(hasItem(DEFAULT_BIRTHDAY.toString())))
            .andExpect(jsonPath("$.[*].aniversary").value(hasItem(DEFAULT_ANIVERSARY.toString())));
    }
}
