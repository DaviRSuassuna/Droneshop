import { Pipe, PipeTransform } from '@angular/core';
import { aplicarMascara } from '../../core/utils/documentos';

/** CEP vem da API só com dígitos; a máscara 00000-000 é só de exibição. */
export function formatarCep(cep: string | null | undefined): string {
  return cep ? aplicarMascara(cep, 'cep') : '';
}

@Pipe({ name: 'cep', standalone: true })
export class CepPipe implements PipeTransform {
  transform(cep: string | null | undefined): string {
    return formatarCep(cep);
  }
}
