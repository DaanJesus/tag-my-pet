import { AfterViewInit, ChangeDetectorRef, Component, Inject, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { FormPetComponent } from './form-pet/form-pet.component';
import { InfoPetComponent } from './info-pet/info-pet.component';
import { PetService } from 'src/app/services/pet.service';
import { AuthService } from 'src/app/services/auth.service';
import { Pet } from 'src/app/models/Pet';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-pets',
  templateUrl: './pets.component.html',
  styleUrls: ['./pets.component.css'],
})
export class PetsComponent implements OnInit, AfterViewInit {

  constructor(
    public dialog: MatDialog,
    public petService: PetService,
    public authService: AuthService,
  ) { }

  pets: Pet[] = [];
  subscription: Subscription = new Subscription();

  ngOnInit(): void {
    const userSubscription = this.authService.getCurrentUserId().subscribe(user => {
      if (user) {
        const petsSubscription = this.petService.getMyPets(user).subscribe(res => {
          if (res) {
            this.pets = res;
          }
        });
        this.subscription.add(petsSubscription);
      }
    });

    this.subscription.add(userSubscription);
  }

  ngAfterViewInit(): void {
    const dialogWidth = window.innerWidth < 768 ? '100%' : '400px'

  }

  viewInfoPet(pet: Pet) {
    this.dialog.open(InfoPetComponent, {
      data: pet
    });
  }

  addNewPet() {
    this.dialog.open(FormPetComponent, {
      hasBackdrop: true,
      disableClose: true
    });
  }

  calculateAge(birthDate: Date): string {
    const birth = new Date(birthDate);
    const today = new Date();
    const diffTime = today.getTime() - birth.getTime();

    const ageYears = today.getFullYear() - birth.getFullYear() -
      (today < new Date(today.getFullYear(), birth.getMonth(), birth.getDate()) ? 1 : 0);

    if (ageYears > 1) return `${ageYears} anos`;
    if (ageYears === 1) return `${ageYears} ano`;

    const ageMonths = today.getMonth() - birth.getMonth() +
      (today.getDate() < birth.getDate() ? -1 : 0) +
      (ageYears === 0 && today.getMonth() < birth.getMonth() ? 12 : 0);

    if (ageMonths === 1) return '1 mês';
    if (ageMonths > 1) return `${ageMonths} meses`;

    const ageDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
    return ageDays < 30 ? 'Menos de 1 mês' : `${ageMonths} meses`;
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

}