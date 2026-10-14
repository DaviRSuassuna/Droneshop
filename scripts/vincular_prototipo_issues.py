#!/usr/bin/env python3
"""Liga as telas do protótipo às issues.

Lê a seção "### Protótipo" de cada docs/issues/*.md, confere se cada tela citada
existe em docs/design/prototipo/ e escreve na issue um bloco com o link da
versão publicada (GitHub Pages), entre <!-- prototipo:inicio --> e
<!-- prototipo:fim -->, sem mexer no resto do corpo.

Uso:
  python scripts/vincular_prototipo_issues.py --verificar   # só confere as telas (sem gh)
  python scripts/vincular_prototipo_issues.py --dry-run     # mostra o que escreveria
  python scripts/vincular_prototipo_issues.py               # escreve (precisa do gh e de GH_TOKEN)
"""
import argparse
import json
import pathlib
import re
import subprocess
import sys

RAIZ = pathlib.Path(__file__).resolve().parent.parent
PASTA = RAIZ / "docs/issues"
PROTOTIPO = RAIZ / "docs/design/prototipo"
PAGES = "https://davirsuassuna.github.io/droneshop/prototipo/"
INICIO, FIM = "<!-- prototipo:inicio -->", "<!-- prototipo:fim -->"
RE_TELA = re.compile(r"((?:admin/)?[a-z0-9-]+\.html)")


def gh(*args, entrada=None):
    r = subprocess.run(["gh", *args], input=entrada, capture_output=True, text=True)
    if r.returncode != 0:
        raise RuntimeError(f"gh {' '.join(args[:3])}...: {r.stderr.strip()}")
    return r.stdout


def telas_das_issues():
    """[(arquivo, meta, [telas])]"""
    saida = []
    for arq in sorted(PASTA.glob("*.md")):
        texto = arq.read_text(encoding="utf-8")
        m = re.match(r"---\n(.*?)\n---\n", texto, re.S)
        if not m:
            continue
        meta = {l.split(":", 1)[0].strip(): json.loads(l.split(":", 1)[1]) for l in m.group(1).splitlines()}
        sec = re.search(r"### Protótipo\n(.*?)(?:\n### |\Z)", texto, re.S)
        telas = []
        if sec:
            for t in RE_TELA.findall(re.sub(r"\]\([^)]*\)", "]", sec.group(1))):
                if t not in telas:
                    telas.append(t)
        saida.append((arq, meta, telas))
    return saida


def achar_por_titulo(titulo, repo):
    achadas = json.loads(gh("issue", "list", "-R", repo, "--state", "all", "--search", f'"{titulo}" in:title',
                            "--json", "number,title", "--limit", "30"))
    return next((i["number"] for i in achadas if i["title"] == titulo), None)


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--verificar", action="store_true", help="só confere se as telas existem")
    ap.add_argument("--dry-run", action="store_true")
    ap.add_argument("--repo", default="DaviRSuassuna/droneshop")
    args = ap.parse_args()

    dados = telas_das_issues()
    faltando = [(a.name, t) for a, _, telas in dados for t in telas if not (PROTOTIPO / t).exists()]
    for nome, tela in faltando:
        print(f"::error::{nome}: a tela {tela} não existe em docs/design/prototipo/")
    if args.verificar or faltando:
        print(f"{sum(len(t) for _, _, t in dados)} referências a telas, {len(faltando)} inexistentes.")
        return 1 if faltando else 0

    for arq, meta, telas in dados:
        if not telas:
            continue
        numero = meta.get("numero") or achar_por_titulo(meta["titulo"], args.repo)
        if not numero:
            print(f"::warning::{arq.name}: issue não encontrada")
            continue
        bloco = "\n".join([INICIO, "### Telas no protótipo publicado", ""]
                          + [f"- [{t}]({PAGES}{t})" for t in telas]
                          + ["", "_Atualizado pelo workflow `vincular-prototipo`._", FIM])
        if args.dry_run:
            print(f"#{numero}: {', '.join(telas)}")
            continue
        corpo = json.loads(gh("issue", "view", str(numero), "-R", args.repo, "--json", "body"))["body"] or ""
        if INICIO in corpo and FIM in corpo:
            novo = corpo[: corpo.index(INICIO)] + bloco + corpo[corpo.index(FIM) + len(FIM):]
        else:
            novo = corpo.rstrip() + "\n\n" + bloco + "\n"
        if novo.strip() != corpo.strip():
            gh("issue", "edit", str(numero), "-R", args.repo, "--body-file", "-", entrada=novo)
            print(f"#{numero} atualizada")
    return 0


if __name__ == "__main__":
    sys.exit(main())
