# 🌙 Lunar API

**A 3rd-party LunarClient backend implementation inspired by Solar-Sockets**

[![Project Status](https://img.shields.io/badge/status-active-brightgreen.svg)]()
[![Java](https://img.shields.io/badge/Java-21+-orange?logo=openjdk)]()
[![WebSocket](https://img.shields.io/badge/WebSocket-API-blue?logo=websocket)]()

## ✨ Features

| Feature                  | Status      |
|--------------------------|-------------|
| ✅ Authenticator          | Implemented |
| ✅ Cosmetics              | Implemented |
| ✅ Emotes                 | Implemented |
| ✅ Jams                   | Implemented |
| ✅ Conversation & Friends | Implemented |
| ✅ Styngr API             | Implemented |
| ✅ Game Metadata          | Implemented |
| ❌ Launcher Metadata      | Pending     |


## Structure

The backend is available at `src`

Frontend at `dashboard`

## Develop on the servlet

Please see [develop.md](docs/develop.md)

## Connect to the server

Enable LunarClient debug mode and add these JVM params to your client

```text
-DserviceOverrideAuthenticator=ws://127.0.0.1:8080/ws
-DserviceOverrideAssetServer=ws://127.0.0.1:8080/ws
-DserviceOverrideApi=http://127.0.0.1:8080/api/lunar
-DserviceOverrideStyngr=http://127.0.0.1:8080/api/styngr
```

Use the [Javaagent](https://github.com/earthsworth/LunarDebugger/releases/tag/v1.0.0) to enable Lunar debug mode
if you don't want to manually modify the file (DO NOT USE THE 1.1.0 VERSION)

## OpenAPI

The OpenAPI is available at `https://<your-backend>/api-docs` (use `http` protocol if you're on the local one)

You can import it via [Swagger Editor](https://editor-next.swagger.io/)

## Resources

- The [ProtoDumper](https://github.com/ManTouQAQ/ProtoDumper)
- The [Celestial Launcher](https://github.com/earthsworth/celestial)
- The [LunarClient OpenAPI spec](docs/lc-openapi.json)
- Everything about [serviceOverride](https://github.com/earthsworth/celestial/wiki/Service-Override)
- Everything about [reactor](https://projectreactor.io/)
- [Shadcn/ui components](https://ui.shadcn.com/docs/)

# Guideline

[CONTRIBUTING.md](./CONTRIBUTING.md)