package me.cozo.api.domain.event;

public record ArticleUpdatedEvent(Long articleId, boolean linkUpdated) {
}
