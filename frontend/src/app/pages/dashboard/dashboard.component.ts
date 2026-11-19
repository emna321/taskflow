import { Component, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { ClientService } from '../../core/services/client.service';
import { ProjetService } from '../../core/services/projet.service';
import { TacheService } from '../../core/services/tache.service';
import { PRIORITE_LABELS, STATUT_LABELS, Tache, errorMessage } from '../../core/models';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, DatePipe],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  auth = inject(AuthService);
  private clientService = inject(ClientService);
  private projetService = inject(ProjetService);
  private tacheService = inject(TacheService);

  statutLabels = STATUT_LABELS;
  prioriteLabels = PRIORITE_LABELS;
  chargement = true;
  erreur = '';
  nbClients = 0;
  nbProjets = 0;
  taches: Tache[] = [];

  get mesTaches(): Tache[] {
    const id = this.auth.currentUser()?.id;
    return this.taches.filter(t => t.userId === id && t.statut !== 'TERMINEE');
  }
  compte(statut: string): number { return this.taches.filter(t => t.statut === statut).length; }

  ngOnInit(): void {
    forkJoin({
      clients: this.clientService.getAll(),
      projets: this.projetService.getAll(),
      taches: this.tacheService.getAll()
    }).subscribe({
      next: r => {
        this.nbClients = r.clients.length;
        this.nbProjets = r.projets.length;
        this.taches = r.taches;
        this.chargement = false;
      },
      error: err => { this.erreur = errorMessage(err); this.chargement = false; }
    });
  }
}
