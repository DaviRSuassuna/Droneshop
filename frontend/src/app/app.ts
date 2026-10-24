import { Component, DOCUMENT, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';

@Component({
  imports: [RouterOutlet],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  constructor() {
    // A loja usa as superfícies do site.css e o painel as do admin.css (mesmos nomes de token)
    const raiz = inject(DOCUMENT).documentElement;
    inject(Router)
      .events.pipe(
        filter((e): e is NavigationEnd => e instanceof NavigationEnd),
        takeUntilDestroyed(),
      )
      .subscribe((e) => raiz.classList.toggle('loja', !e.urlAfterRedirects.startsWith('/admin')));
  }
}
