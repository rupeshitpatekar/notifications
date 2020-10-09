package com.invincible.notifications.service.dto;

import java.io.Serializable;
import java.util.Objects;
import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.FloatFilter;
import io.github.jhipster.service.filter.IntegerFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import io.github.jhipster.service.filter.LocalDateFilter;

/**
 * Criteria class for the {@link com.invincible.notifications.domain.Customer} entity. This class is used
 * in {@link com.invincible.notifications.web.rest.CustomerResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /customers?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class CustomerCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private StringFilter mobile;

    private StringFilter email;

    private StringFilter address;

    private StringFilter city;

    private StringFilter firm_name;

    private LocalDateFilter birthday;

    private LocalDateFilter aniversary;

    public CustomerCriteria() {
    }

    public CustomerCriteria(CustomerCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.mobile = other.mobile == null ? null : other.mobile.copy();
        this.email = other.email == null ? null : other.email.copy();
        this.address = other.address == null ? null : other.address.copy();
        this.city = other.city == null ? null : other.city.copy();
        this.firm_name = other.firm_name == null ? null : other.firm_name.copy();
        this.birthday = other.birthday == null ? null : other.birthday.copy();
        this.aniversary = other.aniversary == null ? null : other.aniversary.copy();
    }

    @Override
    public CustomerCriteria copy() {
        return new CustomerCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getName() {
        return name;
    }

    public void setName(StringFilter name) {
        this.name = name;
    }

    public StringFilter getMobile() {
        return mobile;
    }

    public void setMobile(StringFilter mobile) {
        this.mobile = mobile;
    }

    public StringFilter getEmail() {
        return email;
    }

    public void setEmail(StringFilter email) {
        this.email = email;
    }

    public StringFilter getAddress() {
        return address;
    }

    public void setAddress(StringFilter address) {
        this.address = address;
    }

    public StringFilter getCity() {
        return city;
    }

    public void setCity(StringFilter city) {
        this.city = city;
    }

    public StringFilter getFirm_name() {
        return firm_name;
    }

    public void setFirm_name(StringFilter firm_name) {
        this.firm_name = firm_name;
    }

    public LocalDateFilter getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDateFilter birthday) {
        this.birthday = birthday;
    }

    public LocalDateFilter getAniversary() {
        return aniversary;
    }

    public void setAniversary(LocalDateFilter aniversary) {
        this.aniversary = aniversary;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CustomerCriteria that = (CustomerCriteria) o;
        return
            Objects.equals(id, that.id) &&
            Objects.equals(name, that.name) &&
            Objects.equals(mobile, that.mobile) &&
            Objects.equals(email, that.email) &&
            Objects.equals(address, that.address) &&
            Objects.equals(city, that.city) &&
            Objects.equals(firm_name, that.firm_name) &&
            Objects.equals(birthday, that.birthday) &&
            Objects.equals(aniversary, that.aniversary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        name,
        mobile,
        email,
        address,
        city,
        firm_name,
        birthday,
        aniversary
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CustomerCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (name != null ? "name=" + name + ", " : "") +
                (mobile != null ? "mobile=" + mobile + ", " : "") +
                (email != null ? "email=" + email + ", " : "") +
                (address != null ? "address=" + address + ", " : "") +
                (city != null ? "city=" + city + ", " : "") +
                (firm_name != null ? "firm_name=" + firm_name + ", " : "") +
                (birthday != null ? "birthday=" + birthday + ", " : "") +
                (aniversary != null ? "aniversary=" + aniversary + ", " : "") +
            "}";
    }

}
