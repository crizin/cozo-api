INSERT IGNORE INTO site VALUES (1, 'RULIWEB', '루리웹', 'https://bbs.ruliweb.com/', 'https://m.ruliweb.com/');
INSERT IGNORE INTO site VALUES (2, 'THEQOO', '더쿠', 'https://theqoo.net/', 'https://theqoo.net/');
INSERT IGNORE INTO site VALUES (3, 'INSTIZ', '인스티즈', 'https://m.instiz.net/', 'https://m.instiz.net/');
INSERT IGNORE INTO site VALUES (4, 'CLIEN', '클리앙', 'https://www.clien.net/service/', 'https://m.clien.net/service/');
INSERT IGNORE INTO site VALUES (5, 'BOBAE', '보배드림', 'https://www.bobaedream.co.kr/', 'https://m.bobaedream.co.kr/');
INSERT IGNORE INTO site VALUES (6, 'DDANZI', '딴지일보', 'https://www.ddanzi.com/', 'https://www.ddanzi.com/');
INSERT IGNORE INTO site VALUES (7, 'MLBPARK', 'MLBPARK', 'https://mlbpark.donga.com/mp', 'https://mlbpark.donga.com/mp');
INSERT IGNORE INTO site VALUES (8, 'YGOSU', '와이고수', 'https://ygosu.com/', 'https://m.ygosu.com/');
INSERT IGNORE INTO site VALUES (9, 'PPOMPPU', '뽐뿌', 'https://ppomppu.co.kr/', 'https://m.ppomppu.co.kr/new/index.php');
INSERT IGNORE INTO site VALUES (10, 'ETOLAND', '이토랜드', 'https://www.etoland.co.kr/', 'https://www.etoland.co.kr/plugin/mobile/');
INSERT IGNORE INTO site VALUES (11, 'SLRCLUB', 'SLRCLUB', 'http://www.slrclub.com/', 'http://m.slrclub.com/');
INSERT IGNORE INTO site VALUES (12, 'TODAYHUMOR', '오늘의유머', 'http://www.todayhumor.co.kr/', 'http://m.todayhumor.co.kr/');
INSERT IGNORE INTO site VALUES (13, 'FMKOREA', '에펨코리아', 'https://www.fmkorea.com/', 'https://m.fmkorea.com/');

INSERT IGNORE INTO board
VALUES (1, 1, '유머 게시판', 'https://bbs.ruliweb.com/community/board/300143', 'https://m.ruliweb.com/community/board/300143', 'https://bbs.ruliweb.com/community/board/300143?page=%s',
		'https://bbs.ruliweb.com/community/board/300143/read/%s', 'https://m.ruliweb.com/community/board/300143/read/%s', 'me.cozo.api.infrastructure.crawler.RuliwebCrawler',
		b'1');
INSERT IGNORE INTO board
VALUES (2, 2, '스퀘어', 'https://theqoo.net/square', 'https://theqoo.net/square', 'https://theqoo.net/index.php?mid=square&page=%s', 'https://theqoo.net/square/%s',
		'https://theqoo.net/square/%s', 'me.cozo.api.infrastructure.crawler.TheQooCrawler', b'1');
INSERT IGNORE INTO board
VALUES (3, 3, '이슈', 'https://www.instiz.net/pt', 'https://www.instiz.net/pt', 'https://www.instiz.net/pt?page=%s', 'https://instiz.net/pt/%s', 'https://www.instiz.net/pt/%s',
		'me.cozo.api.infrastructure.crawler.InstizCrawler', b'1');
INSERT IGNORE INTO board
VALUES (4, 4, '모두의공원', 'https://www.clien.net/service/board/park', 'https://m.clien.net/service/board/park', 'https://www.clien.net/service/board/park?&od=T31&category=0&po=%s',
		'https://www.clien.net/service/board/park/%s', 'https://m.clien.net/service/board/park/%s', 'me.cozo.api.infrastructure.crawler.ClienCrawler', b'1');
INSERT IGNORE INTO board
VALUES (5, 5, '유머게시판', 'https://www.bobaedream.co.kr/list?code=strange', 'https://m.bobaedream.co.kr/board/new_writing/strange',
		'https://www.bobaedream.co.kr/list?code=strange&s_cate=&maker_no=&model_no=&or_gu=10&or_se=desc&s_selday=&pagescale=30&info3=&noticeShow=&s_select=&s_key=&level_no=&vdate=&type=list&page=%s',
		'https://www.bobaedream.co.kr/view?code=strange&No=%s', 'https://m.bobaedream.co.kr/board/bbs_view/strange/%s', 'me.cozo.api.infrastructure.crawler.BobaeCrawler', b'1');
