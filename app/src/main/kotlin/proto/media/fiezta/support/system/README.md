# support/system

Dois utilitários independentes de plataforma, sem relação um com o outro além de morarem fora do
alcance de qualquer lib maior. Sem `androidx`, sem `R`, sem import do app.

- **`DrmDiagnostics.log()`** — abre um `MediaDrm` Widevine efêmero só para logar
  `securityLevel`/`hdcpLevel`/`maxHdcpLevel`/`version`/`systemId`/`vendor` via `Log.d` e depois
  libera. Cada propriedade é lida isoladamente — uma que falhar loga
  `<unavailable: ExceptionClass>` em vez de derrubar as outras. Sem suporte a Widevine no
  aparelho, cai num `catch` silencioso (`Log.d`, não crash). Ferramenta de diagnóstico, não
  afeta reprodução.
- **`UnlockUtils.unlock(activity)`** — hack de root, específico deste app: usa
  `libsuperuser.Shell.SU` para editar o banco `phenotype.db` do Google Play Services e incluir o
  `packageName` do app na whitelist (`app_white_list`) que o Android Auto oficial consulta antes
  de mostrar qualquer app de terceiro. Exige superusuário disponível (`Shell.SU.available()`);
  sem root, só mostra um `Toast` e sai. **Não é reaproveitável em outro app sem mudar a lógica**
  — a lista de pacotes que ele grava inclui `activity.applicationContext.packageName`, ou seja, o
  hack sempre libera quem o está chamando, não um pacote arbitrário passado por fora.

## Testes

Nenhum dos dois tem teste — ambos são só efeito colateral sobre APIs do sistema
(`MediaDrm`/`Shell`/SQLite), não lógica que valha a pena isolar.
