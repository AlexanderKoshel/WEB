import { Injectable, inject, signal } from '@angular/core';
import { Menu } from '../menu';
import { FirestoreService } from './firestore.service';

@Injectable({ providedIn: 'root' })
export class MenuService {
  private readonly firestore = inject(FirestoreService);
  private readonly items = signal<Menu[]>([]);
  readonly dishes = this.items.asReadonly();

  constructor() {
    void this.firestore.watch(dishes => this.items.set(dishes));
  }

  find(id: string) {
    return this.dishes().find(dish => dish.id === id);
  }

  save(id: string, fields: Omit<Menu, 'id'>) {
    return this.firestore.save(id, fields);
  }

  remove(id: string) {
    return this.firestore.remove(id);
  }
}
