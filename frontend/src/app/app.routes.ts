import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { LayoutComponent } from './layout/layout.component';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { ClientsComponent } from './pages/clients/clients.component';
import { ProjetsComponent } from './pages/projets/projets.component';
import { TachesComponent } from './pages/taches/taches.component';
import { UsersComponent } from './pages/users/users.component';

export const routes: Routes = [
  // Visiteur : uniquement connexion et inscription
  { path: 'login', component: LoginComponent, canActivate: [guestGuard] },
  { path: 'register', component: RegisterComponent, canActivate: [guestGuard] },

  // Utilisateur connecte
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'clients', component: ClientsComponent },
      { path: 'projets', component: ProjetsComponent },
      { path: 'taches', component: TachesComponent },
      { path: 'users', component: UsersComponent, canActivate: [roleGuard], data: { roles: ['ADMIN'] } },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: '' }
];
