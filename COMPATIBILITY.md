# Shared chat compatibility prototype

Based on AntiPopup commit `cffaba18732414cb3a8dcde13f052b10916b946c`.

This fork prefers a shared runtime-resolved injector on Mojang-mapped Paper/Folia
servers retaining the modern chat contract. It does not require a new injector
entry for each Minecraft release. It is not a promise of support for unknown
future packet semantics.

## Changes

- Resolve and validate the player handle, unique typed connection/channel fields,
  message accessors, decoration method and exact system-chat constructor once.
- Traverse inherited fields; reject ambiguous or inaccessible layouts.
- Support both nullable unsigned components and `Optional<Component>` (26.3).
- Replace outgoing chat using its existing Netty write context and promise.
  Preserve the server's rich component and chat-type decoration.
- Make pipeline changes on the channel event loop and detach on quit/disable.
- Log conversion errors and fail the write/close that connection rather than
  silently send the original player-chat packet while claiming protection.
- Preserve established version-specific adapters as a fallback for older or
  obfuscated server layouts. Their existing behavior is unchanged.
- Bundle PacketEvents 2.14.0 for its 26.3 support.

## Remaining compatibility boundaries

PacketEvents still initializes and implements popup suppression, chat-session
filtering, and other packet features. Its compatibility requirements still apply.
This change reduces maintenance in AntiPopup's chat conversion; it does not
remove that dependency or automatically interpret future packet changes.

The shared resolver deliberately recognizes named semantic accessors and known
return-type variants. It does not guess which arbitrary boolean or method has a
particular meaning. A changed contract results in a diagnostic or a known legacy
adapter, not speculative packet mutation.

## Validation and installation

Run `gradlew.bat :nms:test :spigot:shadowJar` on Windows. The installable server
plugin is the `spigot` module's shaded JAR, not the small `nms` JAR.

Automated tests cover content/decoration preservation, literal fallback,
Optional handling, inherited/ambiguous fields, packet ordering and promises,
conversion failure, repeated attachment, quit and shutdown cleanup.

Class-resolution and actual packet-conversion probes were run with official
Paper 1.20.6 build 151, Paper 1.21.11 build 132, Paper 26.2 build 134 and Folia
26.3 build 3. These initialize server registries but do not launch a world or
connect a Minecraft client. They are not full multiplayer acceptance tests.

Test on a separate server first. Replace the existing AntiPopup JAR and restart.
Keep `block-chat-reports: true`, `show-popup: false`, and leave the old hidden
`experimental-mode` disabled. `enforce-secure-profile=false` is still required
by the original setup. Look for `Validated shared chat injector using server
capabilities.` in the log. Verify joining, ordinary/unsigned chat, custom chat
formatting, click/hover events, reconnects, and your ViaVersion/chat-plugin setup.
Popup behavior and complete live Folia sessions still require client testing.
