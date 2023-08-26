package me.cozo.api.domain.dto;

import me.cozo.api.domain.model.Site;

import java.io.Serializable;

public record SiteDto(
	String key,
	String name,
	String mainUrlPc,
	String mainUrlMobile
) implements Serializable {

	public static SiteDto of(Site site) {
		return new SiteDto(
			site.getKey(),
			site.getName(),
			site.getMainUrlPc(),
			site.getMainUrlMobile()
		);
	}
}
