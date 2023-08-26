package me.cozo.api.application.command;

import me.cozo.api.domain.model.Article;

public record FetchArticleCommand(Article fetchedArticle, int trial) {

	public FetchArticleCommand(Article fetchedArticle) {
		this(fetchedArticle, 1);
	}
}
