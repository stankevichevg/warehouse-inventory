package com.xxx.inventory.uploader.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public class ProductArticleModel {

    @JsonProperty("art_id")
    private long id;

    @JsonProperty("amount_of")
    private int quantity;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
