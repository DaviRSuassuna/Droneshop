import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  email = '';
  senha = '';
  manterConectado = false;
  readonly carregando = signal(false);
  readonly erro = signal<string | null>(null);

  constructor() {
    const tema = localStorage.getItem('aeria-theme') === 'dark' ? 'dark' : 'light';
    document.documentElement.setAttribute('data-theme', tema);
  }

  entrar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.auth.login(this.email.trim(), this.senha, this.manterConectado).subscribe({
      next: () => {
        this.carregando.set(false);
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
        const destino = returnUrl?.startsWith('/') && !returnUrl.startsWith('//')
          ? returnUrl
          : '/admin/marcas';
        void this.router.navigateByUrl(destino);
      },
      error: (resposta: { status?: number; error?: { detail?: string } }) => {
        this.carregando.set(false);
        this.erro.set(
          resposta.status === 401
            ? 'E-mail ou senha inválidos.'
            : (resposta.error?.detail ?? 'Não foi possível entrar. Tente novamente.'),
        );
      },
    });
  }
}
