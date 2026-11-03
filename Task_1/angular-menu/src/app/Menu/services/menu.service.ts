import { Injectable, signal } from '@angular/core';
import { menus } from '../mock-menu-list';

@Injectable({ providedIn: 'root' })
export class MenuService {
  protected readonly items = signal(menus);
  readonly dishes = this.items.asReadonly();

  find(id: string) {
    return this.dishes().find(dish => dish.id === id);
  }
}
