package org.frias.avalon.domain.person.dto;

public record PersonRequestNewDto(

    String numberId,

    String name,

    String lastName,

    String address,

    Long identificationId,

    Long sexId

) {
}
