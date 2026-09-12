import { Component, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

interface ItemNav {
  rotulo: string;
  rota?: string;
}

const CHAVE_TEMA = 'aeria-theme';

@Component({
  selector: 'app-admin-nav',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './admin-nav.html',
  styleUrl: './admin-nav.scss',
})
export class AdminNav {
  // Somente "Marcas" está implementado nesta etapa; os demais módulos
  // do admin ainda não têm rota e por isso aparecem como itens inertes.
  readonly itens: ItemNav[] = [
    { rotulo: 'Dashboard' },
    { rotulo: 'Produtos' },
    { rotulo: 'Marcas', rota: '/admin/marcas' },
    { rotulo: 'Categorias' },
    { rotulo: 'Fornecedores' },
    { rotulo: 'Promoções' },
    { rotulo: 'Cupons' },
    { rotulo: 'Estoque' },
    { rotulo: 'Pedidos' },
    { rotulo: 'Usuários' },
  ];

  readonly tema = signal(this.temaInicial());

  private temaInicial(): 'light' | 'dark' {
    const salvo = localStorage.getItem(CHAVE_TEMA);
    return salvo === 'dark' ? 'dark' : 'light';
  }

  constructor() {
    document.documentElement.setAttribute('data-theme', this.tema());
  }

  alternarTema(): void {
    const novo = this.tema() === 'light' ? 'dark' : 'light';
    this.tema.set(novo);
    document.documentElement.setAttribute('data-theme', novo);
    localStorage.setItem(CHAVE_TEMA, novo);
  }
}
