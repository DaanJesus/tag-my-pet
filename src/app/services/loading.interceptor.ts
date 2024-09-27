import { Injectable } from '@angular/core';
import { HttpEvent, HttpInterceptor, HttpHandler, HttpRequest } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoadingService } from './loading.service'; // Ajuste o caminho conforme necessário

@Injectable()
export class LoadingInterceptor implements HttpInterceptor {
  constructor(private loadingService: LoadingService) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    this.loadingService.setLoading(true);

    return next.handle(req).pipe(
      tap({
        next: () => { },
        error: () => {
          this.loadingService.setLoading(false);
        },
        complete: () => {
          this.loadingService.setLoading(false);
        },
      })
    );
  }
}
