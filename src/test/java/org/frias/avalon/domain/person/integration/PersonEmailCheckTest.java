package org.frias.avalon.domain.person.integration;

import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
public class PersonEmailCheckTest {

    @Autowired
    private PersonRepositoryPort personRepositoryPort;

    @Test
    public void checkEmails() {
        System.out.println("--- STARTING DATABASE PERSONS CHECK ---");
        
        // Busquemos a algunos clientes comunes.
        // El cliente genérico suele tener cédula "1" o similar.
        // Busquemos por ejemplo el cliente con cédula "123456" o "1" o "10203040"
        String[] testIds = {"1", "123456", "10203040", "12345678"};
        
        for (String id : testIds) {
            Optional<PersonDomain> person = personRepositoryPort.findByNumberid(id);
            if (person.isPresent()) {
                System.out.println("Person found - ID: " + id + 
                                   ", Name: " + person.get().getFullName() + 
                                   ", Email: " + person.get().getEmail());
            } else {
                System.out.println("Person with ID: " + id + " not found in DB.");
            }
        }
        
        System.out.println("--- DATABASE PERSONS CHECK FINISHED ---");
    }
}
