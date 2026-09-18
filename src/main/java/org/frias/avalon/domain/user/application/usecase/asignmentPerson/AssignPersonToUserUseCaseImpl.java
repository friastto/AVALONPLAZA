package org.frias.avalon.domain.user.application.usecase.asignmentPerson;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.person.application.dto.request.CreatePersonRequest;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonDto;
import org.frias.avalon.domain.user.infraestructure.persistence.mapper.UserAvalonMapper;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class AssignPersonToUserUseCaseImpl implements AssignPersonToUserUseCase {

    private final UserAvalonRepositoryPort userRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;
    private final MasterDataRepositoryPort masterDataRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;
    private final UserAvalonMapper userAvalonMapper;

    @Override
    @Transactional // Súper importante para que si algo falla, no quede la persona creada sin usuario
    public UserAvalonDto execute(Long userId, CreatePersonRequest data) {

        MasterTree tree = masterTreeProvider.getTree();

        // 1. Validar que el usuario exista
        UserAvalonDomain user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        // 2. Validar que el usuario no tenga ya una persona asignada (Regla de negocio)
        if (user.getPersonId() != null) {
            throw new BusinessException("El usuario ya tiene una persona vinculada");
        }

        // 3. Validar y resolver que typeIdentificationId y sexId sean nodos semánticamente válidos
        Long typeId = data.typeIdentificationId();
        MasterRoot typeNode = (typeId != null) ? tree.getById(typeId) : null;
        if (typeNode == null || !tree.isChildOf(typeNode, "IDENT")) {
            MasterRoot ccNode = tree.getByCode("CC");
            typeId = ccNode != null ? ccNode.getId() : null;
        }

        Long sexId = data.sexId();
        MasterRoot sexNode = (sexId != null) ? tree.getById(sexId) : null;
        if (sexNode == null || !tree.isChildOf(sexNode, "GEN")) {
            MasterRoot defaultSex = tree.getByCode("SINDET");
            if (defaultSex == null) defaultSex = tree.getByCode("M");
            sexId = defaultSex != null ? defaultSex.getId() : null;
        }

        MasterRoot statusNode = tree.getByCode("ACT");
        if (statusNode == null) {
            statusNode = tree.getByCode("ACTIVO");
        }
        if (statusNode == null) {
            throw new IllegalStateException("Estado ACTIVO (ACT) no encontrado en MasterTree");
        }
        Long statusId = statusNode.getId();

        // 4. Crear el objeto de dominio de la nueva Persona (incluyendo la dirección)
        PersonDomain newPerson = PersonDomain.createBasic(
                typeId,
                data.numberid(),
                data.name(),
                data.lastName(),
                data.address(),
                sexId,
                data.phoneNumber(),
                data.email(),
                statusId
        );

        // 5. Guardar la persona en la base de datos a través de su puerto
        PersonDomain savedPerson = personRepositoryPort.save(newPerson);

        // 6. Asignar el ID de la nueva persona al objeto de dominio del usuario
        UserAvalonDomain userWithNewPerson = UserAvalonDomain.fromPersistenceAdvanced(
                user.getId(),
                savedPerson.getId(),
                user.getUserName(),
                user.getHashSalt(),
                user.getHashPassword(),
                user.getStatusId()
        );
        
        // 7. Actualizar el usuario en la base de datos
        return userAvalonMapper.toResponseWithPersonData(
                userRepositoryPort.save(userWithNewPerson),
                savedPerson, 
                tree.getById(userWithNewPerson.getStatusId())
        );
    }
}