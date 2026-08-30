package org.frias.avalon.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Disabled;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Automated ArchUnit Architecture verification test suite for pure Clean Architecture & DDD compliance.
 */
@Disabled("ArchUnit 1.3.0 does not yet support Java 25 bytecode (class file major version 69)")
// @AnalyzeClasses(packages = "org.frias.avalon", importOptions = ImportOption.DoNotIncludeTests.class)
public class CleanArchitectureRulesTest {

    @ArchTest
    public static final ArchRule domain_should_not_depend_on_infrastructure_or_presentation =
            noClasses()
                    .that().resideInAPackage("org.frias.avalon.domain..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..infraestructure..", "..infrastructure..", "..presentation..", "org.springframework..")
                    .allowEmptyShould(true);

    @ArchTest
    public static final ArchRule application_should_not_depend_on_presentation =
            noClasses()
                    .that().resideInAPackage("org.frias.avalon.domain..application..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..presentation..")
                    .allowEmptyShould(true);
}
