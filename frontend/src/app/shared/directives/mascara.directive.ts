import { Directive, inject, input } from '@angular/core';
import { NgControl } from '@angular/forms';
import { TipoMascara, aplicarMascara } from '../../core/utils/documentos';

/**
 * Aplica a máscara enquanto o usuário digita: <input formControlName="cep" appMascara="cep" />.
 * O controle guarda o valor mascarado; o componente tira a máscara (somenteDigitos) antes de enviar.
 */
@Directive({
  selector: 'input[appMascara]',
  standalone: true,
  host: { '(input)': 'aoDigitar()' },
})
export class MascaraDirective {
  readonly appMascara = input.required<TipoMascara>();
  private readonly ngControl = inject(NgControl, { self: true });

  aoDigitar(): void {
    const control = this.ngControl.control;
    if (!control) return;
    const atual = String(control.value ?? '');
    const formatado = aplicarMascara(atual, this.appMascara());
    if (formatado !== atual) control.setValue(formatado);
  }
}
