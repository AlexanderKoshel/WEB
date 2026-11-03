import { Routes } from '@angular/router';
import { MenuCenter } from './menu-center';
import { MenuDetails } from './menu-details';
import { MenuList } from './menu-list';

export const menuRoutes: Routes = [{
  path: '',
  component: MenuCenter,
  children: [
    { path: '', component: MenuList },
    { path: ':id', component: MenuDetails },
  ],
}];
