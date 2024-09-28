import { EventEmitter, Injectable, Output } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, Observable, tap, throwError } from 'rxjs';
import { HttpService } from './http.service';
import { environment } from 'src/environments/environment';
import { Pet } from '../models/Pet';

@Injectable({
  providedIn: 'root'
})
export class PetService extends HttpService {
  @Output()
  event: EventEmitter<any> = new EventEmitter<any>();
  private apiUrl = `${environment.apiUrl}/pets`

  constructor(private http: HttpClient) {
    super()
  }

  // Método para criar um novo pet
  createPet(pet: Pet): Observable<Pet> {
    return this.http.post<Pet>(this.apiUrl + '/', pet, { ...this.getHttOptions(), observe: 'body' })
      .pipe(
        tap((data: any) => {
          return data;
        }),
        catchError(error => {
          this.event.emit({ "erro": error });
          return throwError(() => new Error(error));
        })
      );
  }

  // Método para obter um pet pelo ID
  getPetById(id: string): Observable<Pet> {
    return this.http.get<Pet>(`${this.apiUrl}/pet-info/${id}`)
      .pipe(
        tap((data: any) => {
          return data;
        }),
        catchError(error => {
          this.event.emit({ "erro": error });
          return throwError(() => new Error(error))
        })
      )
  }

  getMyPets(userId: string): Observable<Pet[]> {
    return this.http.get<Pet>(`${this.apiUrl}/my-pets/${userId}`, { ...this.getHttOptions(), observe: 'body' })
      .pipe(
        tap((data: any) => {
          return data;
        }),
        catchError(error => {
          this.event.emit({ "erro": error });
          return throwError(() => new Error(error))
        })
      )
  }

  getBreedsByType(petType: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/breeds/${petType}`, this.getHttOptions())
      .pipe(
        tap((data: any) => {
          return data;
        }),
        catchError(error => {
          this.event.emit({ "erro": error });
          return throwError(() => new Error(error))
        })
      )
  }
}
