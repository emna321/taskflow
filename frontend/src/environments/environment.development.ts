// Developpement : Angular sur http://localhost:4200, Spring Boot sur http://localhost:8089 (CORS)
// Port 8080 est occupe par le listener HTTP d'Oracle XE (TNSLSNR) sur cette machine.
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8089/api'
};
