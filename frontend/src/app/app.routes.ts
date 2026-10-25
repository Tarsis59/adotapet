import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: '/animais' },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login/login.component').then((m) => m.LoginComponent),
    title: 'Entrar — AdotaPet',
  },
  {
    path: 'registrar',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/registrar/registrar.component').then((m) => m.RegistrarComponent),
    title: 'Criar conta — AdotaPet',
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./shared/shell/shell.component').then((m) => m.ShellComponent),
    children: [
      {
        path: 'animais',
        loadComponent: () =>
          import('./features/animais/animal-list/animal-list.component').then((m) => m.AnimalListComponent),
        title: 'Animais — AdotaPet',
      },
      {
        path: 'animais/novo',
        loadComponent: () =>
          import('./features/animais/animal-create/animal-create.component').then(
            (m) => m.AnimalCreateComponent,
          ),
        title: 'Novo animal — AdotaPet',
      },
      {
        path: 'animais/:id',
        loadComponent: () =>
          import('./features/animais/animal-detail/animal-detail.component').then(
            (m) => m.AnimalDetailComponent,
          ),
        title: 'Detalhes do animal — AdotaPet',
      },
      {
        path: 'animais/:id/editar',
        loadComponent: () =>
          import('./features/animais/animal-edit/animal-edit.component').then((m) => m.AnimalEditComponent),
        title: 'Editar animal — AdotaPet',
      },
    ],
  },
  {
    path: '**',
    loadComponent: () => import('./not-found/not-found.component').then((m) => m.NotFoundComponent),
    title: 'Página não encontrada — AdotaPet',
  },
];
