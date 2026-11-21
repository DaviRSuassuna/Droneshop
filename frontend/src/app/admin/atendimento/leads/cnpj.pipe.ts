import { Pipe, PipeTransform } from '@angular/core';
import { aplicarMascara } from '../../../core/utils/documentos';

/** CNPJ só com dígitos -> 00.000.000/0000-00. */
@Pipe({ name: 'cnpj' })
export class CnpjPipe implements PipeTransform {
  transform(valor: string | null | undefined): string {
    return valor ? aplicarMascara(valor, 'cnpj') : '';
  }
}
