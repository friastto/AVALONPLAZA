package org.frias.avalon.domain.company.dtos;


import jakarta.validation.constraints.NotBlank;

record EmpresaDto (

        @NotBlank(message = "La empresa no piuede estar sin name")
    String nombre,

        @NotBlank(message = "La empresa debe tener un nit")
    String nit,

        @NotBlank(message = "La empresa debe tener un email de contacto")
    String email,

        @NotBlank(message = "La empresa debe tener una direccion de correspondencia fisica")
    String address

){}
