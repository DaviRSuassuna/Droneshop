import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly dark = signal(true);

  initialize(): void {
    const stored = localStorage.getItem('aeria-theme');
    this.dark.set(stored !== 'light');
    document.documentElement.dataset['theme'] = this.dark() ? 'dark' : 'light';
  }

  toggle(): void {
    this.dark.update((value) => !value);
    localStorage.setItem('aeria-theme', this.dark() ? 'dark' : 'light');
    document.documentElement.dataset['theme'] = this.dark() ? 'dark' : 'light';
  }
}