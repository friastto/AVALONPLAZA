package org.frias.avalon.domain.usergeneral.useravalon.dtos;

public record UserLinkPersonRequestDto(
        String userName,
       String password,
       Long roleId,

       Long personId) {
}
