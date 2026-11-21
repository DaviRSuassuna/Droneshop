/**
 * "Reduzir movimento" ligado no sistema (DESIGN.md, Movimento). As diretivas de efeito
 * consultam isto e, quando verdadeiro, deixam a tela pronta sem animar.
 */
export function reduzirMovimento(janela: Window | null): boolean {
  return !!janela?.matchMedia?.('(prefers-reduced-motion: reduce)').matches;
}
