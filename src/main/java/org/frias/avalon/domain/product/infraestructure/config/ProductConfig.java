package org.frias.avalon.domain.product.infraestructure.config;

import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.product.domain.service.UnitConversionService;
import org.frias.avalon.domain.product.domain.service.UnitConversionServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Infrastructure Spring Configuration registering UnitConversionService domain service.
 */
@Configuration
public class ProductConfig {

    @Bean
    public UnitConversionService unitConversionService(MasterTreeProvider masterTreeProvider) {
        return new UnitConversionServiceImpl(masterTreeProvider);
    }
}
