package com.live_commerce.company.domain.repository;

import com.live_commerce.company.domain.model.Company;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import com.live_commerce.company.domain.model.QCompany;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CompanyQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QCompany company = QCompany.company;

    //업체 전체 조회 쿼리DSL
    public Page<Company> findAll(Pageable pageable) {
        List<Company> companies = queryFactory
                .selectFrom(company)
                .where(company.deletedStatus.isFalse())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(queryFactory
                .select(company.count())
                .from(company)
                .where(company.deletedStatus.isFalse())
                .fetchFirst()).orElse(0L);

        return new PageImpl<>(companies, pageable, total);
    }

    //업체 검색 쿼리 DSL
    public Page<Company> getCompaniesByKeyword(Pageable pageable, String keyword) {
        List<Company> companies = queryFactory
                .selectFrom(company)
                .where(company.name.containsIgnoreCase(keyword)
                        .and(company.deletedStatus.isFalse())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();  // 결과 리스트 반환

        long total = Optional.ofNullable(queryFactory
                .select(company.count())
                .from(company)
                .where(company.name.containsIgnoreCase(keyword)
                        .and(company.deletedStatus.isFalse())
                )
                .fetchOne()).orElse(0L);
        return new PageImpl<>(companies, pageable, total);
    }
}
