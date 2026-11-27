import {
  ApplicationConfig,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { catchError, of } from 'rxjs';
import { AuthService } from './core/services/auth.service';
import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { midiaUrlInterceptor } from './core/interceptors/midia-url.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideAppInitializer(() =>
      inject(AuthService)
        .renovar()
        .pipe(catchError(() => of(null))),
    ),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor, midiaUrlInterceptor])),
  ],
};
