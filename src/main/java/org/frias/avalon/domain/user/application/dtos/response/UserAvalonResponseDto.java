package org.frias.avalon.domain.user.application.dtos.response;


import org.frias.avalon.domain.masterdata.application.dto.response.StatusResponseDto;

public record UserAvalonResponseDto(
    Long id,
    String userName,
    StatusResponseDto status
    ){
}
