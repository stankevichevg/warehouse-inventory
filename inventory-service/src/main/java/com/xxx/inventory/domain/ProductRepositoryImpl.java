package com.xxx.inventory.domain;

import com.google.inject.Inject;
import com.xxx.inventory.domain.ProductCommand.CreateOrUpdateProduct;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectConditionStep;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.xxx.inventory.domain.ArticleRepositoryImpl.toArticle;
import static com.xxx.inventory.sql.Tables.ARTICLE;
import static com.xxx.inventory.sql.Tables.PRODUCT;
import static com.xxx.inventory.sql.Tables.PRODUCT_HAS_ARTICLE;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public class ProductRepositoryImpl implements ProductRepository {

    private static final int PRODUCT_FETCH_SIZE = 100;

    private final DSLContext db;
    private final ArticleRepository articleRepository;

    @Inject
    public ProductRepositoryImpl(DSLContext db, ArticleRepository articleRepository) {
        this.db = db;
        this.articleRepository = articleRepository;
    }

    @Override
    public Stream<Product> list() {
        return db.select().from(PRODUCT).orderBy(PRODUCT.ID)
            .fetchSize(PRODUCT_FETCH_SIZE).fetchStreamInto(PRODUCT)
            .map(this::toProduct);
    }

    @Override
    public List<ProductArticle> lockProductArticles(final long productId) {
        return selectProductArticles(productId)
            .orderBy(ARTICLE.ID)
            .forUpdate()
            .fetchStream()
            .map(record -> new ProductArticle(toArticle(record), record.get(PRODUCT_HAS_ARTICLE.QUANTITY)))
            .collect(toList());
    }

    @Override
    public boolean lockProduct(long productId) {
        return db.select().from(PRODUCT).where(PRODUCT.ID.eq(productId)).forUpdate().execute() > 0;
    }

    @Override
    public void apply(CreateOrUpdateProduct productSpec) {
        db.insertInto(PRODUCT)
            .columns(PRODUCT.ID, PRODUCT.NAME, PRODUCT.PRICE)
            .values(productSpec.getId(), productSpec.getName(), productSpec.getPrice())
            .onDuplicateKeyUpdate()
            .set(PRODUCT.NAME, productSpec.getName())
            .set(PRODUCT.PRICE, productSpec.getPrice())
            .execute();
        final List<ProductArticle> productArticles = lockProductArticles(productSpec.getId());
        final Map<Long, ProductArticle> currentArticles = productArticles.stream()
            .collect(toMap(a -> a.getArticle().getId(), a -> a));
        // save quantities on changed relations and delete absent relations
        for (ProductArticle productArticle : productArticles) {
            final long articleId = productArticle.getArticle().getId();
            if (productSpec.includesArticle(articleId)) {
                final int newQuantity = productSpec.getArticleRelations().get(articleId);
                if (newQuantity != productArticle.getQuantity()) {
                    updateProductArticleQuantity(productSpec.getId(), articleId, newQuantity);
                }
            } else {
                unlinkArticleFromProduct(productSpec.getId(), articleId);
            }
        }
        // insert new product relations
        productSpec.getArticleRelations().keySet().stream().sorted()
            .filter(articleId -> !currentArticles.containsKey(articleId))
            .forEach(articleId -> {
                articleRepository.lockArticle(articleId);
                linkArticleToProduct(productSpec.getId(), articleId, productSpec.getArticleRelations().get(articleId));
            });
    }

    private void linkArticleToProduct(long productId, long articleId, Integer quantity) {
        db.insertInto(PRODUCT_HAS_ARTICLE)
            .columns(PRODUCT_HAS_ARTICLE.PRODUCT_ID, PRODUCT_HAS_ARTICLE.ARTICLE_ID, PRODUCT_HAS_ARTICLE.QUANTITY)
            .values(productId, articleId, quantity)
            .execute();
    }

    private void updateProductArticleQuantity(long productId, long articleId, int quantity) {
        db.update(PRODUCT_HAS_ARTICLE)
            .set(PRODUCT_HAS_ARTICLE.QUANTITY, quantity)
            .where(PRODUCT_HAS_ARTICLE.PRODUCT_ID.eq(productId))
            .and(PRODUCT_HAS_ARTICLE.ARTICLE_ID.eq(articleId))
            .execute();
    }

    private void unlinkArticleFromProduct(long productId, long articleId) {
        db.delete(PRODUCT_HAS_ARTICLE)
            .where(PRODUCT_HAS_ARTICLE.PRODUCT_ID.eq(productId))
            .and(PRODUCT_HAS_ARTICLE.ARTICLE_ID.eq(articleId))
            .execute();
    }

    private Product toProduct(Record record) {
        return new Product(
            record.get(PRODUCT.ID),
            record.get(PRODUCT.NAME),
            record.get(PRODUCT.PRICE),
            fetchProductArticles(record.get(PRODUCT.ID))
        );
    }

    private List<ProductArticle> fetchProductArticles(long productId) {
        return selectProductArticles(productId)
            .fetchStream()
            .map(record -> new ProductArticle(toArticle(record), record.get(PRODUCT_HAS_ARTICLE.QUANTITY)))
            .collect(toList());
    }

    private SelectConditionStep<Record> selectProductArticles(final long productId) {
        return db.select()
            .from(PRODUCT_HAS_ARTICLE)
            .join(ARTICLE)
            .on(PRODUCT_HAS_ARTICLE.ARTICLE_ID.eq(ARTICLE.ID))
            .where(PRODUCT_HAS_ARTICLE.PRODUCT_ID.eq(productId));
    }
}
