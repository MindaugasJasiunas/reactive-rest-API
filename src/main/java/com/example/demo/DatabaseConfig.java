package com.example.demo;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

@Configuration
public class DatabaseConfig {

    @Value("classpath:/schema.sql")  //load that resource
    Resource resource;  //import org.springframework.core.io

    @Bean
    ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory){  // connectionFactory created at runtime
        ConnectionFactoryInitializer initializer= new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(resource));
//        initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));  // alternative way
        return initializer;
    }

}
