package com.xxx.inventory.uploader.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public class ArticleModel {

    @JsonProperty("art_id")
    private long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("stock")
    private int stock;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
