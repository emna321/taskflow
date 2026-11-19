import { Component, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration } from 'chart.js';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { ClientService } from '../../core/services/client.service';
import { ProjetService } from '../../core/services/projet.service';
import { TacheService } from '../../core/services/tache.service';
import { StatsService } from '../../core/services/stats.service';
import { PRIORITE_LABELS, STATUT_LABELS, Stats, Tache, errorMessage } from '../../core/models';

const COULEURS = ['#5e72e4', '#11cdef', '#2dce89', '#fb6340', '#f5365c', '#825ee4', '#8898aa'];

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, DatePipe, BaseChartDirective],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  auth = inject(AuthService);
  private clientService = inject(ClientService);
  private projetService = inject(ProjetService);
  private tacheService = inject(TacheService);
  private statsService = inject(StatsService);

  statutLabels = STATUT_LABELS;
  prioriteLabels = PRIORITE_LABELS;
  chargement = true;
  erreur = '';
  nbClients = 0;
  nbProjets = 0;
  taches: Tache[] = [];
  stats: Stats | null = null;

  // ----- Graphiques (Chart.js via ng2-charts) -----
  statutChartData: ChartConfiguration<'doughnut'>['data'] = { labels: [], datasets: [{ data: [] }] };
  statutChartOptions: ChartConfiguration<'doughnut'>['options'] = { plugins: { legend: { position: 'bottom' } } };

  prioriteChartData: ChartConfiguration<'bar'>['data'] = { labels: [], datasets: [{ data: [], label: 'Tâches' }] };
  prioriteChartOptions: ChartConfiguration<'bar'>['options'] = { plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true, ticks: { precision: 0 } } } };

  projetChartData: ChartConfiguration<'bar'>['data'] = { labels: [], datasets: [{ data: [], label: 'Tâches' }] };
  projetChartOptions: ChartConfiguration<'bar'>['options'] = {
    indexAxis: 'y', plugins: { legend: { display: false } }, scales: { x: { beginAtZero: true, ticks: { precision: 0 } } }
  };

  chargeChartData: ChartConfiguration<'bar'>['data'] = { labels: [], datasets: [{ data: [], label: 'Charge (tâches non terminées)' }] };
  chargeChartOptions: ChartConfiguration<'bar'>['options'] = { plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true, ticks: { precision: 0 } } } };

  get mesTaches(): Tache[] {
    const id = this.auth.currentUser()?.id;
    return this.taches.filter(t => t.userId === id && t.statut !== 'TERMINEE');
  }
  compte(statut: string): number { return this.taches.filter(t => t.statut === statut).length; }

  ngOnInit(): void {
    forkJoin({
      clients: this.clientService.getAll(),
      projets: this.projetService.getAll(),
      taches: this.tacheService.getAll(),
      stats: this.statsService.obtenir()
    }).subscribe({
      next: r => {
        this.nbClients = r.clients.length;
        this.nbProjets = r.projets.length;
        this.taches = r.taches;
        this.stats = r.stats;
        this.construireGraphiques(r.stats);
        this.chargement = false;
      },
      error: err => { this.erreur = errorMessage(err); this.chargement = false; }
    });
  }

  private construireGraphiques(s: Stats): void {
    this.statutChartData = {
      labels: Object.keys(s.tachesParStatut).map(k => this.statutLabels[k as keyof typeof this.statutLabels]),
      datasets: [{ data: Object.values(s.tachesParStatut), backgroundColor: COULEURS }]
    };
    this.prioriteChartData = {
      labels: Object.keys(s.tachesParPriorite).map(k => this.prioriteLabels[k as keyof typeof this.prioriteLabels]),
      datasets: [{ data: Object.values(s.tachesParPriorite), label: 'Tâches', backgroundColor: COULEURS }]
    };
    this.projetChartData = {
      labels: s.tachesParProjet.map(p => p.projetLibelle),
      datasets: [{ data: s.tachesParProjet.map(p => p.nbTaches), label: 'Tâches', backgroundColor: COULEURS[0] }]
    };
    this.chargeChartData = {
      labels: s.chargeParCollaborateur.map(c => c.userNom),
      datasets: [{ data: s.chargeParCollaborateur.map(c => c.nbTaches), label: 'Charge (tâches non terminées)', backgroundColor: COULEURS[2] }]
    };
  }
}
