package com.xxx.inventory.domain;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public abstract class ArticleCommand {

    private final long id;

    public ArticleCommand(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public static class CreateOrUpdateArticle extends ArticleCommand {

        private final String name;
        private final int availableStock;

        public CreateOrUpdateArticle(long id, String name, int availableStock) {
            super(id);
            this.name = name;
            this.availableStock = availableStock;
        }

        public String getName() {
            return name;
        }

        public int getAvailableStock() {
            return availableStock;
        }
    }

}
