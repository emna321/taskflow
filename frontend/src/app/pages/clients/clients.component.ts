import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { ClientService } from '../../core/services/client.service';
import { ProjetService } from '../../core/services/projet.service';
import { Client, Projet, errorMessage } from '../../core/models';

@Component({
  selector: 'app-clients',
  imports: [ReactiveFormsModule],
  templateUrl: './clients.component.html'
})
export class ClientsComponent implements OnInit {
  auth = inject(AuthService);
  private fb = inject(FormBuilder);
  private clientService = inject(ClientService);
  private projetService = inject(ProjetService);

  clients: Client[] = [];
  recherche = '';
  chargement = true;
  erreur = '';
  message = '';

  // Formulaire ajout / modification
  formOuvert = false;
  enEdition: Client | null = null;
  erreurForm = '';
  form = this.fb.nonNullable.group({
    nom: ['', Validators.required],
    email: ['', Validators.email],
    tel: ['', Validators.pattern(/^[0-9+ ]{8,15}$/)],
    adresse: ['']
  });

  // Consultation
  detail: Client | null = null;
  projetsDuClient: Projet[] = [];

  get clientsFiltres(): Client[] {
    const q = this.recherche.toLowerCase().trim();
    return q ? this.clients.filter(c => [c.nom, c.email, c.tel, c.adresse].some(v => v?.toLowerCase().includes(q)))
             : this.clients;
  }

  ngOnInit(): void { this.charger(); }

  charger(): void {
    this.chargement = true;
    this.clientService.getAll().subscribe({
      next: c => { this.clients = c; this.chargement = false; },
      error: err => { this.erreur = errorMessage(err); this.chargement = false; }
    });
  }

  invalide(champ: string): boolean {
    const c = this.form.get(champ);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  ouvrirAjout(): void {
    this.enEdition = null;
    this.form.reset();
    this.erreurForm = '';
    this.formOuvert = true;
  }

  ouvrirEdition(c: Client): void {
    this.enEdition = c;
    this.form.reset({ nom: c.nom, email: c.email ?? '', tel: c.tel ?? '', adresse: c.adresse ?? '' });
    this.erreurForm = '';
    this.formOuvert = true;
  }

  enregistrer(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const data = this.form.getRawValue();
    const req = this.enEdition?.id
      ? this.clientService.update(this.enEdition.id, data)
      : this.clientService.create(data);
    req.subscribe({
      next: () => {
        this.message = this.enEdition ? 'Client modifié.' : 'Client ajouté.';
        this.formOuvert = false;
        this.charger();
      },
      error: err => this.erreurForm = errorMessage(err)
    });
  }

  supprimer(c: Client): void {
    if (!c.id || !confirm(`Supprimer le client « ${c.nom} » et tous ses projets ?`)) return;
    this.clientService.delete(c.id).subscribe({
      next: () => { this.message = 'Client supprimé.'; this.charger(); },
      error: err => this.erreur = errorMessage(err)
    });
  }

  consulter(c: Client): void {
    this.detail = c;
    this.projetsDuClient = [];
    if (c.id) this.projetService.getAll(c.id).subscribe(p => this.projetsDuClient = p);
  }
}
