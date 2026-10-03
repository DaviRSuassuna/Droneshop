import { Pipe, PipeTransform } from '@angular/core';
import { aplicarMascara } from '../../core/utils/documentos';

/** Telefone vem da API só com dígitos (DDD + número); a máscara é só de exibição. */
export function formatarTelefone(telefone: string | null | undefined): string {
  return telefone ? aplicarMascara(telefone, 'telefone') : '';
}

@Pipe({ name: 'telefone', standalone: true })
export class TelefonePipe implements PipeTransform {
  transform(telefone: string | null | undefined): string {
    return formatarTelefone(telefone);
  }
}
