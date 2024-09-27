import { Component, OnInit } from '@angular/core';
import { ParticlesConfig } from './particles-config';

declare var particlesJS: any;

@Component({
  selector: 'app-particles',
  templateUrl: './particles.component.html',
  styleUrls: ['./particles.component.css']
})
export class ParticlesComponent implements OnInit {

  constructor() { }

  public ngOnInit(): void {
    particlesJS('particles-js', ParticlesConfig, function () { });
  }

}