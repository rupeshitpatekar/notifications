package com.invincible.notifications.service.dto;

import java.time.LocalDate;
import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * A DTO for the {@link com.invincible.notifications.domain.Customer} entity.
 */
public class CustomerDTO implements Serializable {

    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String mobile;

    @NotNull
    private String email;

    @NotNull
    private String address;

    @NotNull
    private String city;

    @NotNull
    private String firm_name;

    private LocalDate birthday;

    private LocalDate aniversary;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getFirm_name() {
        return firm_name;
    }

    public void setFirm_name(String firm_name) {
        this.firm_name = firm_name;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public LocalDate getAniversary() {
        return aniversary;
    }

    public void setAniversary(LocalDate aniversary) {
        this.aniversary = aniversary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CustomerDTO)) {
            return false;
        }

        return id != null && id.equals(((CustomerDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CustomerDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", mobile=" + getMobile() +
            ", email='" + getEmail() + "'" +
            ", address='" + getAddress() + "'" +
            ", city='" + getCity() + "'" +
            ", firm_name='" + getFirm_name() + "'" +
            ", birthday='" + getBirthday() + "'" +
            ", aniversary='" + getAniversary() + "'" +
            "}";
    }
}
