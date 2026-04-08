package org.frias.avalon.domain.constumer.service;

import org.frias.avalon.domain.outlet.dtos.request.OutletMap;
import org.frias.avalon.domain.outlet.dtos.response.OutletDto;

import java.util.List;

public interface ConsumerService {

    List<OutletDto> nearbyAllOutlets(OutletMap request);




}
