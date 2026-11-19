import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Ajoute automatiquement "Authorization: Bearer <token>" a chaque requete vers l'API.
 * Si l'API repond 401 (token expire / invalide), l'utilisateur est deconnecte.
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token;
  const isAuthRoute = req.url.includes('/auth/login') || req.url.includes('/auth/register');

  const request = token && !isAuthRoute
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(request).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !isAuthRoute) {
        auth.logout();
      }
      return throwError(() => err);
    })
  );
};
