import { Routes } from '@angular/router';
import { menuRoutes } from './Menu/menu.routes';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'menu' },
  { path: 'menu', children: menuRoutes },
  { path: '**', redirectTo: 'menu' },
];
