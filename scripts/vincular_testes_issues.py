#!/usr/bin/env python3
"""Liga os testes do backend às issues.

Lê os @Tag dos testes Java (backend/src/test/java) e:
  - escreve em cada issue um bloco "Testes automatizados" entre marcadores HTML
    (<!-- testes:inicio --> ... <!-- testes:fim -->), sem mexer no resto do corpo;
  - com --matriz, atualiza a matriz issue x testes e a lista de testes pulados
    do backend/src/test/README.md (também entre marcadores).

Etiquetas aceitas: issue-<numero> (issue existente) e issue-<nome>, para issues
novas descritas em docs/issues/nova-<nome>.md (a issue é achada pelo título).
A etiqueta "base" não tem issue.

Uso:
  python scripts/vincular_testes_issues.py --matriz          # só o README (local)
  python scripts/vincular_testes_issues.py --dry-run         # mostra o que mudaria nas issues
  python scripts/vincular_testes_issues.py                   # escreve nas issues (precisa do gh e de GH_TOKEN)
"""
import argparse
import json
import pathlib
import re
import subprocess
import sys

RAIZ = pathlib.Path(__file__).resolve().parent.parent
TESTES = RAIZ / "backend/src/test/java"
ENTREGUES = RAIZ / "backend/src/test/issues-entregues.txt"
README = RAIZ / "backend/src/test/README.md"
ISSUES = RAIZ / "docs/issues"
INICIO, FIM = "<!-- testes:inicio -->", "<!-- testes:fim -->"
BLOB = "https://github.com/{repo}/blob/develop/backend/src/test/java/"

RE_TAG = re.compile(r'@Tag\("([^"]+)"\)')
RE_DISABLED = re.compile(r'@Disabled\("([^"]*)"\)')
RE_CLASSE = re.compile(r"^\s*(?:public\s+)?(?:final\s+)?class\s+(\w+)", re.M)
RE_METODO = re.compile(r"^\s*(?:public\s+|protected\s+)?void\s+(\w+)\s*\(", re.M)


def ler_testes():
    """{etiqueta: [ {arquivo, classe, metodo, pulado} ]}"""
    por_tag = {}
    for arq in sorted(TESTES.rglob("*.java")):
        texto = arq.read_text(encoding="utf-8")
        m_classe = RE_CLASSE.search(texto)
        if not m_classe or "@Test" not in texto:
            continue
        cabecalho = texto[: m_classe.start()]
        tags_classe = RE_TAG.findall(cabecalho)
        pulado_classe = RE_DISABLED.findall(cabecalho)
        corpo = texto[m_classe.end():]
        # cada @Test com as anotações que vêm antes dele (até a assinatura do método)
        for bloco in re.finditer(r"((?:\s*@\w+(?:\([^)]*\))?\s*)+)\s*(?:public\s+|protected\s+)?void\s+(\w+)\s*\(", corpo):
            anot, metodo = bloco.group(1), bloco.group(2)
            if "@Test" not in anot:
                continue
            tags = tags_classe + RE_TAG.findall(anot)
            pulado = (RE_DISABLED.findall(anot) or pulado_classe or [None])[0]
            rel = arq.relative_to(TESTES).as_posix()
            for t in tags or ["(sem etiqueta)"]:
                por_tag.setdefault(t, []).append(
                    {"arquivo": rel, "classe": m_classe.group(1), "metodo": metodo, "pulado": pulado})
    return por_tag


def ler_entregues():
    if not ENTREGUES.exists():
        return set()
    return {l.strip() for l in ENTREGUES.read_text(encoding="utf-8").splitlines()
            if l.strip() and not l.strip().startswith("#")}


def chave_ordem(tag):
    m = re.fullmatch(r"issue-(\d+)", tag)
    return (0, "") if tag == "base" else ((1, int(m.group(1))) if m else (2, tag))


def substituir_bloco(texto, inicio, fim, novo):
    if inicio in texto and fim in texto:
        a, b = texto.index(inicio), texto.index(fim) + len(fim)
        return texto[:a] + novo + texto[b:]
    return texto.rstrip() + "\n\n" + novo + "\n"


