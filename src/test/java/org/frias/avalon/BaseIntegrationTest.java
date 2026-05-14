package org.frias.avalon;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional // Para que cada test se ejecute en su propia transacción y se haga rollback al finalizar
public abstract class BaseIntegrationTest {
    // Esta clase base centraliza la configuración para todas las pruebas de integración.
    // - @ExtendWith(SpringExtension.class): Integra JUnit 5 con el contexto de Spring.
    // - @SpringBootTest: Carga el contexto completo de Spring Boot.
    // - @ActiveProfiles("test"): Activa el perfil "test", que cargará application-test.yml.
    // - @Transactional: Asegura que cada método de prueba se ejecute dentro de una transacción
    //   y que esta se revierta al finalizar, dejando la base de datos limpia para el siguiente test.
    //   Esto es crucial para la independencia de las pruebas.
}
