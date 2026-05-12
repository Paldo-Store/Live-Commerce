package com.live_commerce.payment;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.freeze.FreezingArchRule.freeze;

/**
 * 레이어 의존 방향 강제 테스트.
 *
 * FreezingArchRule: 최초 실행 시 현재 위반을 baseline으로 기록하고,
 * 이후 신규 위반이 추가될 때만 실패한다.
 * baseline 파일 위치: src/test/resources/archunit-violations/
 *
 * 기존 위반(RequestUserDetails, OrderClient 등)은 PDSH-TODO 이슈로 별도 처리 예정.
 */
@AnalyzeClasses(
	packages = "com.live_commerce.payment",
	importOptions = ImportOption.DoNotIncludeTests.class
)
public class ArchitectureRuleTest {

	@ArchTest
	static final ArchRule application_must_not_depend_on_infrastructure =
		freeze(noClasses().that().resideInAPackage("..application..")
			.should().dependOnClassesThat()
			.resideInAPackage("..infrastructure..")
			.because("application 레이어는 infrastructure를 직접 참조할 수 없습니다"));

	@ArchTest
	static final ArchRule domain_must_not_depend_on_application_or_infrastructure =
		freeze(noClasses().that().resideInAPackage("..domain..")
			.should().dependOnClassesThat()
			.resideInAnyPackage("..application..", "..infrastructure..")
			.because("domain 레이어는 application/infrastructure에 의존할 수 없습니다"));
}