def atualizar_readme(por_tag, entregues):
    linhas = ["<!-- matriz:inicio -->", "", "| Etiqueta | Situação no CI | Arquivos | Testes |", "|---|---|---|---:|"]
    for tag in sorted(por_tag, key=chave_ordem):
        testes = por_tag[tag]
        arquivos = sorted({t["arquivo"].removesuffix(".java").removeprefix("br/com/droneshop/") for t in testes})
        situacao = "bloqueia o merge" if tag in entregues else "informativo"
        linhas.append(f"| `{tag}` | {situacao} | {', '.join(f'`{a}`' for a in arquivos)} | {len(testes)} |")
    linhas += ["", f"Total: {sum(len(v) for v in por_tag.values())} testes. Gerado por `scripts/vincular_testes_issues.py --matriz`.",
               "", "<!-- matriz:fim -->"]
    pulados = [(tag, t) for tag in sorted(por_tag, key=chave_ordem) for t in por_tag[tag] if t["pulado"]]
    lp = ["<!-- pulados:inicio -->", ""]
    if pulados:
        lp += ["| Etiqueta | Teste | Motivo |", "|---|---|---|"]
        lp += [f"| `{tag}` | `{t['classe']}#{t['metodo']}` | {t['pulado']} |" for tag, t in pulados]
    else:
        lp.append("Nenhum no momento.")
    lp += ["", "<!-- pulados:fim -->"]
    texto = README.read_text(encoding="utf-8")
    texto = substituir_bloco(texto, "<!-- matriz:inicio -->", "<!-- matriz:fim -->", "\n".join(linhas))
    texto = substituir_bloco(texto, "<!-- pulados:inicio -->", "<!-- pulados:fim -->", "\n".join(lp))
    README.write_text(texto, encoding="utf-8")
    print(f"README atualizado: {len(por_tag)} etiquetas.")


def gh(*args, entrada=None):
    r = subprocess.run(["gh", *args], input=entrada, capture_output=True, text=True)
    if r.returncode != 0:
        raise RuntimeError(f"gh {' '.join(args)}: {r.stderr.strip()}")
    return r.stdout


def titulo_da_nova(nome):
    arq = ISSUES / f"nova-{nome}.md"
    if not arq.exists():
        return None
    for linha in arq.read_text(encoding="utf-8").splitlines():
        if linha.startswith("titulo:"):
            return json.loads(linha.split(":", 1)[1])
    return None


def numero_da_issue(tag, repo):
    m = re.fullmatch(r"issue-(\d+)", tag)
    if m:
        return int(m.group(1))
    m = re.fullmatch(r"issue-([\w-]+)", tag)
    titulo = titulo_da_nova(m.group(1)) if m else None
    if not titulo:
        return None
    achadas = json.loads(gh("issue", "list", "-R", repo, "--state", "all", "--search", f'"{titulo}" in:title',
                            "--json", "number,title", "--limit", "20"))
    for i in achadas:
        if i["title"] == titulo:
            return i["number"]
    return None


def bloco_issue(tag, testes, entregue, repo):
    base = BLOB.format(repo=repo)
    situacao = ("**entregue**: estes testes bloqueiam o merge se falharem" if entregue
                else "ainda não entregue: no CI estes testes só informam")
    linhas = [INICIO, "### Testes automatizados", "",
              f"Etiqueta `{tag}`, {situacao}. Rodar: `./mvnw test -Dgroups={tag}`.", "",
              "| Teste | Situação |", "|---|---|"]
    for t in testes:
        sit = f"pulado: {t['pulado']}" if t["pulado"] else ("bloqueia" if entregue else "informativo")
        linhas.append(f"| [`{t['classe']}#{t['metodo']}`]({base}{t['arquivo']}) | {sit} |")
    linhas += ["", "_Atualizado pelo workflow `vincular-testes`._", FIM]
    return "\n".join(linhas)


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--matriz", action="store_true", help="só atualiza a matriz do README dos testes")
    ap.add_argument("--dry-run", action="store_true", help="mostra o que seria escrito nas issues, sem escrever")
    ap.add_argument("--repo", default="DaviRSuassuna/droneshop")
    args = ap.parse_args()

    por_tag = ler_testes()
    entregues = ler_entregues()
    if args.matriz:
        atualizar_readme(por_tag, entregues)
        return 0

    erros = 0
    for tag in sorted(por_tag, key=chave_ordem):
        if not tag.startswith("issue-"):
            continue
        try:
            numero = numero_da_issue(tag, args.repo)
        except RuntimeError as e:
            print(f"::warning::{tag}: {e}")
            erros += 1
            continue
        if not numero:
            print(f"::warning::{tag}: issue não encontrada (rode o workflow sincronizar-issues antes)")
            continue
        bloco = bloco_issue(tag, por_tag[tag], tag in entregues, args.repo)
        if args.dry_run:
            print(f"--- #{numero} ({tag}): {len(por_tag[tag])} testes")
            continue
        corpo = json.loads(gh("issue", "view", str(numero), "-R", args.repo, "--json", "body"))["body"] or ""
        novo = substituir_bloco(corpo, INICIO, FIM, bloco)
        if novo.strip() != corpo.strip():
            gh("issue", "edit", str(numero), "-R", args.repo, "--body-file", "-", entrada=novo)
            print(f"#{numero} atualizada ({tag})")
        else:
            print(f"#{numero} sem mudança")
    return 1 if erros else 0


if __name__ == "__main__":
    sys.exit(main())
