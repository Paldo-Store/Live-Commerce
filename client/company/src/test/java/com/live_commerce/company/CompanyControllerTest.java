package com.live_commerce.company;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.live_commerce.company.application.dto.request.CompanyCreateRequest;
import com.live_commerce.company.application.dto.response.CompanyCreateResponse;
import com.live_commerce.company.application.service.CompanyService;
import com.live_commerce.company.domain.model.CompanyType;
import com.live_commerce.company.presentation.controller.CompanyController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collection;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@AutoConfigureMockMvc
@WebMvcTest(CompanyController.class)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createCompany_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        String role = "ROLE_MASTER";

        CompanyCreateRequest request = new CompanyCreateRequest(
                "테스트 업체",
                UUID.randomUUID(),
                CompanyType.A,
                "서울시 강남구",
                "123-45-67890",
                "맛있는 음식 전문점"
        );

        CompanyCreateResponse response = new CompanyCreateResponse(
                UUID.randomUUID(),
                "테스트 업체",
                UUID.randomUUID(),
                CompanyType.A,
                "서울시 강남구",
                "123-45-67890",
                "맛있는 음식 전문점"
        );

        when(companyService.createCompany(eq(request), eq(userId), eq(role)))
                .thenReturn(response);

        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        RequestUserDetails userDetails = new RequestUserDetails(userId, "테스트유저", authorities);

        // when & then
        mockMvc.perform(post("/api/v1/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(authentication(new UsernamePasswordAuthenticationToken(userDetails, null, authorities))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("테스트 업체"))
                .andExpect(jsonPath("$.data.owner").value(ownerId.toString()))
                .andExpect(jsonPath("$.data.type").value(type.name()))
                .andExpect(jsonPath("$.data.address").value("서울시 강남구"))
                .andExpect(jsonPath("$.data.number").value("123-45-67890"))
                .andExpect(jsonPath("$.data.description").value("맛있는 음식 전문점"));
    }
}