SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS site (
	id              BIGINT       NOT NULL AUTO_INCREMENT,
	`key`           VARCHAR(31)  NOT NULL,
	name            VARCHAR(31)  NOT NULL,
	main_url_pc     VARCHAR(511) NOT NULL,
	main_url_mobile VARCHAR(511) NOT NULL,
	PRIMARY KEY (id),
	UNIQUE INDEX uk_key (`key`) USING HASH
);

CREATE TABLE IF NOT EXISTS board (
	id                 BIGINT       NOT NULL AUTO_INCREMENT,
	site_id            BIGINT       NOT NULL,
	name               VARCHAR(31)  NOT NULL,
	main_url_pc        VARCHAR(511) NOT NULL,
	main_url_mobile    VARCHAR(511) NOT NULL,
	paging_url_pc      VARCHAR(511) NOT NULL,
	content_url_pc     VARCHAR(511) NOT NULL,
	content_url_mobile VARCHAR(511) NOT NULL,
	crawler_class      VARCHAR(63)  NOT NULL,
	active             BIT(1)       NOT NULL,
	PRIMARY KEY (id),
	INDEX idx_site_id (site_id) USING HASH,
	INDEX idx_active (active),
	CONSTRAINT fk_board_site_id FOREIGN KEY (site_id) REFERENCES site (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS article (
	id             BIGINT        NOT NULL AUTO_INCREMENT,
	board_id       BIGINT        NOT NULL,
	origin_id      VARCHAR(127)  NOT NULL,
	title          VARCHAR(1023) NOT NULL,
	hits           INT           NOT NULL,
	comments       INT           NOT NULL,
	likes          INT           NOT NULL,
	clicks         INT           NOT NULL,
	contains_image BIT(1)        NOT NULL,
	contains_video BIT(1)        NOT NULL,
	created_at     DATETIME(6)   NOT NULL,
	collected_at   DATETIME(6)   NOT NULL,
	PRIMARY KEY (id),
	UNIQUE INDEX uk_board_id_origin_id (board_id, origin_id) USING HASH,
	INDEX idx_board_id_collected_at (board_id, collected_at DESC),
	INDEX idx_created_at (created_at DESC),
	CONSTRAINT fk_article_board_id FOREIGN KEY (board_id) REFERENCES board (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS article_content (
	article_id BIGINT   NOT NULL,
	content    LONGTEXT NOT NULL,
	PRIMARY KEY (article_id),
	CONSTRAINT fk_article_content_article_id FOREIGN KEY (article_id) REFERENCES article (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS article_link (
	article_id BIGINT NOT NULL,
	link_id    BIGINT NOT NULL,
	PRIMARY KEY (article_id, link_id),
	INDEX idx_link_id (link_id) USING HASH,
	CONSTRAINT fk_article_link_article_id FOREIGN KEY (article_id) REFERENCES article (id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT fk_article_link_link_id FOREIGN KEY (link_id) REFERENCES link (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS article_tag (
	article_id BIGINT NOT NULL,
	tag_id     BIGINT NOT NULL,
	PRIMARY KEY (article_id, tag_id),
	INDEX idx_tag_id (tag_id),
	CONSTRAINT fk_article_tag_article_id FOREIGN KEY (article_id) REFERENCES article (id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT fk_article_tag_tag_id FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS link (
	id            BIGINT        NOT NULL AUTO_INCREMENT,
	type          VARCHAR(5)    NOT NULL,
	host          VARCHAR(255)  NOT NULL,
	url           VARCHAR(768)  NOT NULL,
	title         VARCHAR(1023) NULL DEFAULT NULL,
	description   VARCHAR(1023) NULL DEFAULT NULL,
	thumbnail_url VARCHAR(1023) NULL DEFAULT NULL,
	last_used_at  DATETIME(6)   NOT NULL,
	PRIMARY KEY (id),
	UNIQUE INDEX uk_url (url) USING HASH,
	INDEX idx_host (host),
	INDEX idx_last_used_at (last_used_at DESC)
);

CREATE TABLE IF NOT EXISTS tag (
	id   BIGINT      NOT NULL AUTO_INCREMENT,
	name VARCHAR(31) NOT NULL,
	PRIMARY KEY (id),
	UNIQUE INDEX uk_name (name) USING HASH
);

CREATE TABLE IF NOT EXISTS tag_trend (
	date    DATE   NOT NULL,
	ranking INT    NOT NULL,
	tag_id  BIGINT NOT NULL,
	diff    INT    NOT NULL,
	PRIMARY KEY (date, ranking),
	INDEX idx_tag_id (tag_id),
	CONSTRAINT fk_tag_trend_tag_id FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS shedlock (
	name       VARCHAR(64)  NOT NULL,
	lock_until TIMESTAMP(3) NOT NULL,
	locked_at  TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
	locked_by  VARCHAR(255) NOT NULL,
	PRIMARY KEY (name)
);

TRUNCATE TABLE shedlock;

SET FOREIGN_KEY_CHECKS = 1;
