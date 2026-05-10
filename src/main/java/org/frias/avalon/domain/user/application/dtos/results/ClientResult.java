package org.frias.avalon.domain.user.application.dtos.results;

import java.util.List;
import java.util.Set;

public record ClientResult (
        String type,
    boolean status,
    List<String> permissions
){
}
