import { Component, effect, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { AuthService } from '../core/services/auth.service';
import { FichierService } from '../core/services/fichier.service';

interface NavItem { path: string; label: string; icon: string; color: string; adminOnly?: boolean; }

@Component({
  selector: 'app-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.css'
})
export class LayoutComponent {
  auth = inject(AuthService);
  private router = inject(Router);
  private fichierService = inject(FichierService);
  menuOuvert = false;

  /** Photo de l'utilisateur connecte, chargee en blob (jamais via une URL directe avec le token). */
  readonly photoUrl = signal<string | null>(null);

  constructor() {
    let urlPrecedente: string | null = null;
    effect(() => {
      const u = this.auth.currentUser();
      if (u?.hasPhoto) {
        this.fichierService.getPhotoBlob(u.id).subscribe(blob => {
          if (urlPrecedente) URL.revokeObjectURL(urlPrecedente);
          urlPrecedente = URL.createObjectURL(blob);
          this.photoUrl.set(urlPrecedente);
        });
      } else {
        this.photoUrl.set(null);
      }
    });
  }

  readonly navItems: NavItem[] = [
    { path: '/dashboard', label: 'Tableau de bord', icon: 'bi-tv', color: 'text-primary' },
    { path: '/clients', label: 'Clients', icon: 'bi-building', color: 'text-warning' },
    { path: '/projets', label: 'Projets', icon: 'bi-kanban', color: 'text-info' },
    { path: '/taches', label: 'Tâches', icon: 'bi-list-check', color: 'text-success' },
    { path: '/kanban', label: 'Kanban', icon: 'bi-columns-gap', color: 'text-primary' },
    { path: '/users', label: 'Utilisateurs', icon: 'bi-people', color: 'text-danger', adminOnly: true }
  ];

  /** Titre de la page courante affiche dans la navbar */
  readonly titre = toSignal(
    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd),
      map(() => this.titreDepuisUrl(this.router.url))
    ),
    { initialValue: this.titreDepuisUrl(this.router.url) }
  );

  get visibleItems(): NavItem[] {
    return this.navItems.filter(i => !i.adminOnly || this.auth.isAdmin());
  }

  get initiales(): string {
    const u = this.auth.currentUser();
    return u ? (u.prenom[0] + u.nom[0]).toUpperCase() : '';
  }

  private titreDepuisUrl(url: string): string {
    if (url.startsWith('/profil')) return 'Mon profil';
    return this.navItems.find(i => url.startsWith(i.path))?.label ?? 'TaskFlow';
  }
}
