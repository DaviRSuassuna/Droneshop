<<<<<<< HEAD
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  imports: [RouterOutlet],
=======
import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { StorefrontHeaderComponent } from './shared/storefront-header.component';
import { ThemeService } from './core/theme.service';

@Component({
  imports: [RouterOutlet, StorefrontHeaderComponent],
>>>>>>> ed3d54b (Criação do frontend(telas) com o Angular)
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
<<<<<<< HEAD
export class App {}
=======
export class App {
  private readonly theme = inject(ThemeService);

  constructor() {
    this.theme.initialize();
  }
}
>>>>>>> ed3d54b (Criação do frontend(telas) com o Angular)
