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
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;
import org.frias.avalon.domain.user.domain.mapper.UserAvalonMapper;
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

        MasterRoot status = masterDataRepositoryPort.getActiveStatus().orElseThrow(()->new BusinessException("no se puede activar esta persona para el usuario actual"));

        // 3. Crear el objeto de dominio de la nueva Persona
        PersonDomain newPerson = PersonDomain.createBasic(
                data.typeIdentificationId(),
                data.numberid(),
                data.name(),
                data.lastName(),
                data.phoneNumber(),
                data.email(),
                status.getId()

        );

        // 4. Guardar la persona en la base de datos a través de su puerto
        // El repositorio de infraestructura debe retornar la persona con el ID generado por la BD
        PersonDomain savedPerson = personRepositoryPort.save(newPerson);

        // 5. Asignar el ID de la nueva persona al objeto de dominio del usuario

         UserAvalonDomain userWithNewPerson = UserAvalonDomain.fromPersistenceAdvanced(
                 user.getId(),
                 savedPerson.getId(),
                 user.getUserName(),
                 user.getHashSalt(),
                 user.getHashPassword(),
                 user.getStatusId()
         );
        // 6. Actualizar el usuario en la base de datos
        return userAvalonMapper.toResponseWithPersonData(userRepositoryPort.save(userWithNewPerson),savedPerson, tree.getById(userWithNewPerson.getStatusId()));
    }
}