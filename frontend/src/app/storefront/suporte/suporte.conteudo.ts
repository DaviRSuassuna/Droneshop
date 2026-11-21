/**
 * Conteúdo fixo da central de ajuda (#90): temas e perguntas frequentes, sem API nem
 * cadastro no admin. Os prazos da configuração da loja (#21) entram pelas funções, para o
 * texto não contradizer a regra (arrependimento e frete reverso).
 */
export interface PrazosLoja {
  prazoArrependimentoDias: number;
  freteReversoPorContaLoja: boolean;
}

export interface PerguntaFrequente {
  pergunta: string;
  resposta: (p: PrazosLoja) => string;
}

export interface TemaSuporte {
  id: string;
  titulo: string;
  resumo: (p: PrazosLoja) => string;
  perguntas: PerguntaFrequente[];
}

const freteReverso = (p: PrazosLoja) =>
  p.freteReversoPorContaLoja
    ? 'O frete de volta é por nossa conta.'
    : 'O frete de volta fica por conta do cliente.';

export const PRAZOS_PADRAO: PrazosLoja = { prazoArrependimentoDias: 7, freteReversoPorContaLoja: true };

export const TEMAS: TemaSuporte[] = [
  {
    id: 'pedidos',
    titulo: 'Pedidos e entrega',
    resumo: () => 'Prazos, rastreio, alteração de endereço',
    perguntas: [
      {
        pergunta: 'Qual o prazo de entrega?',
        resposta: () =>
          'Expresso em 3 dias úteis para capitais e padrão em até 7 dias úteis. O prazo aparece no carrinho ao informar o CEP e no e-mail de confirmação.',
      },
      {
        pergunta: 'Como acompanho o meu pedido?',
        resposta: () =>
          'Em Meus pedidos, na sua conta, cada pedido mostra o status e os eventos de rastreio da transportadora.',
      },
      {
        pergunta: 'Posso mudar o endereço depois da compra?',
        resposta: () =>
          'Enquanto o pedido não sai para separação, sim: abra um chamado informando o número do pedido e o novo endereço.',
      },
    ],
  },
  {
    id: 'trocas',
    titulo: 'Trocas e devoluções',
    resumo: (p) => `${p.prazoArrependimentoDias} dias para desistir da compra`,
    perguntas: [
      {
        pergunta: 'Posso desistir da compra?',
        resposta: (p) =>
          `Sim. Você tem ${p.prazoArrependimentoDias} dias corridos a partir do recebimento para pedir a devolução, sem precisar justificar. ${freteReverso(p)}`,
      },
      {
        pergunta: 'Como peço a troca de um produto com defeito?',
        resposta: (p) =>
          `Pelo item do pedido em Meus pedidos ou por um chamado aqui. Avaliamos o produto e trocamos ou reparamos dentro da garantia. ${freteReverso(p)}`,
      },
    ],
  },
  {
    id: 'garantia',
    titulo: 'Garantia e reparo',
    resumo: () => '12 meses e como abrir um chamado',
    perguntas: [
      {
        pergunta: 'Como funciona a garantia?',
        resposta: () =>
          'Todos os drones têm 12 meses de garantia contra defeito de fabricação. Acessórios seguem a mesma cobertura de 12 meses.',
      },
      {
        pergunta: 'Como abro um chamado de reparo?',
        resposta: () =>
          'Pelo formulário ao lado ou em Meus pedidos, no item comprado. Você recebe uma etiqueta de envio e acompanha o reparo pela conta.',
      },
    ],
  },
  {
    id: 'anac',
    titulo: 'Regras ANAC e SISANT',
    resumo: () => 'Cadastro da aeronave e limites de voo',
    perguntas: [
      {
        pergunta: 'Preciso registrar meu drone na ANAC?',
        resposta: () =>
          'Aeronaves acima de 250 g precisam de cadastro no SISANT. Enviamos o passo a passo junto com a nota fiscal e nossa equipe orienta o preenchimento.',
      },
      {
        pergunta: 'Onde posso voar?',
        resposta: () =>
          'Longe de aeroportos, de pessoas que não participam do voo e abaixo de 120 m de altura, conforme as regras da ANAC e do DECEA.',
      },
    ],
  },
  {
    id: 'baterias',
    titulo: 'Baterias e transporte',
    resumo: () => 'Cuidados, armazenamento e viagem aérea',
    perguntas: [
      {
        pergunta: 'Posso levar as baterias em viagem aérea?',
        resposta: () =>
          'Sim, na bagagem de mão e com terminais protegidos. Companhias costumam limitar a 100 Wh por bateria, e as nossas ficam abaixo desse limite.',
      },
      {
        pergunta: 'Como guardo as baterias?',
        resposta: () =>
          'Com carga entre 40% e 60%, em local seco e fresco. Bateria inchada ou com dano não deve ser usada nem carregada.',
      },
    ],
  },
  {
    id: 'firmware',
    titulo: 'Firmware e apps',
    resumo: () => 'Atualização, calibração e erros comuns',
    perguntas: [
      {
        pergunta: 'Como atualizo o firmware?',
        resposta: () =>
          'Pelo app do fabricante, com o drone e o controle ligados e as baterias acima de 50%. Não desligue durante a atualização.',
      },
      {
        pergunta: 'Quando calibrar a bússola?',
        resposta: () =>
          'Ao voar em um lugar novo, longe do anterior, ou quando o app pedir. Faça a calibração longe de metais e de construções.',
      },
    ],
  },
];

/** Perguntas da lista inicial "Respostas rápidas", como no protótipo. */
export const RAPIDAS: string[] = [
  'Qual o prazo de entrega?',
  'Como funciona a garantia?',
  'Preciso registrar meu drone na ANAC?',
  'Posso levar as baterias em viagem aérea?',
  'Como abro um chamado de reparo?',
];
