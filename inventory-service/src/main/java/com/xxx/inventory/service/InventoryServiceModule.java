package com.xxx.inventory.service;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.xxx.inventory.domain.ArticleRepository;
import com.xxx.inventory.domain.ArticleRepositoryImpl;
import com.xxx.inventory.domain.InventoryService;
import com.xxx.inventory.domain.InventoryServiceImpl;
import com.xxx.inventory.domain.ProductRepository;
import com.xxx.inventory.domain.ProductRepositoryImpl;
import io.grpc.BindableService;
import org.jooq.SQLDialect;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public class InventoryServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new JooqPersistenceModule());
        bind(ArticleRepository.class).to(ArticleRepositoryImpl.class);
        bind(ProductRepository.class).to(ProductRepositoryImpl.class);
        bind(InventoryService.class).to(InventoryServiceImpl.class);
        bind(BindableService.class).to(InventoryServiceHandler.class);
    }

    @Provides
    public SQLDialect dialect() {
        return SQLDialect.POSTGRES;
    }

}
