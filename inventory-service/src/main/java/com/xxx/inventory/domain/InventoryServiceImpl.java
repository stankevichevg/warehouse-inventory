package com.xxx.inventory.domain;

import com.google.inject.Inject;
import com.xxx.inventory.domain.ArticleCommand.CreateOrUpdateArticle;
import com.xxx.inventory.domain.InventoryException.InsufficientArticlesInStockException;
import com.xxx.inventory.domain.InventoryException.ProductNotFoundException;
import com.xxx.inventory.domain.InventoryException.WrongStockQuantityException;
import com.xxx.inventory.domain.ProductCommand.CreateOrUpdateProduct;
import org.jooq.DSLContext;

import java.util.stream.Stream;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public class InventoryServiceImpl implements InventoryService {

    private final DSLContext db;

    private final ArticleRepository articleRepository;
    private final ProductRepository productRepository;

    @Inject
    public InventoryServiceImpl(DSLContext create, ArticleRepository articleRepository, ProductRepository productRepository) {
        this.db = create;
        this.articleRepository = articleRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Stream<Product> listProducts() {
        return productRepository.list();
    }

    @Override
    public void sellProduct(final long productId) throws InventoryException {
        db.transaction(() -> {
            if (!productRepository.lockProduct(productId)) {
                throw new ProductNotFoundException();
            }
            for (ProductArticle pa : productRepository.lockProductArticles(productId)) {
                final int updatedAvailableStock = pa.getArticle().getAvailableStock() - pa.getQuantity();
                if (updatedAvailableStock >= 0) {
                    articleRepository.save(new Article(
                        pa.getArticle().getId(), pa.getArticle().getName(), updatedAvailableStock)
                    );
                } else {
                    throw new InsufficientArticlesInStockException();
                }
            }
        });
    }

    @Override
    public void changeArticle(ArticleCommand command) {
        if (command instanceof CreateOrUpdateArticle) {
            createOrUpdateArticle((CreateOrUpdateArticle) command);
        } else {
            throw new IllegalArgumentException("Unknown command type: " + command.getClass().getSimpleName());
        }
    }

    @Override
    public void changeProduct(ProductCommand command) {
        if (command instanceof CreateOrUpdateProduct) {
            createOrUpdateProduct((CreateOrUpdateProduct) command);
        } else {
            throw new IllegalArgumentException("Unknown command type: " + command.getClass().getSimpleName());
        }
    }

    private void createOrUpdateArticle(CreateOrUpdateArticle command) {
        if (command.getAvailableStock() < 0) {
            throw new WrongStockQuantityException();
        }
        db.transaction(() -> {
            articleRepository.lockArticle(command.getId());
            articleRepository.save(new Article(command.getId(), command.getName(), command.getAvailableStock()));
        });
    }

    private void createOrUpdateProduct(CreateOrUpdateProduct command) {
        db.transaction(() -> {
            productRepository.lockProduct(command.getId());
            productRepository.apply(command);
        });
    }

}
