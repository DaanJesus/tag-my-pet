import { EventEmitter, Injectable, Output } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, Observable, tap, throwError } from 'rxjs';
import { HttpService } from './http.service';

interface Pet {
  _id: string,
  type: string,
  breed: string,
  birthDate: Date,
  furColor: string,
  weight: number,
  name: string,
  photo: string,
  sex: string,
  medicalInfo: string,
  castrated: string,
  qrCode: string,
}

@Injectable({
  providedIn: 'root'
})
export class PetService extends HttpService {
  @Output()
  event: EventEmitter<any> = new EventEmitter<any>();
  private apiUrl = 'http://localhost:5000/api/pets'; // URL da API

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
  getPet(id: string): Observable<Pet> {
    return this.http.get<Pet>(`${this.apiUrl}/${id}`, { ...this.getHttOptions(), observe: 'body' })
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

  getMyPets(userId: string): Observable<Pet> {
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
