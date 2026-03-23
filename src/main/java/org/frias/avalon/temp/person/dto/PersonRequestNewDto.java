package org.frias.avalon.temp.person.dto;

public record PersonRequestNewDto(

String numberId,

String name,

String lastName,

String address,

Long identificationId,

Long sexId

) {
}
