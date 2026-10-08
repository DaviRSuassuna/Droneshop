import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * CNPJ, CEP e telefone trafegam e ficam no banco só com dígitos.
 * A máscara existe apenas na tela (diretiva appMascara e pipes cnpj/cep/telefone).
 */
export function somenteDigitos(valor: string | null | undefined): string {
  return (valor ?? '').replace(/\D/g, '');
}

export type TipoMascara = 'cnpj' | 'cep' | 'telefone';

/** Formata enquanto o usuário digita (aceita valor parcial). */
export function aplicarMascara(valor: string, tipo: TipoMascara): string {
  const d = somenteDigitos(valor);
  switch (tipo) {
    case 'cnpj': {
      const n = d.slice(0, 14);
      return n
        .replace(/^(\d{2})(\d)/, '$1.$2')
        .replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3')
        .replace(/\.(\d{3})(\d)/, '.$1/$2')
        .replace(/(\d{4})(\d)/, '$1-$2');
    }
    case 'cep': {
      const n = d.slice(0, 8);
      return n.length > 5 ? `${n.slice(0, 5)}-${n.slice(5)}` : n;
    }
    case 'telefone': {
      const n = d.slice(0, 11);
      if (n.length === 0) return '';
      if (n.length <= 2) return `(${n}`;
      const ddd = n.slice(0, 2);
      const resto = n.slice(2);
      // 9 dígitos depois do DDD = celular (00000-0000); senão fixo (0000-0000)
      const corte = resto.length > 8 ? 5 : 4;
      return resto.length > corte
        ? `(${ddd}) ${resto.slice(0, corte)}-${resto.slice(corte)}`
        : `(${ddd}) ${resto}`;
    }
  }
}

/** Valida os dígitos verificadores do CNPJ (mesma regra do @CNPJ do back). */
export function cnpjValido(cnpj: string): boolean {
  const d = somenteDigitos(cnpj);
  if (d.length !== 14 || /^(\d)\1{13}$/.test(d)) return false;
  const calc = (base: string, pesos: number[]) => {
    const soma = pesos.reduce((acc, peso, i) => acc + Number(base[i]) * peso, 0);
    const resto = soma % 11;
    return resto < 2 ? 0 : 11 - resto;
  };
  const dv1 = calc(d, [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]);
  const dv2 = calc(d, [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]);
  return dv1 === Number(d[12]) && dv2 === Number(d[13]);
}

/** Validator de formulário: vazio passa (quem exige é o Validators.required). */
export const cnpjValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const valor = String(control.value ?? '');
  if (!valor) return null;
  return cnpjValido(valor) ? null : { cnpj: true };
};
