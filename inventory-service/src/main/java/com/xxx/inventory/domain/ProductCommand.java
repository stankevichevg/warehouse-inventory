package com.xxx.inventory.domain;

import java.util.Map;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public class ProductCommand {

    private final long id;

    public ProductCommand(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public static class CreateOrUpdateProduct extends ProductCommand {

        private final String name;
        private final long price;
        private final Map<Long, Integer> articleRelations;

        public CreateOrUpdateProduct(long id, String name, long price, Map<Long, Integer> articleRelations) {
            super(id);
            this.name = name;
            this.price = price;
            this.articleRelations = articleRelations;
        }

        public String getName() {
            return name;
        }

        public long getPrice() {
            return price;
        }

        public Map<Long, Integer> getArticleRelations() {
            return articleRelations;
        }

        public boolean includesArticle(long articleId) {
            return articleRelations != null && articleRelations.containsKey(articleId);
        }

    }

}
