package org.frias.avalon.domain.user.domain.dtos.request;

public interface BaseNewUserDto {
    String userName();
    String password();
    Long roleId();
    Long companyId();
}
