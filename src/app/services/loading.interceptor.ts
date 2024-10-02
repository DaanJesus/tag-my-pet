import { Injectable } from '@angular/core';
import { HttpEvent, HttpInterceptor, HttpHandler, HttpRequest } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoadingService } from './loading.service';

@Injectable()
export class LoadingInterceptor implements HttpInterceptor {
  constructor(private loadingService: LoadingService) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Verifica se a requisição tem um cabeçalho específico para o tipo de loading
    const loadingType = req.headers.get('loading-type');

    if (loadingType === 'paw') {
      this.loadingService.setPawLoading(true);
    } else {
      this.loadingService.setFullLoading(true); // Por padrão, usa o loading completo
    }

    return next.handle(req).pipe(
      tap({
        next: () => { },
        error: () => {
          this.loadingService.setPawLoading(false);
          this.loadingService.setFullLoading(false);
        },
        complete: () => {
          this.loadingService.setPawLoading(false);
          this.loadingService.setFullLoading(false);
        },
      })
    );
  }
}