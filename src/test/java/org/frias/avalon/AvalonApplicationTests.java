package org.frias.avalon;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

// Agregamos la propiedad 'app.jwt.secret' (o JWT_SECRET_KEY) directamente aquí
// para que el test no falle buscando la variable de entorno real.
@SpringBootTest
class AvalonApplicationTests {


	@Test
	void contextLoads() {
	}

}
