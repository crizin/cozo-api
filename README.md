# cozo-api

[![Build](https://github.com/crizin/cozo-api/actions/workflows/build.yml/badge.svg)](https://github.com/crizin/cozo-api/actions)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/65868e91fbd848e78f587d314aba85ec)](https://app.codacy.com/gh/crizin/cozo-api/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![License: MIT](https://img.shields.io/github/license/crizin/cozo-api)](https://opensource.org/licenses/MIT)

https://cozo.me 사이트의 API 백엔드를 담당하는 https://api.cozo.me 서버

## 로컬 개발

로컬 개발에 필요한 mysql, redis, elasticsearch를 docker 디렉토리 아래 준비된 [Docker Compose](docker/docker-compose.yml) 파일을 이용하여 실행한다.

```shell
$ cd docker
$ docker compose up -d
```

## production에서 실행시 필요한 환경 변수

- `SPRING_PROFILES_ACTIVE`: production
- `DB_USERNAME`: MySQL user
- `DB_PASSWORD`: MySQL password
- `DISCORD_WEBHOOK`: Discord webhook URL
- `TURNSTILE_SECRET`: [Cloudflare Turnstile](https://www.cloudflare.com/products/turnstile/) secret
- `YOUTUBE_API_KEY`: YouTube API key

## GitHub Action

- [.github/workflows/build.yml](.github/workflows/build.yml): 프로젝트 빌드 후 GitHub Container Registry에 Docker 이미지를 푸시
    - 필요한 Secrets
        - `DISCORD_WEBHOOK`: https://discord.com/api/webhooks/0000000000000000000/oooooooooooooo_ooooooooooooooooooooooooooooooooooooooooooooooooooooo

## MCP (Model Context Protocol) 서버

Spring AI를 통해 MCP 서버를 제공한다

### MCP 서버 등록 방법

```json
{
  "mcpServers": {
    "cozo-mcp-server": {
      "type": "http",
      "command": "npx",
      "args": [
        "@modelcontextprotocol/server-sse",
        "https://api.cozo.me/sse"
      ]
    }
  }
}
```

### 제공되는 도구

- **getTrendingKeywords**: 특정 날짜의 인기 키워드와 관련 게시글 조회
- **searchArticles**: 키워드로 커뮤니티 게시글 검색

## 참고

- [cozo-web / 웹 클라이언트](https://github.com/crizin/cozo-web)
