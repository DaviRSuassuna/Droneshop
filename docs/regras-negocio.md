# Regras de negócio

Índice. O texto das regras fica só no [PRD](PRD-Aeria.md): aqui não se copia nem se edita regra, apenas se aponta para ela.

| Onde | O quê |
|---|---|
| [PRD, seção 6](PRD-Aeria.md#6-regras-de-negócio) | Regras de negócio RN01 a RN18 |
| [PRD, seção 10](PRD-Aeria.md#10-decisões-registradas) | Decisões D1 a D56, que valem como regra |
| [PRD, seção 5](PRD-Aeria.md#5-requisitos-funcionais) | Requisitos funcionais (RF) |
| `docs/issues/NNN.md` | Contrato de API e critérios de aceite de cada issue |
| [API.md](API.md) | Convenções de endpoints, paginação e erros |

## Por domínio

| Domínio | Regras | Decisões |
|---|---|---|
| Catálogo | RN04, RN05, RN17, RN18 | D7, D13 (substituída), D14 a D17, D26, D27, D29, D36, D39, D40, D46 a D48 |
| Estoque | RN01, RN02, RN12, RN14 | D10, D11, D33, D55 |
| Compra e cupom | RN03, RN06, RN07, RN13, RN15, RN16 | D9, D12, D49, D50, D54 |
| Pagamento | RN11 | D2, D50 |
| Pedido, entrega e fiscal | RN07, RN08 | D3, D4, D11, D30 |
| Devolução | RN09 | D8, D11 |
| Notificações | RN10 | D5 |
| Conta, login e privacidade | | D1, D6, D32, D35, D37, D51 a D53, D56 |
| Cadastros do admin | | D18, D21 a D24, D28, D31 |
| Telas e design | | D19, D20, D38, D41, D43 a D45 |
| Testes e infraestrutura | | D25, D34, D42 |

Decisão nova entra no PRD pelo supervisor (`AGENTS.md`, "Trabalho em paralelo") e, depois, ganha linha aqui.
