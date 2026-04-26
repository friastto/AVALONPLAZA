package org.frias.avalon.domain.masterdata.domain.model;

import org.frias.avalon.core.exeptions.DomainValidationException;

import java.util.Map;
import java.util.Set;

public class StatusRules {

    public static final String ACT = "ACT";
    public static final String INA = "INA";
    public static final String SUS = "SUS";
    public static final String DEL = "DEL";

    static Map<String, Set<String>> transitions = Map.of(
            "ACT", Set.of("INA", "SUS", "DEL"),
            "INA", Set.of("ACT", "DEL"),
            "SUS", Set.of("ACT", "DEL"),
            "DEL", Set.of()
    );


    public static void validateTransition(MasterRoot current, MasterRoot next) {

        if (current.getShortName().equals(next.getShortName())) {
            throw new DomainValidationException("Ya tiene ese estado");
        }

        Set<String> allowed = transitions.get(current.getShortName());

        if (allowed == null) {
            throw new DomainValidationException("Estado actual inválido: " + current);
        }

        if (!allowed.contains(next.getShortName())) {
            throw new DomainValidationException(
                    "Transición no permitida: " + current + " --> " + next
            );
        }


    }

}