import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Products } from './products/products';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'products', component: Products, canActivate: [authGuard] },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];
