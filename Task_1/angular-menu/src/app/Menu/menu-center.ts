import { FirestoreService } from './services/firestore.service';
import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';

@Component({
  imports: [RouterLink, RouterOutlet],
  template: `
    <nav class="mb-3" aria-label="Меню ресторана">
      <a routerLink="/menu">Список блюд</a>
      <a class="ms-3" routerLink="/menu/edit">Редактировать меню</a>
    </nav>
    @if (firestore.error(); as error) {
      <p class="alert alert-danger" role="alert">{{ error }}</p>
    }
    <router-outlet />
  `,
})
export class MenuCenter {
  readonly firestore = inject(FirestoreService);
}
