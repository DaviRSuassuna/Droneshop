import { describe, expect, it } from 'vitest';
import { absolutizarMidias } from './midia-url.interceptor';

describe('absolutizarMidias', () => {
  const api = 'http://localhost:8080';

  it('prefixa os caminhos de mídia em qualquer nível do JSON', () => {
    const corpo = {
      items: [{ nome: 'Falcon', imagemPrincipal: '/midias/arquivos/a.webp', galeria: ['/midias/arquivos/b.png'] }],
    };
    expect(absolutizarMidias(corpo, api)).toEqual({
      items: [
        {
          nome: 'Falcon',
          imagemPrincipal: 'http://localhost:8080/midias/arquivos/a.webp',
          galeria: ['http://localhost:8080/midias/arquivos/b.png'],
        },
      ],
    });
  });

  it('não mexe em URL externa, texto comum nem valores nulos', () => {
    const corpo = { url: 'https://youtu.be/x', descricao: 'ver /midias/arquivos/', logo: null, n: 3 };
    expect(absolutizarMidias(corpo, api)).toEqual(corpo);
  });
});
