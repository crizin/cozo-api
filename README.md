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
- `SLACK_WEBHOOK_URL`: Slack webhook URL
- `TURNSTILE_SECRET`: [Cloudflare Turnstile](https://www.cloudflare.com/products/turnstile/) secret
- `YOUTUBE_API_KEY`: YouTube API key

## GitHub Action

- [.github/workflows/build.yml](.github/workflows/build.yml): 프로젝트 빌드 후 GitHub Container Registry에 Docker 이미지를 푸시
    - 필요한 Secrets
        - `SLACK_WEBHOOK_URL`: https://hooks.slack.com/services/ABCDEFGHI/JKLMNOPQRST/abcdefghijklmnOPQRSTU012

## 참고

- [cozo-web / 웹 클라이언트](https://github.com/crizin/cozo-web)
