import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:5000/api/auth'; // URL base da API
  private tokenKey = 'authToken';
  private userKey = 'authUser'
  private userSubject = new BehaviorSubject<any>(null);
  user$ = this.userSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadToken();
  }

  // Registro de usuário
  register(name: string, email: string, password: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, { name, email, password });
  }

  // Login de usuário
  login(email: string, password: string): Observable<any> {
    return this.http.post<{ token: string, user: any }>(`${this.apiUrl}/login`, { email, password })
      .pipe(
        tap(response => {
          this.storeToken(response.token, response.user); // Armazena o token
          this.userSubject.next(response.user); // Atualiza o usuário
        })
      );
  }

  getCurrentUserId(): Observable<string | null> {
    return this.user$.pipe(
      map(user => user ? user._id : null)
    );
  }

  // Armazenar token no localStorage
  private storeToken(token: string, user: any): void {
    localStorage.setItem(this.tokenKey, token);
    localStorage.setItem(this.userKey, JSON.stringify(user));
  }

  // Carregar token do localStorage
  private loadToken(): void {
    const token = localStorage.getItem(this.tokenKey);
    const user = localStorage.getItem('authUser'); // Carrega o usuário do localStorage
    if (token && user) {
      this.userSubject.next(JSON.parse(user)); // Atualiza o BehaviorSubject com o usuário carregado
    }
  }

  // Decodificar JWT
  private parseJwt(token: string): any {
    const payload = token.split('.')[1];
    return JSON.parse(atob(payload));
  }

  // Verificar se o usuário está autenticado
  isAuthenticated(): boolean {
    const token = localStorage.getItem(this.tokenKey);
    if (token) {
      const decodedToken = this.parseJwt(token);
      const expiry = new Date(decodedToken.exp * 1000);
      return expiry > new Date();
    }
    return false;
  }

  // Logout do usuário
  cleanLocalStorage(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    this.userSubject.next(null);
  }
}