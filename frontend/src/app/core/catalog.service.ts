import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { catchError, of } from 'rxjs';

export interface Drone {
  id: string;
  nome: string;
  preco: number;
  estoque: number;
  categoria: string;
  resumo: string;
  autonomia: string;
  alcance: string;
  camera: string;
  destaque?: string;
}

const demoDrones: Drone[] = [
  { id: 'falcon-x2-pro', nome: 'Falcon X2 Pro', preco: 12499, estoque: 8, categoria: 'Profissionais', resumo: 'Precisao aerea para quem transforma cada voo em resultado.', autonomia: '43 min', alcance: '15 km', camera: '4K 60 fps', destaque: 'Lancamento' },
  { id: 'nimbus-mini-3', nome: 'Nimbus Mini 3', preco: 4290, estoque: 14, categoria: 'Compactos', resumo: 'Leve, silencioso e pronto para acompanhar qualquer roteiro.', autonomia: '34 min', alcance: '10 km', camera: '4K 30 fps', destaque: 'Oferta' },
  { id: 'heron-survey-pro', nome: 'Heron Survey Pro', preco: 28400, estoque: 3, categoria: 'Profissionais', resumo: 'Dados confiaveis para mapeamento, inspeção e topografia.', autonomia: '38 min', alcance: '12 km', camera: 'RTK', destaque: 'Pro' },
  { id: 'sparrow-kids', nome: 'Sparrow Kids', preco: 690, estoque: 22, categoria: 'Compactos', resumo: 'O primeiro voo com controle simples e estrutura resistente.', autonomia: '18 min', alcance: '2 km', camera: '2.7K', destaque: 'Oferta' },
];

@Injectable({ providedIn: 'root' })
export class CatalogService {
  private readonly http = inject(HttpClient);

  list() {
    return this.http.get<Drone[]>('/drones').pipe(catchError(() => of(demoDrones)));
  }

  find(id: string) {
    const local = demoDrones.find((drone) => drone.id === id);
    return this.http.get<Drone>(`/drones/${id}`).pipe(catchError(() => of(local ?? demoDrones[0])));
  }
}