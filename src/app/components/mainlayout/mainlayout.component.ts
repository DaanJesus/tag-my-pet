import { AfterViewInit, Component, ElementRef, OnInit, Renderer2, ViewChild } from '@angular/core';
import { TimelineMax, Power2, Power4 } from 'gsap';
import * as $ from 'jquery';
import { filter, timestamp } from 'rxjs';
import { NavigationEnd, Route, Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-mainlayout',
  templateUrl: './mainlayout.component.html',
  styleUrls: ['./mainlayout.component.css']
})
export class MainlayoutComponent implements OnInit, AfterViewInit {
  title: string = '';
  sub_title: string = '';

  user: any;

  constructor(
    private renderer: Renderer2,
    private router: Router,
    private authService: AuthService
  ) { }

  @ViewChild('menuBtn') menuBtn!: ElementRef;
  @ViewChild('closeBtn') closeBtn!: ElementRef;
  @ViewChild('main') main!: ElementRef;
  @ViewChild('body') body!: ElementRef;

  menu: any = [
    { url: '/home', title: 'Início', sub_title: "" },
    { url: '/profile', title: 'Meu Perfil', },
    { url: '/pets', title: 'Meus Pets', sub_title: "Cadastrar ou alterar um pet" },
    { url: '/config', title: 'Configurações' }
  ];

  posts: any = [];
  pet: any = [];

  ngOnInit(): void {

    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))  // Filtro para capturar o final da navegação
      .subscribe(() => {
        this.upgradePageTitle();
      });

    this.authService.user$.subscribe(user => {
      this.user = user
    })
  }

  upgradePageTitle() {

    const currentPage = this.router.url
    const activeMenuItem = this.menu.find((item: { url: string; }) => item.url === currentPage);
    this.title = activeMenuItem ? activeMenuItem.title : 'Início'
    /* this.sub_title = activeMenuItem.sub_title */

  }

  logout() {
    this.authService.cleanLocalStorage();
    this.router.navigate(['/auth'])
  }

  ngAfterViewInit() {
    const bodyElement = this.body.nativeElement;
    const menuBtnElement = this.menuBtn.nativeElement;
    const closeBtnElement = this.closeBtn.nativeElement;
    const mainElement = this.main.nativeElement;

    this.renderer.listen(menuBtnElement, 'click', () => {
      this.renderer.addClass(mainElement, 'menu-open__main');
      this.renderer.addClass(mainElement, 'overflow-hidden');
      this.renderer.addClass(bodyElement, 'overflow-hidden');
    });

    this.renderer.listen(closeBtnElement, 'click', () => {
      this.renderer.removeClass(mainElement, 'menu-open__main');
      this.renderer.removeClass(mainElement, 'overflow-hidden');
      this.renderer.removeClass(bodyElement, 'overflow-hidden');
    });

  }

}
