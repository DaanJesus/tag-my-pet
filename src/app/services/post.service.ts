import { EventEmitter, Injectable, Output } from '@angular/core';
import { catchError, Observable, tap, throwError } from 'rxjs';
import { HttpService } from './http.service';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PostService extends HttpService {

  @Output()
  event: EventEmitter<any> = new EventEmitter<any>();
  private apiUrl = `${environment.apiUrl}/post`

  constructor(private http: HttpClient) {
    super()
  }

  toggleLike(postId: string, user: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${postId}/like`, { user }, { ...this.getHttpCustomOption(), observe: 'body' })
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

  registerPost(post: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, post, { ...this.getHttpCustomOption(), observe: 'body' })
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

  getPosts(page: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/get?page=${page}&limit=10`, { ...this.getHttpCustomOption(), observe: 'body' })
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
}
