# support/plugins

Motor de scripting por site (uma extensão de navegador, não um plugin de app):
lê manifestos de plugin (embutidos no APK ou instalados pelo usuário via zip), casa contra a URL
atual e decide o que injetar e quando. Mantém uma pilha de ativação ordenável e persistida por
usuário. Não importa `android.*`, e não depende do app nem do navegador. O único resto de
plataforma é o `org.json` do `PluginManifestJson`.

## Pastas

| Pasta | O que tem |
|---|---|
| `contract/` | As portas que o app implementa: `PluginFiles` (disco e assets), `PluginStateStore` (pilha de ativação persistida), `PluginArchive` (pacote de plugin), `PluginLog`, `PluginPageListener` (o que a página avisa) |
| `model/` | Os tipos: `PluginManifest` e o resto do modelo, `PageStage`/`PageVisibility`, `InvalidPluginException` |
| `manifest/` | Ler e validar o manifesto: `PluginManifestJson`, `PluginManifestValidator`, `UrlMatch`, `PluginDomain`, `PluginPlatforms` |
| `archive/` | `ZipPluginArchive`, a implementação de `PluginArchive` para `.zip` |
| `activation/` | A pilha: `PluginSource`, `PluginActivationStack`, `ActivationOrderCodec`, `InstalledOptionKey` |
| `injection/` | O que roda na página: `PluginInjector`, `PluginResolver`, `JsString` |
| `voice/` | Busca por voz: `VoiceDestination`, `VoiceListener`, `VoiceSearchUrl` |

As implementações Android dos contratos moram em `shared/plugins/` (`AndroidPluginFiles`,
`PreferencesPluginStateStore`, `AndroidPluginLog`, `PluginZipInstaller`, montados por
`AppPlugins.source(context)`). O ciclo de vida é da lib (`PageStage`, `PageVisibility`),
alinhado ao de `support/webviewex` pelos mesmos `jsName`; o `PluginInjector` recebe só uma função
`evaluate: (String) -> Boolean`. A tradução `DocumentStage → PageStage` e
`RenderMode → PageVisibility` é cola e mora em `shared/plugins/PluginLifecycleAlignment.kt`, com
teste que quebra se os dois ciclos divergirem.

## As três peças

- **`PluginSource(files, store, log, postToMain)`** — composição: `stack()` (defaults +
  instalados, cacheado na instância), `activate`/`deactivate`/`restoreDefaults`,
  `install(from: File)`/`remove(id)`, `script(pluginId, script)`, `runtime()`,
  `effectiveOptionKey(...)`. A verificação dos instalados (SHA-256 contra `index.json`) é
  papel do `PluginFiles`: um arquivo que não bate é descartado, não travado.
- **`PluginInjector`** — o orquestrador ligado ao ciclo de vida da página:
  `onPageStage(stage, url)`, `onStackChanged(url)`, `onPageVisibilityChanged(visibility)`,
  `dispatch(event, argument): Boolean`. É quem decide, a cada estágio, quais scripts rodam.
- **`PluginResolver(stack)`** — a lógica pura de casamento: `eventsFor(url)` (o primeiro plugin
  da pilha cuja regra bate, sem cair para o próximo por especificidade) e `tweaksFor(url, env)`
  (todos os tweaks de todos os plugins que casam, sem repetir `(pluginId, script)`).

## O modelo

Um `PluginManifest` tem uma lista de `PluginRule(match, events, tweaks)`; `match` são padrões
glob (`*`) comparados sem diferenciar maiúsculas via `UrlMatch.matches`. Um `TweakSpec` carrega
`runAt: PageStage` (quando injetar), `rerun` (`LOAD` uma vez por carregamento, `URL_CHANGE`
uma vez por URL distinta), `requires` (pré-condição, hoje só `ADBLOCK`) e `option` (liga/desliga
próprio). A pilha de ativação (`ActivationState(order, seenDefaults, removedDefaults)`) é pura e
testável em `PluginActivationStack`: plugin padrão novo entra ativado sozinho a menos que o
usuário já tenha removido; plugin `fallback` fica sempre por último e não pode ser
desativado/removido pelo usuário.

## Regras que não dá para descobrir lendo por cima

- **O app usa uma instância só.** O cache e os listeners são da instância; por isso todo mundo
  pega o `PluginSource` por `AppPlugins.source(context)` e nunca constrói outro.
- **Eventos são exclusivos do carro.** `dispatch()` só tem efeito com `env == PluginEnv.CAR` —
  tweaks continuam valendo para o celular, a pilha de handlers de evento não.
- **`dispatch()` retorna `false` em silêncio** quando a página não registrou aquele evento; quem
  chama precisa checar o retorno para saber se a tecla do volante foi de fato consumida.
- **Rerun por `LOAD` só zera em documento novo** (`COMMITTED`), não em troca de rota dentro do
  SPA (`ROUTE_CHANGED`) — um tweak `LOAD` não roda de novo numa navegação interna, só `URL_CHANGE`
  roda se a URL mudou de fato.
- **Plugin instalado tem restrições que o padrão não tem**: não pode ser `fallback`, não pode
  usar um padrão universal de URL (`UrlMatch.isUniversal`) — evita que um plugin de terceiros
  sequestre o app inteiro.

## Testes

`app/src/test/kotlin/proto/media/fiesta/support/plugins/`, nas mesmas pastas do código — cobre
a parte pura (resolução, pilha de ativação, casamento de URL, validação de manifesto, voz,
`JsString`, extração do zip) e o `PluginSource` com portas falsas. O que depende de parsear
manifesto não roda em JUnit, porque o `org.json` devolve `null` fora do Android.
