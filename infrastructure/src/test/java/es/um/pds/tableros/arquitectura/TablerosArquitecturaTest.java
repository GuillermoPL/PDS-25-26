package es.um.pds.tableros.arquitectura;

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

/**
 * @brief Pruebas de Cumplimiento Arquitectónico mediante ArchUnit.
 * Esta clase analiza de forma reflexiva las dependencias del bytecode del proyecto para garantizar 
 * la estricta separación de capas de la Arquitectura Hexagonal, el cumplimiento de las convenciones 
 * de nomenclatura DDD, restricciones de inyección y políticas de validación REST.
 */
@AnalyzeClasses(packages = "es.um.pds.tableros")
public class TablerosArquitecturaTest {

    // REGLAS GENERALES
    
    /** * @brief Garantiza que las interfaces mantengan un nombre conceptual puro y no utilicen el sufijo técnico Impl. */
    @ArchTest
    static final ArchRule ninguna_interfaz_acaba_en_impl = noClasses().that().areInterfaces().should()
            .haveSimpleNameEndingWith("Impl").because("Las interfaces no deben acabar por Impl");

    /** * @brief Prohíbe el uso de System.out en el código productivo para forzar el uso de loggers (Slf4j). Excluye paquetes de test. */
    @ArchTest
    static final ArchRule codigo_sin_system_out = noClasses().that()
            // Excluimos esta clase de test para que pueda imprimir las métricas
            .resideOutsideOfPackages("..test..", "..arquitectura..").should().accessField(System.class, "out");

    /** * @brief Evita malas prácticas de depuración prohibiendo llamadas directas a printStackTrace() sobre excepciones. */
    @ArchTest
    static final ArchRule codigo_sin_print_stacktrace = noClasses().should().callMethod(Throwable.class,
            "printStackTrace");

    // REGLAS SOBRE CAPAS (ARQUITECTURA HEXAGONAL)
    
    /** * @brief Regla central de control de la Arquitectura Hexagonal.
     * Define las capas de Dominio, Aplicación e Infraestructura y valida que la dirección de las dependencias 
     * sea estrictamente entrante: El dominio es agnóstico, la aplicación orquesta el dominio e infraestructura 
     * depende de ambas sin que ninguna capa interna conozca detalles de la base de datos o interfaces.
     */
    @ArchTest
    static final ArchRule codigo_respeta_arquitectura_hexagonal = layeredArchitecture()
            .consideringAllDependencies()
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Infrastructure").definedBy("..infrastructure..")
            
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure")
            .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer();
    
    /** * @brief Exige que cualquier componente de captura de excepciones o eventos (Handler) esté registrado en el contenedor IoC. */
    @ArchTest
    static final ArchRule handler_anotado_por_component = classes().that().haveSimpleNameEndingWith("Handler").should()
            .beAnnotatedWith(org.springframework.stereotype.Component.class)
            .orShould().beAnnotatedWith(org.springframework.web.bind.annotation.RestControllerAdvice.class)
            .allowEmptyShould(true)
            .because("Un handler debe ser manejado por SpringBoot como Component");

    // REGLAS SOBRE NOMENCLATURA
    
    /** * @brief Predicado descriptivo para filtrar clases que actúen como implementaciones de contratos. */
    private static final DescribedPredicate<JavaClass> IMPLEMENTA_ALGUNA_INTERFAZ = new DescribedPredicate<>(
            "implementa al menos una interfaz") {
        @Override
        public boolean test(JavaClass javaClass) {
            return !javaClass.getInterfaces().isEmpty();
        }
    };

    /** * @brief Fuerza a que los adaptadores de infraestructura que realizan contratos acaben con el sufijo Impl, obviando elementos de configuración o UI. */
    @ArchTest
    static final ArchRule implementaciones_de_interfaces_acaban_en_impl = classes().that()
            .areNotInterfaces()
            .and().areNotEnums()
            .and(IMPLEMENTA_ALGUNA_INTERFAZ)
            // 1. En DDD, el dominio no usa Impl
            .and().resideOutsideOfPackage("..domain..")
            // 2. AÑADIDO: Excepciones legítimas de frameworks (Spring/JavaFX)
            .and().haveSimpleNameNotEndingWith("Controller")
            .and().haveSimpleNameNotEndingWith("Config")
            .and().haveSimpleNameNotEndingWith("Interceptor")
            .should().haveSimpleNameEndingWith("Impl")
            .allowEmptyShould(true);

