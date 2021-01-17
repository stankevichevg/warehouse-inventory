package com.xxx.inventory.uploader.model;

import java.util.List;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public class InventoryCollectionModel {

    private List<ArticleModel> inventory;

    public List<ArticleModel> getInventory() {
        return inventory;
    }

    public void setInventory(List<ArticleModel> inventory) {
        this.inventory = inventory;
    }
}
