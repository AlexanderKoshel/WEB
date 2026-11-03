import { Injectable, signal } from '@angular/core';
import { Menu } from '../menu';
import { menus } from '../mock-menu-list';

@Injectable({ providedIn: 'root' })
export class MenuService {
  protected readonly items = signal(menus);
  readonly dishes = this.items.asReadonly();

  find(id: string) {
    return this.dishes().find(dish => dish.id === id);
  }

  async save(id: string, fields: Omit<Menu, 'id'>) {
    if (id) {
      this.items.update(items => items.map(dish => dish.id === id ? { id, ...fields } : dish));
      return;
    }
    this.items.update(items => [...items, { id: crypto.randomUUID(), ...fields }]);
  }

  async remove(id: string) {
    this.items.update(items => items.filter(dish => dish.id !== id));
  }
}