    /** * @brief Asegura la homogeneidad de nombres exigiendo que los repositorios de persistencia o salida terminen en 'Repository'. */
    @ArchTest
    static final ArchRule repositorios_acaban_en_repository = classes().that()
            .resideInAnyPackage("..persistence..", "..output..")
            // AÑADIDO: Excluimos expresamente la carpeta de entidades
            .and().resideOutsideOfPackage("..entity..")
            .and().areNotEnums().and().haveSimpleNameNotStartingWith("SpringData") 
            .and().haveSimpleNameNotEndingWith("Impl") 
            .should().haveSimpleNameEndingWith("Repository")
            .allowEmptyShould(true);

    /** * @brief Controla la nomenclatura uniforme de los objetos de transferencia de datos. */
    @ArchTest
    static final ArchRule dto_acaban_en_dto = classes().that()
            .areNotEnums().and().resideInAPackage("..dto..").should().haveSimpleNameEndingWith("DTO")
            .allowEmptyShould(true);

    /** * @brief Controla la nomenclatura de los mapeos relacionales JPA excluyendo los componentes incrustables (Embeddable). */
    @ArchTest
    static final ArchRule entidades_acaban_en_entity = classes().that()
            .areNotEnums().and().resideInAPackage("..entity..")
            .and().haveSimpleNameNotEndingWith("Embeddable") // Los Embeddable no son Entities puras
            .should().haveSimpleNameEndingWith("Entity")
            .allowEmptyShould(true);

    // REGLAS SOBRE EL USO DE SPRINGBOOT
    
    /** * @brief Protege las capas internas asegurando que los controladores REST residan exclusivamente en infraestructura. */
    @ArchTest
    static final ArchRule restcontroller_should_be_on_infrastructure_layer = classes().that()
            .areAnnotatedWith(RestController.class).should().resideInAPackage("..infrastructure..");

    /** * @brief Garantiza que los estereotipos de servicio queden confinados en la capa de aplicación. */
    @ArchTest
    static final ArchRule service_should_be_on_application_layer = classes().that()
            .areAnnotatedWith(Service.class).should().resideInAPackage("..application..").allowEmptyShould(true);

    /** * @brief Prohíbe terminantemente la inyección por campo (@Autowired en atributos) promoviendo la inyección por constructor. */
    @ArchTest
    static final ArchRule sin_inyeccion_autowired =
            NO_CLASSES_SHOULD_USE_FIELD_INJECTION
                    .because("Usa inyeccion por constructor: inmutable y testeable. No usar @Autowired");

    // REGLAS DE VALIDACION REST
    
    /** * @brief Condición a medida para auditar de forma reflectiva que los métodos de entrada web validen estructuralmente los payloads JSON. */
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

    /** * @brief Fuerza normativamente a los endpoints expuestos por los @RestController públicos a adjuntar anotaciones de validación. */
    @ArchTest
    static final ArchRule api_rest_debe_validar_datos_entrada = methods().that().areDeclaredInClassesThat()
            .areAnnotatedWith(RestController.class).and().arePublic().should(METODO_REST_VALIDA_PARAMETROS);

    // REGLAS DE METRICAS
    private static final String DOMAIN = "es.um.pds.tableros.domain";
    private static final String APPLICATION = "es.um.pds.tableros.application";
    private static final String INFRASTRUCTURE = "es.um.pds.tableros.infrastructure";

    /**
     * @brief Test de diagnóstico que calcula e imprime métricas de acoplamiento de Robert C. Martin.
     * Computa el acoplamiento aferente (Ca), eferente (Ce), la abstracción (A) y la inestabilidad (I) 
     * de los paquetes estructurales imprimiendo una tabla formateada por consola.
     * @param classes Catálogo reflectivo de clases analizadas de la aplicación.
     */
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
    
    /**
     * @brief Test de control de cohesión y diseño limpio (Anti-Bloat rule).
     * Evalúa que ninguna clase operativa o de servicio supere un umbral estricto de métodos públicos (35), 
     * previniendo la aparición de clases Dios y promoviendo la refactorización constructiva.
     * @param clases Catálogo de clases a evaluar.
     */
    @ArchTest
    static void ninguna_clase_debe_tener_20_metodos_o_mas(JavaClasses clases) {
        int umbralMetodos = 35; // Ampliado para Agregados Ricos en DDD

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