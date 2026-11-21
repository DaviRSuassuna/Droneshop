import { Component } from '@angular/core';

// Capa da loja: provisória, só a área do conteúdo dentro da casca. O conteúdo vem na #21.
@Component({
  selector: 'app-home',
  template: `<main><h1 class="sr-only">Aeria Drones</h1></main>`,
  styles: `
    main {
      min-height: 60vh;
    }
    .sr-only {
      position: absolute;
      width: 1px;
      height: 1px;
      overflow: hidden;
      clip-path: inset(50%);
      white-space: nowrap;
    }
  `,
})
export class Home {}
