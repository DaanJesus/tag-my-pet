import { AfterViewInit, Component, ElementRef, HostListener, OnInit, QueryList, Renderer2, ViewChild, ViewChildren } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from 'src/app/services/auth.service';
import { Router } from '@angular/router';
import { LoadingService } from 'src/app/services/loading.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements AfterViewInit {

  @ViewChild('signInBtn', { static: true }) signInBtn!: ElementRef;
  @ViewChild('signUpBtn', { static: true }) signUpBtn!: ElementRef;
  @ViewChild('container', { static: true }) container!: ElementRef;

  authForm: FormGroup;
  isLoginMode = true;

  tagExists: boolean = false;
  tagTouched: boolean = false;

  emailValido: boolean = false;
  emailTouched: boolean = false;

  profileImage: string = '';  // Usado para armazenar a URL da imagem de perfil
  defaultColor: string = '#88B04B';  // Cor de fundo padrão para a imagem de perfil

  constructor(
    private loadingService: LoadingService,
    private fb: FormBuilder,
    private _snackBar: MatSnackBar,
    private authService: AuthService,
    private router: Router
  ) {
    this.authForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
      tag: ['', Validators.required],
    });
  }

  switchMode(): void {
    this.isLoginMode = !this.isLoginMode;
    this.authForm.reset();
  }

  ngAfterViewInit() {
    this.signUpBtn.nativeElement.addEventListener('click', () => {
      this.container.nativeElement.classList.add('sign-up-mode');
    });

    this.signInBtn.nativeElement.addEventListener('click', () => {
      this.container.nativeElement.classList.remove('sign-up-mode');
    });
  }

  // Função para gerar a imagem de perfil
  generateProfilePicture(): void {
    const name = this.authForm.get('name')?.value || '';
    const initials = this.getInitials(name);

    // Cria o Canvas para desenhar a imagem
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    const size = 150;

    if (ctx) {
      canvas.width = size;
      canvas.height = size;

      // Desenha o fundo colorido (círculo)
      ctx.fillStyle = this.defaultColor;
      ctx.fillRect(0, 0, size, size);

      // Desenha as iniciais do nome
      ctx.fillStyle = '#ffffff';  // Cor das iniciais
      ctx.font = 'bold 60px Arial';
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      ctx.fillText(initials, size / 2, size / 2);

      // Converte o canvas para uma imagem base64
      this.profileImage = canvas.toDataURL('image/png');
    }
  }

  // Função para capturar as iniciais do nome
  getInitials(name: string): string {
    const words = name.trim().split(' ');
    if (words.length === 1) {
      return words[0].charAt(0).toUpperCase();
    }
    return words[0].charAt(0).toUpperCase() + words[words.length - 1].charAt(0).toUpperCase();
  }

  onSubmit(): void {

    const { name, email, password, tag } = this.authForm.value;

    if (this.isLoginMode) {
      this.authService.login(email, password).subscribe({
        next: (res) => {
          this._snackBar.open(res.message, "X", {
            duration: 2000,
          });
          this.router.navigate(['/home']);
        },
        error: (err) => {
          this._snackBar.open(err.error.message, "X", {
            duration: 2000,
          });
        }
      });
    } else if (!this.isLoginMode && this.authForm.valid && this.tagExists == true && this.emailValido == true) {
      this.generateProfilePicture();
      
      this.authService.register(name, email, password, tag, this.profileImage).subscribe({
        next: (res) => {
          this._snackBar.open(res.message, "X", {
            duration: 2000,
          });
          /* this.switchMode(); */
          this.router.navigate(['/auth']); // Redireciona de volta para login após registro
        },
        error: (err) => {
          this._snackBar.open(err.error.message, "X", {
            duration: 2000,
          });
        }
      });
    }
  }

  checkTagExistence(event: Event) {
    const input = event.target as HTMLInputElement;
    const tag = input.value;

    input.value = tag.replace(/[^a-zA-Z0-9_.]/g, '');

    if (input.value.length == 0) {
      this.tagTouched = true
      this.tagExists = false
    } else {
      this.tagExists = this.checkTagInDatabase(tag);
    }
  }

  checkTagInDatabase(tag: string): boolean {
    const existingTags = ['ana_oliveira', 'john_doe'];
    return existingTags.includes(tag) ? false : true
  }

  verificarEmail(event: Event) {
    const input = event.target as HTMLInputElement;
    const email = input.value;

    this.emailValido = this.validarEmail(email);
  }

  validarEmail(email: string): boolean {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
  }
}