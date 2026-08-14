package org.frias.avalon.domain.masterdata.infraestructure.config;

import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Infrastructure Spring Configuration registering MasterTreeProvider domain service.
 */
@Configuration
public class MasterDataConfig {

    @Bean(initMethod = "init")
    public MasterTreeProvider masterTreeProvider(MasterDataRepositoryPort masterPort) {
        return new MasterTreeProvider(masterPort);
    }
}
