package com.xxx.inventory.service;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.jooq.Configuration;
import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.TransactionProvider;
import org.jooq.impl.DSL;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.ThreadLocalTransactionProvider;

import javax.sql.DataSource;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public class JooqPersistenceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(DataSource.class).toProvider(HikariConnectionPoolProvider.class);
    }

    @Provides
    public DSLContext dslContext(
        SQLDialect sqlDialect,
        ConnectionProvider connectionProvider,
        TransactionProvider transactionProvider
    ) {
        final Configuration config = new DefaultConfiguration()
            .set(sqlDialect)
            .set(connectionProvider)
            .set(transactionProvider);
        return DSL.using(config);
    }

    @Provides
    public ConnectionProvider connectionProvider(DataSource dataSource) {
        return new DataSourceConnectionProvider(dataSource);
    }

    @Provides
    public TransactionProvider transactionProvider(ConnectionProvider connectionProvider) {
        return new ThreadLocalTransactionProvider(connectionProvider);
    }

}
