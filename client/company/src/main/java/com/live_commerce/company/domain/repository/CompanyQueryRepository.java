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

@Repository
@RequiredArgsConstructor
public class CompanyQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QCompany company = QCompany.company;

    //업체 전체 조회 쿼리DSL
    public Page<Company> findAll(Pageable pageable) {
        List<Company> companies = queryFactory
                .selectFrom(company)
                .where(company.deletedStatus.eq(false)) // 삭제되지 않은 데이터만 조회
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(company.count())
                .from(company)
                .where(company.deletedAt.isNull())
                .fetchFirst();

        return new PageImpl<>(companies, pageable, total);
    }
}
