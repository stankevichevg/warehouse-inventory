package com.xxx.inventory.domain;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public class InventoryException extends RuntimeException {

    public static class InsufficientArticlesInStockException extends InventoryException {
    }

    public static class ProductNotFoundException extends InventoryException {
    }

    public static class ArticleNotFoundException extends InventoryException {
    }

    public static class WrongStockQuantityException extends InventoryException {
    }
}
