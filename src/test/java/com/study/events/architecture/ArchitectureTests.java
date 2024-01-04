package com.study.events.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.study.events.infrastructure.adapters.outbound.persistence.entity.AuditingEntity;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ArchitectureTests {
  private static JavaClasses classes;

  private final static String ROOT_PACKAGE = "com.study.events";
  private final static String MODEL_PACKAGE = "domain.model";
  private final static String SERVICE_PACKAGE = "domain.service";
  private final static String ADAPTERS_PACKAGE = "infrastructure.adapters";


  @BeforeAll
  public static void setup() {
    classes = new ClassFileImporter().importPackages("com.study.events");
  }

  @Test
  void testDependenciesModel() {
    var importPackage = ROOT_PACKAGE + "..";
    classes = new ClassFileImporter().importPackages(importPackage);

    checkNoDependencyFromTo(MODEL_PACKAGE, ROOT_PACKAGE, classes);
  }

  @Test
  void testAccessDependenciesService() {
    var importPackage = ROOT_PACKAGE + "..";
    classes = new ClassFileImporter().importPackages(importPackage);

    checkNoAccessFromTo(SERVICE_PACKAGE, ADAPTERS_PACKAGE, classes);
    checkNoDependencyFromTo(SERVICE_PACKAGE, ADAPTERS_PACKAGE, classes);
  }

  @Test
  void testRepositoryNameRules() {
    ArchRule servicePackageRule = classes()
        .that().resideInAPackage("..repository..")
        .should().haveNameMatching(".*Repository");
    servicePackageRule.check(classes);
  }

  @Test
  void testRepositoryTypeRules() {
    ArchRule servicePackageRule = classes()
        .that().resideInAPackage("..repository..")
        .should().beInterfaces();
    servicePackageRule.check(classes);
  }

  @Test
  void testAdapterImpl() {
    ArchRule servicePackageRule = classes()
        .that().resideInAPackage("..persistence")
        .should().implement(JavaClass.Predicates.resideInAPackage("..outbound.."));
    servicePackageRule.check(classes);
  }

  @Test
  void testServiceImpl() {
    ArchRule servicePackageRule = classes()
        .that().resideInAPackage("..service")
        .should().implement(JavaClass.Predicates.resideInAPackage("..inbound.."));
    servicePackageRule.check(classes);
  }

  @Test
  void testEntitites() {
    ArchRule entitiesRule = classes().that().resideInAPackage("..entity").should().beAssignableTo(
        AuditingEntity.class );
    entitiesRule.check(classes);
  }

  private void checkNoDependencyFromTo(
      String fromPackage, String toPackage, JavaClasses classesToCheck) {
    noClasses()
        .that()
        .resideInAPackage(fullyQualified(fromPackage))
        .should()
        .dependOnClassesThat()
        .resideInAPackage(fullyQualified(toPackage))
        .check(classesToCheck);
  }

  private void checkNoAccessFromTo(
      String fromPackage, String toPackage, JavaClasses classesToCheck) {
    noClasses()
        .that()
        .resideInAPackage(fullyQualified(fromPackage))
        .should()
        .accessClassesThat()
        .resideInAPackage(fullyQualified(toPackage))
        .check(classesToCheck);
  }

  private String fullyQualified(String packageName) {
    return ROOT_PACKAGE + '.' + packageName;
  }
}
