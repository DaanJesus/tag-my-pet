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

  constructor(
    private loadingService: LoadingService,
    private fb: FormBuilder,
    private _snackBar: MatSnackBar,
    private authService: AuthService,
    private router: Router
  ) {
    this.authForm = this.fb.group({
      name: [''], // Apenas usado para registro
      email: ['daan@gmail.com', [Validators.required, Validators.email]],
      password: ['123', Validators.required]
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

  onSubmit(): void {
    if (this.authForm.valid) {
      const { name, email, password } = this.authForm.value;

      if (this.isLoginMode) {
        this.authService.login(email, password).subscribe({
          next: (res) => {
            this._snackBar.open('Login bem-sucedido', "X", {
              duration: 2000,
            });
            this.router.navigate(['/home']);
          },
          error: (err) => {
            this._snackBar.open(err.error.message || 'Erro ao fazer login', "X", {
              duration: 2000,
            });
            console.error('Erro de login', err);
          }
        });
      } else {
        this.authService.register(name, email, password).subscribe({
          next: (res) => {
            this._snackBar.open(res.message || 'Registro bem-sucedido', "X", {
              duration: 2000,
            });
            this.switchMode();
            this.router.navigate(['/auth']); // Redireciona de volta para login após registro
          },
          error: (err) => {
            this._snackBar.open(err.error.message || 'Erro ao registrar', "X", {
              duration: 2000,
            });
            console.error('Erro de registro', err);
          }
        });
      }
    }
  }
}