package org.frias.avalon.domain.user.application.usecase.find;

import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;
import org.frias.avalon.domain.user.infraestructure.persistence.mapper.UserAvalonMapper;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

@Component
public class GetAllUserAvalonUseCaseImpl implements GetAllUserAvalonUseCase {

    private final UserAvalonRepositoryPort userPort;
    private final UserAvalonMapper userAvalonMapper;

    private final MasterTreeProvider masterTreeProvider;

    public GetAllUserAvalonUseCaseImpl(UserAvalonRepositoryPort userPort, UserAvalonMapper userAvalonMapper, MasterTreeProvider masterTreeProvider) {
        this.userPort = userPort;
        this.userAvalonMapper = userAvalonMapper;

        this.masterTreeProvider = masterTreeProvider;
    }

    @Override
    public List<UserAvalonResponseDto> execute() {

        var tree = masterTreeProvider.getTree();

        List<UserAvalonResponseDto> result = userPort.getAll().stream()
                .flatMap(ua -> {
                    var status = tree.getById(ua.getStatusId());

                    if (status == null) {
                        // log
                        return Stream.empty();
                    }

                    return Stream.of(userAvalonMapper.toResponse(ua, status));
                })
                .toList();

        return result;


    }
}
