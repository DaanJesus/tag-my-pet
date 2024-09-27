import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ProfileComponent } from './components/profile/profile.component';
import { RegisterComponent } from './components/register/register.component';
import { AuthGuard, AuthRedirectGuard } from './services/auth.guard';
import { LandpageComponent } from './components/landpage/landpage.component';
import { HomeComponent } from './components/home/home.component';
import { PetsComponent } from './components/pets/pets.component';
import { ConfigComponent } from './components/config/config.component';
import { MainlayoutComponent } from './components/mainlayout/mainlayout.component';
import { PgnotfoundComponent } from './components/pgnotfound/pgnotfound.component';

const routes: Routes = [
  { path: 'auth', component: RegisterComponent, canActivate: [AuthRedirectGuard] },
  { path: 'index', component: LandpageComponent },
  { path: '404-page-not-found', component: PgnotfoundComponent },
  {
    path: '',
    component: MainlayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: 'home', component: HomeComponent },
      { path: 'profile', component: ProfileComponent },
      { path: 'pets', component: PetsComponent },
      { path: 'config', component: ConfigComponent },
      { path: '', redirectTo: 'home', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: '404-page-not-found' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
