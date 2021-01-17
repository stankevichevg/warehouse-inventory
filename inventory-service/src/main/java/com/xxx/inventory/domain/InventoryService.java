package com.xxx.inventory.domain;

import com.xxx.inventory.domain.InventoryException.WrongStockQuantityException;

import java.util.stream.Stream;

/**
 * Inventory domain service. Contains all business procedures for the domain.
 *
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public interface InventoryService {

    /**
     * Streams all products sorted by id.
     *
     * @return stream of all products
     */
    Stream<Product> listProducts();

    /**
     * Executes selling of a product with the given id. Decrease number of related articles.
     * Method should ensure data consistency using appropriate locking on articles.
     *
     * @param productId identity of a product to sell
     * @throws InventoryException if something went wrong (see subclasses of the exception)
     */
    void sellProduct(long productId) throws InventoryException;

    /**
     * Executing the given command applying a change for an article.
     *
     * @param command command to execute
     * @throws WrongStockQuantityException if an wrong stock quantity has been provided
     */
    void changeArticle(ArticleCommand command) throws WrongStockQuantityException;

    /**
     * Executing the given command applying a change for a product.
     *
     * @param command command to execute
     */
    void changeProduct(ProductCommand command);

}
