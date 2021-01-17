package com.xxx.inventory.domain;

import com.google.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.Record;

import static com.xxx.inventory.sql.Tables.ARTICLE;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public class ArticleRepositoryImpl implements ArticleRepository {

    private final DSLContext db;

    @Inject
    public ArticleRepositoryImpl(DSLContext db) {
        this.db = db;
    }

    @Override
    public void save(Article article) {
        db.insertInto(ARTICLE)
            .columns(ARTICLE.ID, ARTICLE.NAME, ARTICLE.AVAILABLE_STOCK)
            .values(article.getId(), article.getName(), article.getAvailableStock())
            .onDuplicateKeyUpdate()
            .set(ARTICLE.NAME, article.getName())
            .set(ARTICLE.AVAILABLE_STOCK, article.getAvailableStock())
            .execute();
    }

    @Override
    public boolean lockArticle(long articleId) {
        return db.select().from(ARTICLE).where(ARTICLE.ID.eq(articleId)).forUpdate().execute() > 0;
    }

    public static Article toArticle(Record record) {
        return new Article(
            record.get(ARTICLE.ID),
            record.get(ARTICLE.NAME),
            record.get(ARTICLE.AVAILABLE_STOCK)
        );
    }
}
