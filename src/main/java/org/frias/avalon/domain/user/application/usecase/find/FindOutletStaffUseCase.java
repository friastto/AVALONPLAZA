package org.frias.avalon.domain.user.application.usecase.find;

import org.frias.avalon.domain.user.application.dtos.response.StaffMemberResponse;

import java.util.List;

public interface FindOutletStaffUseCase {
    List<StaffMemberResponse> execute(Long outletId);
}
