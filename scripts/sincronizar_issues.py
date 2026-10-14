#!/usr/bin/env python3
"""Sincroniza as issues do GitHub com os arquivos de docs/issues/.

Cada arquivo tem um cabeçalho entre linhas "---" (numero, titulo, labels, fase,
um valor JSON por linha) e, depois dele, o corpo da issue em Markdown.

  - numero preenchido: atualiza título, corpo e labels da issue existente;
  - numero null (arquivos nova-*.md): procura a issue pelo título exato; se não
    achar, cria. Rodar de novo não duplica.
  - fase: põe a issue no milestone cujo título contém "Fase <n>", se existir.

Os blocos que os outros workflows escrevem na issue (testes e protótipo, entre
marcadores <!-- ...:inicio --> e <!-- ...:fim -->) são preservados.

Uso (precisa do gh autenticado, GH_TOKEN no CI):
  python scripts/sincronizar_issues.py --dry-run          # só mostra
  python scripts/sincronizar_issues.py                    # aplica
  python scripts/sincronizar_issues.py --somente 7,9      # só essas
"""
import argparse
import json
import pathlib
import re
import subprocess
import sys

RAIZ = pathlib.Path(__file__).resolve().parent.parent
PASTA = RAIZ / "docs/issues"
RE_BLOCO = re.compile(r"<!-- (\w+):inicio -->.*?<!-- \1:fim -->", re.S)


def gh(*args, entrada=None):
    r = subprocess.run(["gh", *args], input=entrada, capture_output=True, text=True)
    if r.returncode != 0:
        raise RuntimeError(f"gh {' '.join(args[:3])}...: {r.stderr.strip()}")
    return r.stdout


def ler(arq):
    texto = arq.read_text(encoding="utf-8")
    m = re.match(r"---\n(.*?)\n---\n", texto, re.S)
    if not m:
        raise ValueError(f"{arq.name}: falta o cabeçalho entre ---")
    meta = {}
    for linha in m.group(1).splitlines():
        chave, valor = linha.split(":", 1)
        meta[chave.strip()] = json.loads(valor.strip())
    return meta, texto[m.end():].strip() + "\n"


def milestones(repo):
    try:
        return json.loads(gh("api", f"repos/{repo}/milestones?state=all&per_page=100"))
    except RuntimeError as e:
        print(f"::warning::milestones indisponíveis: {e}")
        return []


def milestone_da_fase(fase, lista):
    if fase is None:
        return None
    for m in lista:
        if re.search(rf"\bfase\s*{fase}\b", m["title"], re.I):
            return m["title"]
    return None


def preservar_blocos(corpo_novo, corpo_atual):
    for bloco in RE_BLOCO.finditer(corpo_atual or ""):
        if f"<!-- {bloco.group(1)}:inicio -->" not in corpo_novo:
            corpo_novo = corpo_novo.rstrip() + "\n\n" + bloco.group(0) + "\n"
    return corpo_novo


def achar_por_titulo(titulo, repo):
    achadas = json.loads(gh("issue", "list", "-R", repo, "--state", "all", "--search", f'"{titulo}" in:title',
                            "--json", "number,title", "--limit", "30"))
    for i in achadas:
        if i["title"] == titulo:
            return i["number"]
    return None


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--dry-run", action="store_true")
    ap.add_argument("--somente", default="", help="números separados por vírgula (vazio = todas)")
    ap.add_argument("--repo", default="DaviRSuassuna/droneshop")
    args = ap.parse_args()
    somente = {s.strip() for s in args.somente.split(",") if s.strip()}

    lista_ms = milestones(args.repo)
    falhas = 0
    for arq in sorted(PASTA.glob("*.md")):
        if arq.name.lower() == "readme.md":
            continue
        meta, corpo = ler(arq)
        numero = meta.get("numero")
        if somente and str(numero) not in somente and arq.stem not in somente:
            continue
        titulo, labels = meta["titulo"], meta.get("labels", [])
        ms = milestone_da_fase(meta.get("fase"), lista_ms)
        try:
            if numero is None:
                numero = achar_por_titulo(titulo, args.repo)
            if numero is None:
                print(f"CRIAR  {arq.name}: {titulo}" + (f" [{ms}]" if ms else ""))
                if not args.dry_run:
                    cmd = ["issue", "create", "-R", args.repo, "--title", titulo, "--body-file", "-"]
                    for l in labels:
                        cmd += ["--label", l]
                    if ms:
                        cmd += ["--milestone", ms]
                    print("       " + gh(*cmd, entrada=corpo).strip())
                continue
            atual = json.loads(gh("issue", "view", str(numero), "-R", args.repo, "--json", "body,title,labels"))
            novo = preservar_blocos(corpo, atual["body"])
            muda = novo.strip() != (atual["body"] or "").strip() or titulo != atual["title"]
            faltam = sorted(set(labels) - {l["name"] for l in atual["labels"]})
            print(f"{'EDITAR' if muda or faltam else 'IGUAL '} #{numero} {arq.name}" + (f" +labels {faltam}" if faltam else ""))
            if (muda or faltam) and not args.dry_run:
                cmd = ["issue", "edit", str(numero), "-R", args.repo, "--title", titulo, "--body-file", "-"]
                for l in faltam:
                    cmd += ["--add-label", l]
                if ms:
                    cmd += ["--milestone", ms]
                gh(*cmd, entrada=novo)
        except RuntimeError as e:
            falhas += 1
            print(f"::error::{arq.name}: {e}")
    return 1 if falhas else 0


if __name__ == "__main__":
    sys.exit(main())
