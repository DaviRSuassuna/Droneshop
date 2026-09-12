import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { StorefrontHeaderComponent } from './shared/storefront-header.component';
import { ThemeService } from './core/theme.service';

@Component({
  imports: [RouterOutlet, StorefrontHeaderComponent],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  private readonly theme = inject(ThemeService);

  constructor() {
    this.theme.initialize();
  }
}
