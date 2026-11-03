import { DecimalPipe } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { MenuService } from './services/menu.service';

@Component({
  imports: [DecimalPipe],
  template: `
    @if (dish(); as dish) {
      <h2>{{ dish.description }}</h2>
      <p>Цена: {{ dish.price | number:'1.2-2' }} BYN</p>
    } @else {
      <p>Блюдо не найдено.</p>
    }
  `,
})
export class MenuDetails {
  private readonly menu = inject(MenuService);
  private readonly params = toSignal(inject(ActivatedRoute).paramMap, { requireSync: true });
  readonly dish = computed(() => this.menu.find(this.params().get('id') ?? ''));
}
