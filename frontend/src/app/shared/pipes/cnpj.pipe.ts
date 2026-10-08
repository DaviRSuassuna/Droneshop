import { Pipe, PipeTransform } from '@angular/core';

/**
 * O CNPJ vem da API só com os 14 dígitos (convenção do banco);
 * a máscara 00.000.000/0000-00 é aplicada apenas na exibição.
 */
export function formatarCnpj(cnpj: string | null | undefined): string {
  const digitos = (cnpj ?? '').replace(/\D/g, '');
  if (digitos.length !== 14) return cnpj ?? '';
  return digitos.replace(/^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})$/, '$1.$2.$3/$4-$5');
}

@Pipe({ name: 'cnpj', standalone: true })
export class CnpjPipe implements PipeTransform {
  transform(cnpj: string | null | undefined): string {
    return formatarCnpj(cnpj);
  }
}
