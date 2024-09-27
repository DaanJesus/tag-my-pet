import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { RegisterComponent } from './components/register/register.component';
import { ProfileComponent } from './components/profile/profile.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { LoadingInterceptor } from './services/loading.interceptor';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { LandpageComponent } from './components/landpage/landpage.component';
import { HomeComponent } from './components/home/home.component';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatDialogModule } from '@angular/material/dialog';
import { FlexLayoutModule } from '@angular/flex-layout';
import { ParticlesComponent } from './components/particles/particles.component';
import { PetsComponent } from './components/pets/pets.component';
import { ConfigComponent } from './components/config/config.component';
import { FormPetComponent } from './components/pets/form-pet/form-pet.component';
import { MAT_DATE_LOCALE, MatNativeDateModule, MatOptionModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import * as moment from 'moment';
import { InfoPetComponent } from './components/pets/info-pet/info-pet.component';
import { MainlayoutComponent } from './components/mainlayout/mainlayout.component';
import { PgnotfoundComponent } from './components/pgnotfound/pgnotfound.component';
import { ConfirmaDialogComponent } from './components/pets/confirma-dialog/confirma-dialog.component';

@NgModule({
  declarations: [
    AppComponent,
    RegisterComponent,
    ProfileComponent,
    LandpageComponent,
    HomeComponent,
    ParticlesComponent,
    PetsComponent,
    ConfigComponent,
    FormPetComponent,
    InfoPetComponent,
    MainlayoutComponent,
    PgnotfoundComponent,
    ConfirmaDialogComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    FlexLayoutModule,
    MatDividerModule,
    MatListModule,
    MatIconModule,
    MatDialogModule,
    MatTableModule,
    MatOptionModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatProgressBarModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: LoadingInterceptor, multi: true },
    { provide: MAT_DATE_LOCALE, useValue: 'pt-BR' }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
