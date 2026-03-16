package org.frias.avalon.useravalon.dtos;

public record UserLinkPersonRequestDto(
        String userName,
       String password,
       Long roleId,

       Long personId) {
}
