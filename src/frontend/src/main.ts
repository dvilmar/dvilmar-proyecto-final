// Polyfill para 'global' (necesario para sockjs-client y @stomp/stompjs)
if (typeof (window as any).global === 'undefined') {
  (window as any).global = window;
}

import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { provideRouter } from '@angular/router';
import { importProvidersFrom } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { routes } from './app/app.routes';

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    importProvidersFrom(HttpClientModule)
  ]
}).catch(err => console.error(err));






