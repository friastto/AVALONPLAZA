package org.frias.avalon.domain.user.application.usecase.asignmentPerson;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
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
    private final MasterTreeProvider masterTreeProvider;
    private final UserAvalonMapper userAvalonMapper;

    @Override
    @Transactional
    public UserAvalonDto execute(Long userId, CreatePersonRequest data) {

        MasterTree tree = masterTreeProvider.getTree();

        // 1. Validar que el usuario exista
        UserAvalonDomain user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        // 2. Validar que el usuario no tenga ya una persona asignada (Regla de negocio)
        if (user.getPersonId() != null) {
            throw new BusinessException("El usuario ya tiene una persona vinculada");
        }

        // 3. Validar y resolver que typeIdentificationId y sexId sean nodos semanticamente validos
        Long typeId = data.typeIdentificationId();
        if (typeId == null && data.typeIdentificationCode() != null && !data.typeIdentificationCode().isBlank()) {
            MasterRoot node = tree.getByCode(data.typeIdentificationCode().trim().toUpperCase());
            if (node != null) {
                typeId = node.getId();
            }
        }
        MasterRoot typeNode = (typeId != null) ? tree.getById(typeId) : null;
        if (typeNode == null || !tree.isChildOf(typeNode, "IDENT")) {
            throw new BusinessException("Tipo de identificacion invalido o no proporcionado");
        }

        Long sexId = data.sexId();
        if (sexId == null && data.sexCode() != null && !data.sexCode().isBlank()) {
            MasterRoot node = tree.getByCode(data.sexCode().trim().toUpperCase());
            if (node != null) {
                sexId = node.getId();
            }
        }
        if (sexId != null) {
            MasterRoot sexNode = tree.getById(sexId);
            if (sexNode == null || !tree.isChildOf(sexNode, "GEN")) {
                throw new BusinessException("Genero invalido");
            }
        }

        MasterRoot statusNode = tree.getByCodeOrThrow("ACT");
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