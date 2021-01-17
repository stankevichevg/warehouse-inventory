package com.xxx.inventory.domain;

import java.util.List;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public class Product {

    private final long id;
    private final String name;
    private final long price;
    private final List<ProductArticle> articles;

    public Product(long id, String name, long price, List<ProductArticle> articles) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.articles = articles;
    }

    public int calculateQuantity() {
        return articles.stream()
            .mapToInt(component -> component.getArticle().getAvailableStock() / component.getQuantity())
            .min().orElse(0);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getPrice() {
        return price;
    }

    public List<ProductArticle> getArticles() {
        return articles;
    }
}
