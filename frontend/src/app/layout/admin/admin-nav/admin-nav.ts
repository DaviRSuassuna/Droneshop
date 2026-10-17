import { Component, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

interface ItemNav {
  rotulo: string;
  rota?: string;
  /** Só marca como ativo na rota exata (o Dashboard é /admin, prefixo de todas as outras). */
  exata?: boolean;
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
  // Somente "Dashboard", "Produtos", "Marcas", "Categorias", "Subcategorias", "Fornecedores",
  // "Configuração", "Estoque", "Pedidos" e "Promoções" estão implementados nesta etapa; os demais
  // módulos do admin ainda não têm rota e por isso aparecem como itens inertes.
  readonly itens: ItemNav[] = [
    { rotulo: 'Dashboard', rota: '/admin', exata: true },
    { rotulo: 'Produtos', rota: '/admin/produtos' },
    { rotulo: 'Marcas', rota: '/admin/marcas' },
    { rotulo: 'Categorias', rota: '/admin/categorias' },
    { rotulo: 'Subcategorias', rota: '/admin/subcategorias' },
    { rotulo: 'Fornecedores', rota: '/admin/fornecedores' },
    { rotulo: 'Configuração', rota: '/admin/configuracao' },
    { rotulo: 'Promoções', rota: '/admin/promocoes' },
    { rotulo: 'Cupons' },
    { rotulo: 'Estoque', rota: '/admin/estoque' },
    { rotulo: 'Pedidos', rota: '/admin/pedidos' },
    { rotulo: 'Usuários', rota: '/admin/usuarios' },
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
