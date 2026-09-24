# support/search

Modelo de motor de busca: define o que é um `SearchEngine`, escolhe o certo para uma URL/host
(`SearchEngineSelector`) e faz o parse de descrições OpenSearch (com uma extensão de namespace
própria) em definições de motor (`OpenSearchParser`/`DescribedSearchEngine`). Kotlin puro — sem
`android.*`, sem `androidx`, sem `Context`.

O que vem de fora entra por contrato: `SearchEngineFiles` entrega a ordem e o fallback
(`SearchEngineDefaults`) e abre cada descrição. `SearchEngineCatalog.load(files)` monta o
seletor e recusa fallback fora da ordem ou descrição cujo `Id` não bate com o nome do arquivo.
A implementação sobre `assets/search/` é `shared/search/AssetSearchEngineFiles`.

Textos e ícones de tela (dica do campo, título, "ouvindo…", ícone) não são do motor e não estão
aqui: moram em `shared/search/SearchEngineTextsById`, escolhidos pelo `id`
(`engine.texts`).

## Entrada

```kotlin
interface SearchEngine {
    val id: String
    fun handles(host: String): Boolean
    fun searchUrl(query: String): String
    fun suggestionsUrl(query: String): String?
}

interface SearchEngineFiles {
    fun readDefaults(): SearchEngineDefaults
    fun openDescription(engineId: String): InputStream
}

object SearchEngineCatalog {
    fun load(files: SearchEngineFiles): SearchEngineSelector
}

class SearchEngineSelector(val engines: List<SearchEngine>, val fallback: SearchEngine) {
    fun forUrl(url: String?): SearchEngine
}

object OpenSearchParser {
    fun parse(input: InputStream): SearchEngineDescription  // throws InvalidSearchEngineException
}
```

## O parser

`http://a9.com/-/spec/opensearch/1.1/` é o namespace OpenSearch padrão (`ShortName`, `Url`,
`InputEncoding`); `urn:fiesta:search:1` é a extensão própria (`Id`, `Version`, `Host`). Exige
exatamente um `Url` `type="text/html"` (o template de busca) e no máximo um
`type="application/x-suggestions+json"`. O template tem que começar com `https://` e conter
**exatamente um** `{searchTerms}` — `<Param>` não é suportado e é rejeitado. `InputEncoding`,
se presente, só aceita `UTF-8`.

## Regras que não dá para descobrir lendo por cima

- **`forUrl` nunca lança.** URL nula, em branco ou malformada cai direto no `fallback` — não há
  exceção para o chamador tratar.
- **`*.example.com` não casa `example.com`.** `HostPattern.matches` exige um host estritamente
  mais longo que o sufixo com ponto — o domínio nu não é subdomínio de si mesmo.
- **A query é `application/x-www-form-urlencoded`, não `%20`.** `URLEncoder.encode(query,
  "UTF-8")` transforma espaço em `+`; não é configurável por motor.

## Testes

`app/src/test/kotlin/proto/media/fiesta/support/search/` — JUnit puro, incluindo o parser
e o `SearchEngineCatalog` contra os arquivos reais de `assets/search/` (via `AssetEngines.kt` e
um `SearchEngineFiles` que lê do disco).
