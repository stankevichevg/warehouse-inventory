package com.xxx.inventory;

import com.google.inject.Inject;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static java.lang.Integer.getInteger;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public class GrpcServer {

    public static final String SERVER_PORT_PROP = "service.port";
    public static final int SERVER_PORT = getInteger(SERVER_PORT_PROP, 50051);

    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcServer.class);

    private final BindableService bindableService;

    private Server server;

    @Inject
    public GrpcServer(BindableService bindableService) {
        this.bindableService = bindableService;
    }

    public void start() throws IOException, InterruptedException {
        server = ServerBuilder.forPort(SERVER_PORT)
            .addService(bindableService)
            .build()
            .start();
        LOGGER.info("Server started, listening on " + SERVER_PORT);
        setUpShutdownHook();
        blockUntilShutdown();
    }

    private void setUpShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    GrpcServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

}
