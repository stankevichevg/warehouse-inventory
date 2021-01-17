package com.xxx.inventory.service;

import com.google.inject.Provider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public class HikariConnectionPoolProvider implements Provider<DataSource> {

    private final HikariConfig config;

    public HikariConnectionPoolProvider() {
        this.config = new HikariConfig();
        this.config.setJdbcUrl(System.getenv("DB_URL"));
        this.config.setUsername(System.getenv("DB_USER"));
        this.config.setPassword(System.getenv("DB_PASSWORD"));
    }

    @Override
    public DataSource get() {
        return new HikariDataSource(config);
    }
}
