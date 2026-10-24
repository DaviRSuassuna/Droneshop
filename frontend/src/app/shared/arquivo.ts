/** Baixa a resposta binária da API usando o navegador. */
export function baixarArquivo(arquivo: string, blob: Blob): void {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = arquivo;
  link.click();
  setTimeout(() => URL.revokeObjectURL(url), 0);
}
