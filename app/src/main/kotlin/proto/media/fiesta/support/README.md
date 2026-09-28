# support

`support/` é onde moram as libs do Fiesta — pacotes autocontidos, cada um resolvendo um problema
que não é específico deste app, escritos para poderem ser reaproveitados em outro projeto Android
sem trazer o Fiesta junto.

## O teste da lib

Um pacote só entra aqui se passar neste teste, arquivo a arquivo:

- **não importa `androidx`** — a ilha do carro (`android.support`) e o celular (AndroidX)
  convivem no classpath por decisão de arquitetura, e nenhum dos dois lados pode vazar para
  dentro de uma lib;
- **não importa `R`** — nada de string, drawable ou cor do Fiesta; textos e ícones entram por
  interface (`WebViewExTexts`) ou não fazem parte do pacote (os de `search` ficam em
  `shared/search`);
- **o que vem de fora entra por contrato** — disco, assets, preferências, log: a lib declara a
  interface (`PluginFiles`, `SearchEngineFiles`, `MediaLog`, ...) e `shared/` implementa;
- **não importa `proto.media.fiesta.shared` nem `proto.media.fiesta.features`** — uma lib nunca
  depende de código do app; a dependência é sempre na direção contrária.

Uma lib **pode** importar outra lib de `support/` (`support/plugins` usa `support/search`, por
exemplo) — o teste é sobre o app, não entre libs. Quando duas libs só precisam concordar sobre
um ciclo de vida, cada uma tem o seu e a tradução fica em `shared/` (`support/plugins` não importa
`support/webviewex`; ver `shared/plugins/PluginLifecycleAlignment.kt`).

O gate é mecânico e roda em qualquer momento:

```bash
grep -rn "import proto\.media\.fiesta\.\(shared\|features\)\|proto\.media\.fiesta\.R\b\|androidx" app/src/main/kotlin/proto/media/fiesta/support/
```

Vazio é o esperado. Se aparecer alguma linha, ou o import está errado ou o arquivo não devia
estar em `support/`.

## O que não é lib

`support/system` importa `android.*` e `support/plugins` usa `org.json` no parser de
manifesto: são dependentes de plataforma, mas continuam sem depender do Fiesta. "Lib" aqui não
significa "Kotlin puro"; significa "app-independente". `support/media` e `support/search` são
Kotlin puro (sem `android.*`), e por isso rodam em JUnit puro, sem Robolectric.

## Cada pacote com dono no app

`support/` nunca conhece o Fiesta, mas o Fiesta precisa de textos localizados, de
`SharedPreferences`, de `AdBlockUtils` — coisas que só existem no app. Essa ligação é
responsabilidade de `shared/`: cada lib com necessidade de cola tem uma contraparte do mesmo nome
em `shared/` (`shared/webviewex`, `shared/media`, `shared/search`, `shared/plugins`) que
implementa os contratos que a lib expõe (`WebViewExHost`, `WebViewExTexts`, `MediaLog`,
`SessionStore`, `PluginFiles`, `PluginStateStore`, ...) usando `R` e
o resto do app. Quem monta os dois lados é `config/wiring/MyApplication`. Ver o README de cada
pacote para o contrato que ele espera de fora.

## Onde muda comportamento por carro/celular

Nenhuma lib tem `if (isCar)`. Onde o ambiente muda o comportamento, o pacote define uma interface
com implementação padrão vazia ou neutra (`WebViewExGearhead` em `support/webviewex`) e quem quer
comportamento de carro passa uma implementação concreta (`AndroidAutoWebViewExGearhead`) — a lib
nunca sabe que "carro" existe, só conhece a interface.

## Testes

Cada pacote com lógica pura tem sua própria pasta em
`app/src/test/kotlin/proto/media/fiesta/support/<pacote>/`. O parser de manifesto de
`support/plugins` não roda sem instrumentação; o resto roda em JUnit puro. Ver o README de cada
pacote para a cobertura específica.
