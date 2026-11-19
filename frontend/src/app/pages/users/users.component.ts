import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { RoleName, User, UserRequest, errorMessage } from '../../core/models';

/** Page reservee a l'admin (protegee par roleGuard) : CRUD des utilisateurs et attribution des roles. */
@Component({
  selector: 'app-users',
  imports: [ReactiveFormsModule],
  templateUrl: './users.component.html'
})
export class UsersComponent implements OnInit {
  auth = inject(AuthService);
  private fb = inject(FormBuilder);
  private userService = inject(UserService);

  users: User[] = [];
  chargement = true;
  erreur = '';
  message = '';

  formOuvert = false;
  enEdition: User | null = null;
  erreurForm = '';
  form = this.fb.nonNullable.group({
    prenom: ['', Validators.required],
    nom: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    tel: ['', Validators.pattern(/^[0-9+ ]{8,15}$/)],
    password: ['', Validators.minLength(6)],
    admin: [false],
    collaborateur: [true]
  });

  ngOnInit(): void { this.charger(); }

  charger(): void {
    this.chargement = true;
    this.userService.getAll().subscribe({
      next: u => { this.users = u; this.chargement = false; },
      error: err => { this.erreur = errorMessage(err); this.chargement = false; }
    });
  }

  estMoi(u: User): boolean { return u.id === this.auth.currentUser()?.id; }

  invalide(champ: string): boolean {
    const c = this.form.get(champ);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  ouvrirAjout(): void {
    this.enEdition = null;
    this.form.reset({ prenom: '', nom: '', email: '', tel: '', password: '', admin: false, collaborateur: true });
    this.form.controls.password.setValidators([Validators.required, Validators.minLength(6)]);
    this.form.controls.password.updateValueAndValidity();
    this.erreurForm = '';
    this.formOuvert = true;
  }

  ouvrirEdition(u: User): void {
    this.enEdition = u;
    this.form.reset({ prenom: u.prenom, nom: u.nom, email: u.email, tel: u.tel ?? '', password: '',
                      admin: u.roles.includes('ADMIN'), collaborateur: u.roles.includes('COLLABORATEUR') });
    this.form.controls.password.setValidators([Validators.minLength(6)]);
    this.form.controls.password.updateValueAndValidity();
    this.erreurForm = '';
    this.formOuvert = true;
  }

  enregistrer(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const v = this.form.getRawValue();
    const roles: RoleName[] = [];
    if (v.admin) roles.push('ADMIN');
    if (v.collaborateur) roles.push('COLLABORATEUR');
    if (roles.length === 0) { this.erreurForm = 'Choisissez au moins un rôle.'; return; }

    const data: UserRequest = { prenom: v.prenom, nom: v.nom, email: v.email, tel: v.tel, roles,
                                password: v.password || undefined };
    const req = this.enEdition ? this.userService.update(this.enEdition.id, data) : this.userService.create(data);
    req.subscribe({
      next: () => {
        this.message = this.enEdition ? 'Utilisateur modifié.' : 'Utilisateur créé.';
        this.formOuvert = false;
        this.charger();
      },
      error: err => this.erreurForm = errorMessage(err)
    });
  }

  basculerAdmin(u: User): void {
    const roles: RoleName[] = u.roles.includes('ADMIN')
      ? ['COLLABORATEUR']
      : Array.from(new Set<RoleName>([...u.roles, 'ADMIN']));
    this.userService.updateRoles(u.id, roles).subscribe({
      next: maj => { u.roles = maj.roles; this.message = `Rôles de ${u.prenom} ${u.nom} mis à jour.`; },
      error: err => this.erreur = errorMessage(err)
    });
  }

  supprimer(u: User): void {
    if (!confirm(`Supprimer l'utilisateur ${u.prenom} ${u.nom} ? Ses tâches ne seront plus affectées.`)) return;
    this.userService.delete(u.id).subscribe({
      next: () => { this.message = 'Utilisateur supprimé.'; this.charger(); },
      error: err => this.erreur = errorMessage(err)
    });
  }
}
