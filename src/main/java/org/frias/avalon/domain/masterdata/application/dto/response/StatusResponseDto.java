package org.frias.avalon.domain.masterdata.application.dto.response;

import lombok.Getter;

@Getter
public class StatusResponseDto {
    private final Long id;
    private final String code;
    private final String name;
    private final boolean active;

    public StatusResponseDto(Long id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.active = "ACT".equals(code);
    }

    public boolean isActive() {
        return "ACT".equals(this.code);
    }

}
