package org.frias.avalon.domain.notification.infrastructure.config;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.ClasspathLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PebbleConfig {

    @Bean
    public PebbleEngine pebbleEngine() {
        ClasspathLoader loader = new ClasspathLoader();
        loader.setPrefix("templates/");
        loader.setSuffix(".html");
        
        return new PebbleEngine.Builder()
                .loader(loader)
                .cacheActive(true)
                .build();
    }
}