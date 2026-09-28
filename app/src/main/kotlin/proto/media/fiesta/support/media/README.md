# support/media

Framework de controle remoto de mídia. Kotlin puro: sem `android.*`, sem `R`, sem dependência
do Fiesta.

## Os três papéis

| Papel | Quantos | O que faz |
|---|---|---|
| **renderer** | um | reproduz de verdade; executa comandos e reporta o que acontece |
| **servidor** | um | a porta de entrada; guarda a sessão e transmite aos clientes |
| **cliente** | vários | consulta o estado e pede coisas |

A **sessão** é a fonte única da verdade. Ela nasce quando um renderer se registra, é dele
enquanto ele estiver registrado, e não decide nada: guarda o estado, reconcilia intenção com
verdade e roteia. Quem decide são as pontas.

## As três formas de chamada

- **Procedimento** — leitura síncrona: `server.state`, `handle.state`, `session.pendingCommand`.
- **Comando** — cliente → sessão → renderer. O cliente chama funções (`handle.pause()`); o
  `MediaCommandDto` é o que viaja daí para dentro.
- **Evento** — renderer → sessão → todos os clientes: `session.report(RendererEventDto)` entra,
  `MediaEventDto.StateChanged` sai.

## Os cinco contratos

A lib não traz implementação de nenhum deles.

```kotlin
interface MediaRenderer { val id: String; fun execute(command: MediaCommandDto) }
interface MediaClient   { fun onConnected(handle: ClientHandle); fun onEvent(event: MediaEventDto) }
interface ClientHandle  { val state: MediaStateDto; fun play(); /* …os dez verbos… */ fun disconnect() }
interface SessionStore  { fun load(rendererId: String): MediaSnapshotDto?; /* save, clear */ }
interface MediaLog      { fun debug(message: String); fun error(message: String, cause: Throwable?) }
```

## Uso

```kotlin
val server = MediaServer(
    clock = SystemClock::elapsedRealtime,
    scheduleAt = { at, action -> handler.postDelayed(action, at - SystemClock.elapsedRealtime()) },
    store = CarSessionStore(context),
    log = AndroidMediaLog
)

val session = server.register(carPlayer)          // cria a sessão; devolve por onde reportar
session.report(RendererEventDto.Read(reading))    // o renderer relata

val handle = server.connect(notification)         // o cliente entra e recebe o estado corrente
handle.pause()                                    // e pede coisas
```

## Regras que não dá para descobrir lendo o código

- **Os callbacks rodam segurando o monitor da sessão.** `MediaClient.onEvent` e
  `MediaRenderer.execute` não podem bloquear — nada de rede, disco ou espera por outra thread
  lá dentro. Reentrância da mesma thread é livre; um cliente que lança é desconectado.
- **Posição não é evento.** A sessão só transmite quando muda algo além da posição; quem quer
  posição lê `handle.state` e extrapola por `progress.updatedAtMillis`. É o eventing moderado
  do UPnP `AVTransport`.
- **Registrar outro `id` não herda nada** — nem a sessão, nem o instantâneo do renderer
  anterior.
- **Comando com renderer ausente é descartado**, sem fila de intenção pendente. Ressuscitar o
  player é trabalho do renderer.
- **Parar não é pausar.** `Stop` deixa a sessão em `STOPPED` e ela fica lá — sai quando a
  página volta a tocar ou quando a faixa muda, não quando chega a próxima leitura pausada.
- **Reiniciar a faixa não é comando de fio.** O renderer recebe `SkipToPrevious` e decide se
  volta ou reinicia — é ele que sabe a posição e o meio.
- **Toda definição mora em `config/MediaDefaults`.** Nada de número solto no código.

## De onde vieram as decisões

Controle remoto de player é problema resolvido. Estas seis coisas foram tiradas de padrão
aberto, não inventadas aqui:

| O que | De onde |
|---|---|
| Conectar entrega o estado inteiro — e o assinante existe **antes** do primeiro evento | GENA: o `SUBSCRIBE` responde com o `SID`, e a mensagem de evento inicial, com todas as variáveis evented, vem depois. Daí o `onConnected(handle)` |
| Posição não é evento, é consulta | `AVTransport`: as variáveis que mudam continuamente ficam fora do `LastChange`, e o ponto de controle chama `GetPositionInfo` na cadência dele |
| As ações possíveis fazem parte do estado | `CurrentTransportActions` do `AVTransport`, e o `actions.disallows` do Spotify — a mesma lista, invertida. Daí `CapabilitiesDto` ser `Set<MediaAction>` |
| O estado diz **quando** foi medido | `timestamp` do Spotify, `updateTime` do `PlaybackStateCompat`. Daí `MediaProgressDto.updatedAtMillis` |
| O estado diz **qual** renderer está tocando | o objeto `device` do Spotify (`id`, `is_active`). Daí `RendererDto` |
| Um assinante quebrado não cega os outros | GENA: um `NOTIFY` que falha não interrompe a entrega aos demais, e a assinatura que falha é descartada |

Um dispositivo ativo por vez, com id, é o modelo do Spotify Connect — sem a transferência de
estado entre dispositivos, que aqui não existe por decisão.

- [UPnP Device Architecture 1.1](https://upnp.org/specs/arch/UPnP-arch-DeviceArchitecture-v1.1.pdf) — descoberta, controle e o eventing GENA
- [UPnP AVTransport:1 Service Template](https://upnp.org/specs/av/UPnP-av-AVTransport-v1-Service.pdf) — `TransportState`, `LastChange`, `CurrentTransportActions`, `GetPositionInfo`
- [UPnP RenderingControl:1 Service Template](https://upnp.org/specs/av/UPnP-av-RenderingControl-v1-Service.pdf) — volume e mudo, que ainda não temos
- [Spotify Web API — Get Playback State](https://developer.spotify.com/documentation/web-api/reference/get-information-about-the-users-current-playback) — o objeto de estado, `device`, `timestamp`, `actions.disallows`
- [Spotify Engineering — Spotify's Player API](https://engineering.atspotify.com/2022/04/spotifys-player-api) — o modelo do Connect: lista de dispositivos e comando com alvo

## O que ainda não existe

Embaralhar e repetir (`PlayMode` do `AVTransport`, `shuffle_state`/`repeat_state` do Spotify),
volume e mudo (`RenderingControl`), e um comando para dizer *o que* tocar
(`SetAVTransportURI`, `context_uri`).

Os três ficaram de fora por não terem cenário: volume aqui é do sistema, porque o renderer é
local e não remoto; embaralhar e repetir são controles da página, e traduzi-los exigiria
trabalho de plugin por site para uma funcionalidade que o app não tem; e "toque isto" não tem
quem emita enquanto a árvore de navegação servir um item só. Ver
`docs/plans/46-framework-de-midia-cliente-servidor.md`.

## Testes

`app/src/test/kotlin/proto/media/fiesta/support/media/` — 70 casos, JUnit puro, com dublês de
renderer, cliente, store, log e relógio em `TestDoubles.kt`.
