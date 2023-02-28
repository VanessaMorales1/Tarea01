package com.distribuida;

import com.distribuida.clientes.authors.AuthorRestProxy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
@Liveness
public class AuthorCheck implements HealthCheck {
@Inject
@RestClient
    AuthorRestProxy authorRestProxy;
 /*   @Override
    public HealthCheckResponse call(){

        return HealthCheckResponse
                .named("app-authors")
                .withData("time", LocalDateTime.now().toString())
                .down()
                .build();
    }*/
 public HealthCheckResponse call(){
    authorRestProxy.findAll();
    return HealthCheckResponse.named("app-authors")
            .up()
            .withData("time", LocalDateTime.now().toString())
            .build();

 }

}
