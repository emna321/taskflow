import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { errorMessage } from '../../core/models';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  erreur = '';
  chargement = false;

  form = this.fb.nonNullable.group({
    prenom: ['', Validators.required],
    nom: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    tel: ['', Validators.pattern(/^[0-9+ ]{8,15}$/)],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  invalide(champ: string): boolean {
    const c = this.form.get(champ);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.chargement = true;
    this.erreur = '';
    this.auth.register(this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/login'], { state: { registered: true } }),
      error: err => { this.erreur = errorMessage(err); this.chargement = false; }
    });
  }
}
