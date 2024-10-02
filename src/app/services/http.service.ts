import { HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class HttpService {

  constructor() { }

  public getHttOptions(): any {

    let _auth: any = localStorage.getItem("authToken");

    let _headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      "Authorization": 'Bearer ' + _auth,
    });

    let _httOptions: any = {};
    _httOptions.headers = _headers;
    _httOptions.withCredentials = false;

    return _httOptions;

  }

  public getHttpCustomOption(): any {
    let _auth: any = localStorage.getItem("authToken");

    let _headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      "Authorization": 'Bearer ' + _auth,
      'loading-type': 'paw'
    });

    let _httOptions: any = {};
    _httOptions.headers = _headers;
    _httOptions.withCredentials = false;

    return _httOptions;
  }
}
