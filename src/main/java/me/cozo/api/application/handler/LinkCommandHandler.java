package me.cozo.api.application.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.cozo.api.application.command.RefreshLinkCommand;
import me.cozo.api.domain.repository.LinkRepository;
import me.cozo.api.infrastructure.client.LinkClient;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkCommandHandler {

	private final LinkClient linkClient;
	private final LinkRepository linkRepository;

	@ServiceActivator(inputChannel = "RefreshLinkCommand")
	public void handle(RefreshLinkCommand command) {
		linkRepository.findById(command.linkId())
			.ifPresent(link -> {

				try {
					linkClient.fetchLink(link);
				} catch (Exception e) {
					log.error("Refresh Link - Failed [type={}, url={}, e={}]", link.getType(), link.getUrl(), e.getMessage());
					return;
				}

				linkRepository.save(link);
				log.info("Refresh Link - Success [type={}, url={}]", link.getType(), link.getUrl());
			});
	}
}
