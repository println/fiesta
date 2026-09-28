# Materiais de terceiros

Registro de assets copiados de outros projetos, exigido pelas respectivas licenças.

O Fiesta é GPL-3.0-or-later (`LICENSE`), com a permissão adicional de
`LICENSES/GPL-3.0-aauto-sdk-exception.txt`. As partes abaixo mantêm as próprias licenças.

## Código — CarStream

- **Origem:** [`thekirankumar/carstream-android-auto`](https://github.com/thekirankumar/carstream-android-auto),
  de onde o Fiesta foi criado como fork, modificado desde então por println
- **Licença:** Apache License 2.0 (texto em `LICENSES/Apache-2.0.txt`; o upstream não tem `NOTICE`)

## Código — VideoEnabledWebView

- **Origem:** [`cprcrack/VideoEnabledWebView`](https://github.com/cprcrack/VideoEnabledWebView),
  base de `support/webviewex/VideoWebView.kt` e `support/webviewex/WebViewExChromeClient.kt`
- **Licença:** MIT, Copyright (c) 2014 Cristian Perez (texto em
  `LICENSES/MIT-VideoEnabledWebView.txt`)

## Biblioteca — aauto-sdk

- **Origem:** `com.github.martoreto:aauto-sdk:v4.7`, via JitPack
  ([`martoreto/aauto-sdk`](https://github.com/martoreto/aauto-sdk))
- **Licença:** nenhuma declarada; o autor retirou o SDK a pedido do Google. A permissão
  adicional da GPL cobre só a combinação com o Fiesta, não a redistribuição do SDK.

## Ícones — Material Symbols

- **Origem:** [`google/material-design-icons`](https://github.com/google/material-design-icons),
  `symbols/android/*/materialsymbolsoutlined/`
- **Licença:** Apache License 2.0
- **Arquivos:** os `ic_car_*.xml` tirados de lá

## Ícones — `mozac_ic_*.xml`

- **Origem:** [`mozilla-mobile/android-components`](https://github.com/mozilla-mobile/android-components),
  `components/ui/icons/src/main/res/drawable/`
- **Licença:** Mozilla Public License 2.0 (cabeçalho preservado em cada arquivo,
  sem alteração)
- **Data da cópia:** 2026-09-09
- **Arquivos:** `mozac_ic_delete.xml`, `mozac_ic_shield.xml`,
  `mozac_ic_shield_disabled.xml`, `mozac_ic_clear.xml`,
  `mozac_ic_add_to_home_screen.xml`, `mozac_ic_menu.xml`, `mozac_ic_back.xml`,
  `mozac_ic_forward.xml`, `mozac_ic_share.xml`, `mozac_ic_refresh.xml`,
  `mozac_ic_pin.xml`, `mozac_ic_open_in.xml`, `mozac_ic_settings.xml`,
  `mozac_ic_bookmark.xml`, `mozac_ic_warning.xml`

## Fonte — Inter

- **Origem:** [`rsms/inter`](https://github.com/rsms/inter), release v4.1,
  `extras/ttf/Inter-Regular.ttf` e `extras/ttf/Inter-Bold.ttf`; Google Fonts
  (instância estática `ital,wght@1,300`) para `inter_light_italic.ttf`
- **Licença:** SIL Open Font License 1.1 (texto completo em
  `app/src/main/assets/fonts/OFL-Inter.txt`)
- **Data da cópia:** 2026-09-09
- **Arquivos:** `app/src/main/res/font/inter_regular.ttf`,
  `app/src/main/res/font/inter_bold.ttf`,
  `app/src/main/res/font/inter_light_italic.ttf`
