package com.live_commerce.company.domain.model;

import com.live_commerce.company.application.dto.request.CompanyUpdateRequest;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.util.UUID;

@Getter
@Entity
@Table(name = "p_company")
@NoArgsConstructor
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    //DB의 company_id와 매핑
    @Column(name = "company_id")
    private UUID id;

    private String name;

    private UUID owner;

    private CompanyType type;

    private String address;

    private String number;

    private String description;

    public Company(String name, UUID owner, CompanyType type, String address, String number, String description) {
        this.name = name;
        this.owner = owner;
        this.type = type;
        this.address = address;
        this.number = number;
        this.description = description;
        this.deletedStatus = false;
    }

    public void update(final CompanyUpdateRequest updateCompany){
        this.name = updateCompany.name();
        this.type = updateCompany.type();
        this.address = updateCompany.address();
        this.number = updateCompany.number();
        this.description = updateCompany.description();
        this.deletedStatus = false;
    }
}
