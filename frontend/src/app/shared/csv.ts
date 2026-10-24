/** Gera e baixa um CSV (uma linha por array, com aspas escapadas), como o "Exportar CSV" do protótipo. */
export function baixarCsv(arquivo: string, linhas: (string | number | null)[][]): void {
  const csv = linhas
    .map((linha) => linha.map((v) => `"${String(v ?? '').replace(/"/g, '""')}"`).join(','))
    .join('\n');
  const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8;' }));
  const a = document.createElement('a');
  a.href = url;
  a.download = arquivo;
  a.click();
  URL.revokeObjectURL(url);
}