INSERT IGNORE INTO board
VALUES (6, 6, '자유게시판', 'https://www.ddanzi.com/free', 'https://www.ddanzi.com/free', 'https://www.ddanzi.com/index.php?mid=free&page=%s', 'https://www.ddanzi.com/free/%s',
		'https://www.ddanzi.com/free/%s', 'me.cozo.api.infrastructure.crawler.DdanziCrawler', b'1');
INSERT IGNORE INTO board
VALUES (7, 7, 'BULLPEN', 'https://mlbpark.donga.com/mp/b.php?b=bullpen', 'https://mlbpark.donga.com/mp/b.php?b=bullpen',
		'https://mlbpark.donga.com/mp/b.php?p=%s&m=list&b=bullpen&query=&select=&user=', 'https://mlbpark.donga.com/mp/b.php?b=bullpen&id=%s',
		'https://mlbpark.donga.com/mp/b.php?b=bullpen&id=%s', 'me.cozo.api.infrastructure.crawler.MlbParkCrawler', b'1');
INSERT IGNORE INTO board
VALUES (8, 8, '엽기자랑', 'https://ygosu.com/board/yeobgi', 'https://m.ygosu.com/board/yeobgi', 'https://ygosu.com/board/yeobgi/?page=%s',
		'https://ygosu.com/board/yeobgi/%s', 'https://m.ygosu.com/board/yeobgi/%s', 'me.cozo.api.infrastructure.crawler.YgosuCrawler', b'1');
INSERT IGNORE INTO board
VALUES (9, 9, '자유게시판', 'https://www.ppomppu.co.kr/zboard/zboard.php?id=freeboard', 'https://m.ppomppu.co.kr/new/bbs_list.php?id=freeboard',
		'https://www.ppomppu.co.kr/zboard/zboard.php?id=freeboard&page=%s', 'https://www.ppomppu.co.kr/zboard/view.php?id=freeboard&no=%s',
		'https://m.ppomppu.co.kr/new/bbs_view.php?id=freeboard&no=%s', 'me.cozo.api.infrastructure.crawler.PpomppuCrawler', b'1');
INSERT IGNORE INTO board
VALUES (10, 10, '유머게시판', 'https://www.etoland.co.kr/bbs/board.php?bo_table=etohumor06', 'https://www.etoland.co.kr/plugin/mobile/board.php?bo_table=etohumor06',
		'https://www.etoland.co.kr/bbs/board.php?bo_table=etohumor06&page=%s', 'https://www.etoland.co.kr/bbs/board.php?bo_table=etohumor06&wr_id=%s',
		'https://www.etoland.co.kr/plugin/mobile/board.php?bo_table=etohumor06&wr_id=%s', 'me.cozo.api.infrastructure.crawler.EtoLandCrawler', b'1');
INSERT IGNORE INTO board
VALUES (11, 11, '자유게시판', 'http://www.slrclub.com/bbs/zboard.php?id=free', 'http://m.slrclub.com/l/free', 'http://www.slrclub.com/bbs/zboard.php?id=free&page=%s',
		'http://www.slrclub.com/bbs/vx2.php?id=free&no=%s', 'http://m.slrclub.com/v/free/%s', 'me.cozo.api.infrastructure.crawler.SlrClubCrawler', b'1');
INSERT IGNORE INTO board
VALUES (12, 12, '유머자료게시판', 'http://www.todayhumor.co.kr/board/list.php?table=humordata', 'http://m.todayhumor.co.kr/list.php?table=humordata',
		'http://www.todayhumor.co.kr/board/list.php?table=humordata&page=%s', 'http://todayhumor.com/?humordata_%s', 'http://m.todayhumor.co.kr/view.php?table=humordata&no=%s',
		'me.cozo.api.infrastructure.crawler.TodayHumorCrawler', b'1');
INSERT IGNORE INTO board
VALUES (13, 13, '유머/움짤/이슈', 'https://www.fmkorea.com/humor', 'https://m.fmkorea.com/humor', 'https://www.fmkorea.com/index.php?mid=humor&page=%s', 'https://www.fmkorea.com/%s',
		'https://m.fmkorea.com/%s', 'me.cozo.api.infrastructure.crawler.FmKoreaCrawler', b'1');
