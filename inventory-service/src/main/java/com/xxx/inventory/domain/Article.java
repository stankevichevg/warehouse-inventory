package com.xxx.inventory.domain;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public class Article {

    private final long id;
    private final String name;
    private final int availableStock;

    public Article(long id, String name, int availableStock) {
        this.id = id;
        this.name = name;
        this.availableStock = availableStock;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAvailableStock() {
        return availableStock;
    }
}
