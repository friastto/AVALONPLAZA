package org.frias.avalon.domain.pqrs.application.dto.response;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterRefDto;
import org.frias.avalon.domain.pqrs.domain.PqrsDomain;
import org.springframework.stereotype.Component;

@Component
public class PqrsResponseMapper {

    public PqrsResponse toResponse(PqrsDomain domain, String userName, String respondedByUserName) {
        if (domain == null) return null;

        String typeCode = normalizeTypeCode(domain.getTypeCode());
        String typeName = resolveTypeName(typeCode);
        MasterRefDto typeRef = new MasterRefDto(null, typeCode, typeName);

        String statusCode = domain.getStatusCode() != null ? domain.getStatusCode().toUpperCase() : "PEN";
        String statusName = resolveStatusName(statusCode);
        MasterRefDto statusRef = new MasterRefDto(null, statusCode, statusName);

        return PqrsResponse.builder()
                .id(domain.getId())
                .ticketNumber(domain.getTicketNumber())
                .userId(domain.getUserId())
                .userName(userName)
                .orderId(domain.getOrderId())
                .storeId(domain.getStoreId())
                .typeCode(typeCode)
                .typeName(typeName)
                .type(typeRef)
                .statusCode(statusCode)
                .statusName(statusName)
                .status(statusRef)
                .subject(domain.getSubject())
                .description(domain.getDescription())
                .contactEmail(domain.getContactEmail())
                .contactPhone(domain.getContactPhone())
                .adminNotes(domain.getAdminNotes())
                .respondedByUserId(domain.getRespondedByUserId())
                .respondedByUserName(respondedByUserName)
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public static String normalizeTypeCode(String code) {
        if (code == null) return "PET";
        String upper = code.trim().toUpperCase();
        if (upper.startsWith("PET")) return "PET";
        if (upper.startsWith("QUE")) return "QUE";
        if (upper.startsWith("REC")) return "REC";
        if (upper.startsWith("SUG")) return "SUG";
        return upper;
    }

    public static String resolveTypeName(String code) {
        if (code == null) return "Peticion";
        return switch (code.toUpperCase()) {
            case "PET" -> "Peticion";
            case "QUE" -> "Queja";
            case "REC" -> "Reclamo";
            case "SUG" -> "Sugerencia";
            default -> code;
        };
    }

    public static String resolveStatusName(String code) {
        if (code == null) return "Pendiente";
        return switch (code.toUpperCase()) {
            case "PEN" -> "Pendiente";
            case "PRO" -> "En Proceso";
            case "RES" -> "Resuelto";
            case "CER" -> "Cerrado";
            default -> code;
        };
    }
}
