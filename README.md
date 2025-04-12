# Lunar API

A 3rd party LunarClient backend implementation like Solar-Sockets

## Features

- [x] Authenticator
- [x] Cosmetics
- [x] Emotes
- [ ] Jams
- [x] Conversation & Friends
- [x] Styngr API
- [ ] Game Metadata
- [ ] Launcher Metadata

## Structure

The backend is available at `src`

Frontend at `dashboard`

## Debugging

Enable LunarClient debug mode and add these JVM params to your client

```text
-DserviceOverrideAuthenticator=ws://127.0.0.1:8080/ws
-DserviceOverrideAssetServer=ws://127.0.0.1:8080/ws
-DserviceOverrideApi=http://127.0.0.1:8080/api/lunar
-DserviceOverrideStyngr=http://127.0.0.1:8080/api/styngr
```

Use the [Javaagent](https://github.com/earthsworth/LunarDebugger/releases/tag/v1.0.0) to enable Lunar debug mode
if you don't want to manually modify the file (DO NOT USE THE 1.1.0 VERSION)

## Resources

- The [ProtoDumper](https://github.com/ManTouQAQ/ProtoDumper)
- The [Celestial Launcher](https://github.com/earthsworth/celestial)
- The [LunarClient OpenAPI spec](https://api.lunarclientprod.com/f5278921b2d4429d95531e025f5318fd/openapi)
- Everything about [serviceOverride](https://github.com/earthsworth/celestial/wiki/Service-Override)
- Everything about [reactor](https://projectreactor.io/)

# Guideline

[CONTRIBUTING.md](./CONTRIBUTING.md)