package es.um.pds.tableros.test.arquitectura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.metrics.ArchitectureMetrics;
import com.tngtech.archunit.library.metrics.ComponentDependencyMetrics;
import com.tngtech.archunit.library.metrics.MetricsComponents;

import jakarta.validation.Valid;

// Indicamos el paquete base
@AnalyzeClasses(packages = "es.um.pds.tableros")
public class TablerosArquitecturaTest {

    // REGLAS GENERALES
    @ArchTest
    static final ArchRule ninguna_interfaz_acaba_en_impl = noClasses().that().areInterfaces().should()
            .haveSimpleNameEndingWith("Impl").because("Las interfaces no deben acabar por Impl");

    @ArchTest
    static final ArchRule codigo_sin_system_out = noClasses().that()
            .resideOutsideOfPackage("..test..").should().accessField(System.class, "out");

    @ArchTest
    static final ArchRule codigo_sin_print_stacktrace = noClasses().should().callMethod(Throwable.class,
            "printStackTrace");

    // REGLAS SOBRE CAPAS (ARQUITECTURA HEXAGONAL)
    @ArchTest
    static final ArchRule codigo_respeta_arquitectura_hexagonal = layeredArchitecture()
            .consideringAllDependencies()
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Infrastructure").definedBy("..infrastructure..")
            
            // Reglas de la flecha de dependencia
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure")
            .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer();
    
    @ArchTest
    static final ArchRule handler_anotado_por_component = classes().that().haveSimpleNameEndingWith("Handler").should()
            .beAnnotatedWith(org.springframework.stereotype.Component.class)
            .orShould().beAnnotatedWith(org.springframework.web.bind.annotation.RestControllerAdvice.class)
            .allowEmptyShould(true)
            .because("Un handler debe ser manejado por SpringBoot como Component");

    // REGLAS SOBRE NOMENCLATURA
    private static final DescribedPredicate<JavaClass> IMPLEMENTA_ALGUNA_INTERFAZ = new DescribedPredicate<>(
            "implementa al menos una interfaz") {
        @Override
        public boolean test(JavaClass javaClass) {
            return !javaClass.getInterfaces().isEmpty();
        }
    };

    @ArchTest
    static final ArchRule implementaciones_de_interfaces_acaban_en_impl = classes().that().areNotInterfaces().and()
            .areNotEnums().and(IMPLEMENTA_ALGUNA_INTERFAZ).should().haveSimpleNameEndingWith("Impl")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule repositorios_acaban_en_repository = classes().that()
            .resideInAnyPackage("..persistence..", "..output..")
            .and().areNotEnums().and().haveSimpleNameNotStartingWith("SpringData") // Excluimos interfaces SpringData
            .should().haveSimpleNameEndingWith("Repository")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule dto_acaban_en_dto = classes().that()
            .areNotEnums().and().resideInAPackage("..dto..").should().haveSimpleNameEndingWith("DTO")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule entidades_acaban_en_entity = classes().that()
            .areNotEnums().and().resideInAPackage("..entity..").should().haveSimpleNameEndingWith("Entity")
            .allowEmptyShould(true);

    // REGLAS SOBRE EL USO DE SPRINGBOOT
    static final ArchRule restcontroller_should_be_on_infrastructure_layer = classes().that()
            .areAnnotatedWith(RestController.class).should().resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule service_should_be_on_application_layer = classes().that()
            .areAnnotatedWith(Service.class).should().resideInAPackage("..application..").allowEmptyShould(true);

    @ArchTest
    static final ArchRule sin_inyeccion_autowired =
            NO_CLASSES_SHOULD_USE_FIELD_INJECTION
                    .because("Usa inyeccion por constructor: inmutable y testeable. No usar @Autowired");

    // REGLAS DE VALIDACION REST
    private static final ArchCondition<JavaMethod> METODO_REST_VALIDA_PARAMETROS = new ArchCondition<>(
            "Rest debe tener @Valid o @Validated en parámetros @RequestBody") {

        @Override
        public void check(JavaMethod metodo, ConditionEvents events) {
            boolean validaParametro = false;
            for (JavaParameter parametro : metodo.getParameters()) {
                if (parametro.isAnnotatedWith(RequestBody.class)) {
                    validaParametro = parametro.isAnnotatedWith(Valid.class)
                            || parametro.isAnnotatedWith(Validated.class)
                            || metodo.getOwner().isAnnotatedWith(Validated.class);

                    if (!validaParametro) {
                        String message = String.format("El método %s tiene un @RequestBody sin validación",
                                metodo.getFullName());
                        events.add(SimpleConditionEvent.violated(metodo, message));
                    }
                }
            }
        }
    };

