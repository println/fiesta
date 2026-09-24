<div align="center">

🌐 **[println.github.io/fiesta](https://println.github.io/fiesta/pt-br/)**

[🇺🇸 English](README.md) · 🇧🇷 Português

<img src="docs/images/logo.svg" alt="Fiesta" width="480">

# Mais estrada. Menos limites.

**Mãos no volante, olhos na estrada e liberdade para a sua multimídia.**

Drive. Explore. Play.

</div>

---

Você liga o carro e a música volta de onde parou. Aperta o botão do volante e a faixa pula.
Fala "Ok Google, tocar synthwave no Fiesta" e está tocando. O mapa continua na tela, o som
continua no ar.

Isso é o **Fiesta**: um app **driving-first** para o Android Auto, feito para quem gosta de dirigir.
Menos toques. Mais estrada.

## Feito para quem dirige

### 🎛️ O volante manda
Play, pause, próxima e anterior direto nos botões do carro. Sem procurar nada na tela.

### 🎙️ É só pedir
"Tocar X no Fiesta" pelo Google Assistente. Você pede, o Fiesta encontra e toca.

### 🌙 Tela fechada, som ligado
O Fiesta toca em segundo plano, no card de mídia do Android Auto, com capa, título e fila.
O mapa fica na frente; a trilha sonora, por trás.

### 🔁 Continua de onde parou
Desligou o carro no meio da música? Na próxima partida, ela volta sozinha, do mesmo ponto.

### 🧭 O painel do seu jeito
Cada carro tem uma tela e o Fiesta se ajusta a ela. A barra de ferramentas vai para o topo,
a base, a esquerda ou a direita e pode sumir para a página ocupar tudo, voltando com um toque
na borda. A **barra de favoritos** deixa seus sites a um toque e qualquer favorito vira
página inicial ali mesmo. Zoom para ler do banco do motorista. E tudo isso se ajusta **pelo
celular, ao vivo**.

### 🛡️ Seu e só seu
Bloqueador de anúncios e rastreadores ligado de fábrica, com controle por site. Zero
telemetria. Os dados de cada site ficam na sua mão.

### 🧩 Pronto para qualquer site
Plugins por site ensinam o Fiesta a falar com cada página: o que o volante faz, o que a tela
mostra. Motores de busca plugáveis e modo desktop por site completam o pacote.

## A estética

O Fiesta nasce da atmosfera **Night Drive**: estradas à noite, cultura **JDM** e
**retrofuturismo**, com influências de **Akira, Mid Night Club, Enduro, synthwave e Vice City**.
Neon, velocidade e liberdade: uma estética inspirada no passado, feita para a estrada.

Cada versão leva um codinome dessa estrada. A primeira é a **1.0.0 · Akira**.

## Fiesta × CarStream

O Fiesta nasceu do [CarStream](https://github.com/thekirankumar/carstream-android-auto) e foi
reconstruído pensando na estrada.

| Recurso | **Fiesta** | CarStream |
| --- | :---: | :---: |
| Comandos pelos botões do volante | **✅** | — |
| "Tocar X no Fiesta" pelo Google Assistente | **✅** | — |
| Música e vídeo no card de mídia do Android Auto | **✅** | — |
| Continua tocando com o mapa na tela | **✅** | — |
| Volta a tocar de onde parou ao ligar o carro | **✅** | — |
| Fila de reprodução no painel | **✅** | — |
| Barra de ferramentas onde você quiser: topo, base, esquerda ou direita | **✅** | — |
| Barra de ferramentas que some e volta com um toque na borda | **✅** | — |
| Barra de favoritos na tela do carro, liga e desliga | **✅** | — |
| Favoritar a página pelo carro e tornar qualquer favorito a página inicial num toque | **✅** | — |
| Zoom da página para ler do banco do motorista | **✅** | — |
| Plugins por site ([`docs/plugins.md`](docs/plugins.md)) | **✅** | — |
| Bloqueador de anúncios e rastreadores, por site | **✅** | — |
| Modo desktop por site | **✅** | — |
| Motores de busca plugáveis | **✅** | — |
| Mandar uma página do celular para o carro | **✅** | — |
| Ajustar o carro pelo celular, ao vivo | **✅** | — |
| Navegador no celular inspirado no Firefox Focus | **✅** | — |
| Controle dos dados de cada site | **✅** | — |
| Zero telemetria (sem Firebase) | **✅** | — |
| Busca por voz na tela do app | **✅** | ✅ |
| Navegador na tela do carro | **✅** | ✅ |
| Vídeo em tela cheia com ajuste de proporção | **✅** | ✅ |
| Teclado e sugestões de busca no carro | **✅** | ✅ |
| Favoritos | **✅** | ✅ |
| Modo de desbloqueio para aparelho com root | **✅** | ✅ |
| Player de arquivos locais | **—** | ✅ |
| Modo noturno por CSS remoto | **—** | ✅ |

## ⛽ Abasteça o Fiesta

<div align="center">

<img src="docs/images/donate.webp" alt="" width="720">

</div>

O Fiesta é **grátis**, **sem anúncios** e **sem rastreamento**. Ele é feito nas horas vagas, movido a café e a vontade de ver tudo funcionando na estrada.

Se ele já te acompanhou numa viagem, retribua com um café. Cada doação vira tempo de estrada:

- 🛠️ correções quando um site muda e algo para de funcionar;
- 🧩 novos plugins e novos comandos pelo volante;
- 🚗 testes em carro de verdade, não só no emulador.

<div align="center">

**☕ [Pagar um café](LINK_KOFI)** · **💖 [GitHub Sponsors](LINK_SPONSORS)** · **💠 [Pix](LINK_PIX)**

Não pode doar agora? Deixar uma ⭐ no GitHub e mostrar o Fiesta para um amigo também abastece.

</div>

## Antes de instalar

O Fiesta é um app **experimental**, assim como o CarStream. Ele não está na Google Play e não
se instala como um app comum: para aparecer no Android Auto, precisa de uma instalação
especial. Use por sua conta e risco e nunca mexa na tela enquanto dirige.

## Para desenvolvedores

Kotlin + AndroidX · JDK 17 · `minSdk 23`

```bash
./gradlew assembleDebug        # build
./gradlew testDebugUnitTest    # testes unitários
npm test                       # scripts de página
npm run e2e                    # ponta a ponta (emulador + DHU, defina ANDROID_SDK)
```

## Créditos

O Fiesta começou como um fork do
[`thekirankumar/carstream-android-auto`](https://github.com/thekirankumar/carstream-android-auto).

- [`cprcrack/VideoEnabledWebView`](https://github.com/cprcrack/VideoEnabledWebView) — base do
  suporte a vídeo em tela cheia.
- Ícones e fonte de terceiros: ver [`docs/TERCEIROS.md`](docs/TERCEIROS.md).

## Licença

[Apache 2.0](LICENSE).

---

<div align="center">

Curtiu? Deixe uma ⭐ no [GitHub](https://github.com/println/fiesta) ou [abasteça o Fiesta](#-abasteça-o-fiesta) ⛽.

**Drive. Explore. Play.**

Feito no 🇧🇷 Brasil.

</div>
