package com.xxx.inventory.domain;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public interface ArticleRepository {

    void save(Article article);
    boolean lockArticle(long articleId);
}
