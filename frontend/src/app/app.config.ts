import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
<<<<<<< HEAD
  providers: [provideBrowserGlobalErrorListeners(), provideRouter(routes), provideHttpClient()],
=======
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(),
    provideRouter(routes)
  ]
>>>>>>> ed3d54b (Criação do frontend(telas) com o Angular)
};
