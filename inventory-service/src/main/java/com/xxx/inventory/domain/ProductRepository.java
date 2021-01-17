package com.xxx.inventory.domain;

import com.xxx.inventory.domain.ProductCommand.CreateOrUpdateProduct;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public interface ProductRepository {

    Stream<Product> list();
    List<ProductArticle> lockProductArticles(final long productId);
    boolean lockProduct(final long productId);
    void apply(CreateOrUpdateProduct command);
}
