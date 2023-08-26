package me.cozo.api.application.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.cozo.api.application.command.ClickArticleCommand;
import me.cozo.api.application.command.DeleteArticleCommand;
import me.cozo.api.domain.repository.ArticleRepository;
import me.cozo.api.domain.repository.search.SearchRepository;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleCommandHandler {

	private final ArticleRepository articleRepository;
	private final SearchRepository searchRepository;

	@ServiceActivator(inputChannel = "ClickArticleCommand")
	public void handle(ClickArticleCommand command) {
		articleRepository.findById(command.articleId())
			.ifPresent(article -> {
				article.click();
				articleRepository.save(article);
			});
	}

	@ServiceActivator(inputChannel = "DeleteArticleCommand")
	public void handle(DeleteArticleCommand command) {
		articleRepository.findById(command.articleId())
			.ifPresent(article -> {
				articleRepository.delete(article);
				searchRepository.deleteById(command.articleId());

				log.info("Delete article [site={}, board={}, title={}, createdAt={}]",
					article.getBoard().getSite().getName(), article.getBoard().getName(), article.getTitle(), article.getCreatedAt()
				);
			});
	}
}
