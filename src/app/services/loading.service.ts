import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class LoadingService {
  // Estado para o loading completo
  private fullLoadingSubject = new BehaviorSubject<boolean>(false);
  fullLoading$ = this.fullLoadingSubject.asObservable();

  // Estado para o spinner simples
  private spinnerLoadingSubject = new BehaviorSubject<boolean>(false);
  spinnerLoading$ = this.spinnerLoadingSubject.asObservable();

  // Controla o loading completo
  setFullLoading(isLoading: boolean) {
    this.fullLoadingSubject.next(isLoading);
  }

  // Controla o spinner simples
  setPawLoading(isLoading: boolean) {
    this.spinnerLoadingSubject.next(isLoading);
  }
}
