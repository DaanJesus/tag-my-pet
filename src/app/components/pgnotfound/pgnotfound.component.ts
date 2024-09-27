import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-pgnotfound',
  templateUrl: './pgnotfound.component.html',
  styleUrls: ['./pgnotfound.component.css']
})
export class PgnotfoundComponent {

  constructor(
    private router: Router
  ){}

  goToIndex(){
    this.router.navigate(['/index'])
  }

}
