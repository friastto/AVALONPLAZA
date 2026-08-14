package org.frias.avalon.domain.outlet.infraestructure.config;

import org.frias.avalon.domain.outlet.domain.service.GeoUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Infrastructure Spring Configuration registering GeoUtil domain service.
 */
@Configuration
public class OutletConfig {

    @Bean
    public GeoUtil geoUtil() {
        return new GeoUtil();
    }
}
