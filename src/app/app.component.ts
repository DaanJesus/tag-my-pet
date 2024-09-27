import { AfterViewInit, Component, ElementRef, OnInit, Renderer2, ViewChild } from '@angular/core';
import { LoadingService } from './services/loading.service';
import { TimelineMax, Power2, Power4 } from 'gsap';
import * as $ from 'jquery';
import { filter, timestamp } from 'rxjs';
import { NavigationEnd, Route, Router } from '@angular/router';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {

  constructor(
    private loadingService: LoadingService
  ) { }

  isLoading$ = this.loadingService.isLoading$;

}