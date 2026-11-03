import { FirestoreService } from './services/firestore.service';
import { Component, inject } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { MenuService } from './services/menu.service';

@Component({
  imports: [FormsModule],
  template: `
    <h2>Редактирование меню</h2>
    @if (firestore.authenticated()) {
    <button class="btn btn-outline-secondary mb-3" type="button" (click)="logout()">Выйти</button>
    <label class="form-label" for="dish">Блюдо</label>
    <select id="dish" class="form-select mb-3" [ngModel]="id"
      (ngModelChange)="selectDish($event)" [disabled]="busy">
      <option value="">Новое блюдо</option>
      @for (dish of menu.dishes(); track dish.id) {
        <option [value]="dish.id">{{ dish.description }}</option>
      }
    </select>

    <form #form="ngForm" (ngSubmit)="save(form)">
      <fieldset [disabled]="busy">
        <label class="form-label" for="description">Описание</label>
        <input id="description" class="form-control mb-3" name="description"
          [(ngModel)]="description" required maxlength="200">

        <label class="form-label" for="price">Цена, BYN</label>
        <input id="price" class="form-control mb-3" name="price" type="number"
          [(ngModel)]="price" required min="0" max="1000000" step="0.01">

        <button class="btn btn-primary" type="submit" [disabled]="!form.valid || !description.trim()">
          {{ id ? 'Сохранить' : 'Добавить' }}
        </button>
        @if (id) {
          <button class="btn btn-outline-danger ms-2" type="button" (click)="remove(form)">
            Удалить
          </button>
        }
      </fieldset>
    </form>
    } @else {
      <form #login="ngForm" (ngSubmit)="signIn()" class="mb-3">
        <label for="email" class="form-label">Email редактора</label>
        <input id="email" class="form-control mb-3" name="email" type="email"
          [(ngModel)]="email" required email autocomplete="username">
        <label for="password" class="form-label">Пароль</label>
        <input id="password" class="form-control mb-3" name="password" type="password"
          [(ngModel)]="password" required autocomplete="current-password">
        <button class="btn btn-primary" type="submit" [disabled]="!login.valid || busy">Войти</button>
      </form>
    }
    <p class="mt-3" aria-live="polite">{{ status }}</p>
  `,
})
export class MenuEditor {
  readonly firestore = inject(FirestoreService);
  email = '';
  password = '';
  readonly menu = inject(MenuService);
  id = '';
  description = '';
  price: number | null = null;
  busy = false;
  status = '';

  async signIn() {
    if (this.busy) return;
    this.busy = true;
    try {
      await this.firestore.signIn(this.email.trim(), this.password);
      this.status = '';
    } catch {
      this.status = 'Не удалось войти. Проверьте email и пароль.';
    } finally {
      this.password = '';
      this.busy = false;
    }
  }

  async logout() {
    try {
      await this.firestore.signOut();
      this.selectDish('');
    } catch {
      this.status = 'Не удалось выйти.';
    }
  }

  selectDish(id: string) {
    const dish = this.menu.find(id);
    this.id = dish?.id ?? '';
    this.description = dish?.description ?? '';
    this.price = dish?.price ?? null;
    this.status = '';
  }

  async save(form: NgForm) {
    const description = this.description.trim();
    if (this.busy || !form.valid || !description || this.price === null || !Number.isFinite(this.price)) {
      return;
    }
    this.busy = true;
    try {
      const editing = Boolean(this.id);
      await this.menu.save(this.id, { description, price: this.price });
      if (!editing) {
        this.selectDish('');
        form.resetForm({ description: '', price: null });
      }
      this.status = editing ? 'Изменения сохранены.' : 'Блюдо добавлено.';
    } catch {
      this.status = 'Не удалось сохранить блюдо.';
    } finally {
      this.busy = false;
    }
  }

  async remove(form: NgForm) {
    if (this.busy || !this.id) return;
    this.busy = true;
    try {
      await this.menu.remove(this.id);
      this.selectDish('');
      form.resetForm({ description: '', price: null });
      this.status = 'Блюдо удалено.';
    } catch {
      this.status = 'Не удалось удалить блюдо.';
    } finally {
      this.busy = false;
    }
  }
}
