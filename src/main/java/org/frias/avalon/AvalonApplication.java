package org.frias.avalon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@SpringBootApplication
// 👈 ASEGÚRATE QUE ESTE SEA EL PADRE DE TODO
//@EnableAspectJAutoProxy
public class AvalonApplication {

	public static void main(String[] args) {
		SpringApplication.run(AvalonApplication.class, args);
	}

}
