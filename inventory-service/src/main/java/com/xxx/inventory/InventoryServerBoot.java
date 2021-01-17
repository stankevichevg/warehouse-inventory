package com.xxx.inventory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.xxx.inventory.service.InventoryServiceModule;

import java.io.IOException;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public class InventoryServerBoot {

    public static void main(String[] args) throws IOException, InterruptedException {
        new InventoryServerBoot().startApplication();
    }

    public void startApplication() throws IOException, InterruptedException {
        final Injector injector = Guice.createInjector(new InventoryServiceModule());
        final GrpcServer server = injector.getInstance(GrpcServer.class);
        server.start();
    }

}
