package com.live_commerce.company.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
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

    public Company(String name, UUID owner, CompanyType type, String address, String number) {
        this.name = name;
        this.type = type;
        this.owner = owner;
        this.address = address;
        this.number = number;
    }
}