    @ArchTest
    static final ArchRule api_rest_debe_validar_datos_entrada = methods().that().areDeclaredInClassesThat()
            .areAnnotatedWith(RestController.class).and().arePublic().should(METODO_REST_VALIDA_PARAMETROS);

    // REGLAS DE METRICAS
    private static final String DOMAIN = "es.um.pds.tableros.domain";
    private static final String APPLICATION = "es.um.pds.tableros.application";
    private static final String INFRASTRUCTURE = "es.um.pds.tableros.infrastructure";

    @ArchTest
    static void imprimir_todas_las_metricas(JavaClasses classes) {
        List<JavaPackage> packages = List.of(classes.getPackage(DOMAIN), classes.getPackage(APPLICATION), classes.getPackage(INFRASTRUCTURE));
        MetricsComponents<JavaClass> components = MetricsComponents.fromPackages(packages);
        ComponentDependencyMetrics metrics = ArchitectureMetrics.componentDependencyMetrics(components);

        String separator = "+" + "-".repeat(20) + "+" + "-".repeat(6)
                + "+" + "-".repeat(6) + "+" + "-".repeat(6)
                + "+" + "-".repeat(5) + "+";

        System.out.println();
        System.out.println(separator);
        System.out.printf("| %-18s | %-4s | %-4s | %-4s | %-3s |%n", "Componente", "Ca", "Ce", "A", "I");
        System.out.println(separator);

        for (String pkg : new String[]{DOMAIN, APPLICATION, INFRASTRUCTURE}) {
            String nombre = pkg.substring(pkg.lastIndexOf('.') + 1);
            int ca = metrics.getAfferentCoupling(pkg);
            int ce = metrics.getEfferentCoupling(pkg);
            double abstractas = metrics.getAbstractness(pkg);
            double inestabilidad = metrics.getInstability(pkg);

            System.out.printf("| %-18s | %-4d | %-4d | %.2f | %.2f |%n", nombre, ca, ce, abstractas, inestabilidad);
        }

        System.out.println(separator);
        System.out.println("  Ca = Quien depende de este componente");
        System.out.println("  Ce = De quien depende este componente");
        System.out.println("  A  = %Clases abstractas (0=todo impl, 1=todo abstracto)");
        System.out.println("  I  = Inestabilidad (Ce / Ca+Ce) (0=estable, 1=inestable)");
        System.out.println();
    }

    // LÍMITE DE MÉTODOS
    @ArchTest
    static void ninguna_clase_debe_tener_20_metodos_o_mas(JavaClasses clases) {
        int umbralMetodos = 20;

        List<String> clasesSuperanUmbral = clases.stream()
                .filter(clase -> !clase.isInterface() && !clase.isEnum())
                .filter(clase -> !(clase.getSimpleName().endsWith("Entity") || clase.getSimpleName().endsWith("DTO")))
                .filter(clase -> clase.getMethods().stream()
                        .filter(metodo -> metodo.getModifiers().toString().contains("PUBLIC")).count() > umbralMetodos)
                .map(clase -> String.format("  %s (%d metodos publicos)", clase.getSimpleName(), (int) clase
                        .getMethods().stream().filter(metodos -> metodos.getModifiers().toString().contains("PUBLIC")).count()))
                .toList();

        System.out.println("Clases con más de " + umbralMetodos + " métodos públicos:");
        if (clasesSuperanUmbral.isEmpty()) {
            System.out.println("  Ninguna. OK");
        } else {
            clasesSuperanUmbral.forEach(System.out::println);
            assertThat(clasesSuperanUmbral).as("Clases a refactorizar detectadas (más de %d métodos públicos):\n",
                    umbralMetodos, String.join("\n", clasesSuperanUmbral)).isEmpty();
        }
    }
}