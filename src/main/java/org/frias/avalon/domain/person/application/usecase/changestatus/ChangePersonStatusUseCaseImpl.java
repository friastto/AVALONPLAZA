package org.frias.avalon.domain.person.application.usecase.changestatus;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.model.StatusRules;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.person.application.dto.response.PersonResponse;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.person.infraestructure.mapper.PersonMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangePersonStatusUseCaseImpl implements ChangePersonStatusUseCase {

    private final PersonRepositoryPort personPort;
    private final MasterTreeProvider treeProvider;
    private final PersonMapper mapper;

    public ChangePersonStatusUseCaseImpl(
            PersonRepositoryPort personPort,
            MasterTreeProvider treeProvider,
            PersonMapper mapper) {
        this.personPort = personPort;
        this.treeProvider = treeProvider;
        this.mapper = mapper;
    }

    @Transactional
    @Override
    public PersonResponse execute(Long idPerson, Long idStatus) {

        PersonDomain person = personPort.findById(idPerson)
                .orElseThrow(() -> new EntityNotFoundException("la persona no se encontro en la base de datos"));

        MasterTree tree = treeProvider.getTree();

        MasterRoot oldStatus = tree.getByIdOrThrow(person.getStatusId());
        MasterRoot newStatus = tree.getByIdOrThrow(idStatus);

        if (!tree.isChildOf(newStatus, "STSGEN")) {
            throw new IllegalStateException("no se puede establecer este estado");
        }


        /*/ 2. Preparar los datos para la validación
        // Asumiendo que PersonDomain tiene getEmployeeRoleCode(), getCompanyId(), getUsername()
        String targetUserRoleCode = person.getEmployeeRoleCode();
        Long targetUserCompanyId = person.getCompanyId();
        String newStatusCode = newStatus.getCode();
        String newStatusType = newStatus.getType();

        // Determinar si es un cambio sobre el propio usuario
        //boolean isSelfChange = currentUserContext.userName().equals(person.getUsername());

        // 3. Invocar la validación
        boolean isAllowed = statusChangeValidator.validate(
                currentUserContext,
                targetUserRoleCode,
                targetUserCompanyId,
                newStatusCode,
                newStatusType,
                false
        );

        // 4. Manejar el resultado
        if (!isAllowed) {
            throw new BusinessException("No tiene permisos para cambiar el estado de esta persona.");
        }
*/
        // Si la validación pasa, procede con la lógica de negocio para cambiar el estado
        StatusRules.validateTransition(oldStatus, newStatus); // Re-ubicado aquí después de la validación de permisos

        person.changeStatus(newStatus.getId()); // Suponiendo que PersonDomain tiene este método
        PersonDomain peronStatusChanged = personPort.save(person); // Guardar la instancia modificada

        return mapper.toResponse(peronStatusChanged);
    }
}
