import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MenuService } from './services/menu.service';

@Component({
  imports: [RouterLink],
  template: `
    <h2>Список блюд</h2>
    <ul class="list-group">
      @for (dish of dishes(); track dish.id) {
        <li class="list-group-item"><a [routerLink]="[dish.id]">{{ dish.description }}</a></li>
      } @empty {
        <li class="list-group-item">Блюд пока нет.</li>
      }
    </ul>
  `,
})
export class MenuList {
  readonly dishes = inject(MenuService).dishes;
}
