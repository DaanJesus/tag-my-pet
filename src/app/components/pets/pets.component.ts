import { animate, state, style, transition, trigger } from '@angular/animations';
import { AfterViewInit, ChangeDetectorRef, Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { FormPetComponent } from './form-pet/form-pet.component';
import { InfoPetComponent } from './info-pet/info-pet.component';
import { PetService } from 'src/app/services/pet.service';
import { AuthService } from 'src/app/services/auth.service';
import { ConfirmaDialogComponent } from './confirma-dialog/confirma-dialog.component';

export interface DialogPet {
  type: string,
  breed: string,
  birthDate: string,
  furColor: string,
  weight: string,
  name: string,
  photo: string,
  sex: string,
  medicalInfo: string,
  castrated: string,
  qrCode: string,
}

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
    private cdr: ChangeDetectorRef
  ) { }

  pets: any = [];

  ngOnInit(): void {
    this.authService.getCurrentUserId().subscribe(user => {
      if (user) {
        this.petService.getMyPets(user).subscribe(res => {
          if (res) {
            setTimeout(() => {
              this.pets = res;
            });
          }
        });
      }
    });
  }

  ngAfterViewInit(): void {
    const dialogWidth = window.innerWidth < 768 ? '100%' : '400px'

    this.dialog.open(ConfirmaDialogComponent, {
      width: dialogWidth,
      data: {
        "_id": "66ef24a322f80dc9ebccd6d8",
        "type": "Hamster",
        "breed": "Hamster Chinês",
        "birthDate": {
          "$date": "2023-11-07T03:00:00.000Z"
        },
        "furColor": "Curto/Cinza",
        "weight": 1,
        "name": "Luna",
        "photo": "https://p2.trrsf.com/image/fget/cf/1200/1200/middle/images.terra.com/2023/12/20/1527502278-golden-retriever.jpg",
        "sex": "Fêmea",
        "medicalInfo": "É doidona",
        "castrated": "Não",
        "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAJQAAACUCAYAAAB1PADUAAAAAklEQVR4AewaftIAAAVwSURBVO3BwY1dSw5EwaPC80cm5Eom0EquZEJaptlyVcDFZfd8CRnx4+fvX3+IWHKIWHSIWHSIWHSIWHSIWHSIWHSIWHSIWHSIWHSIWHSIWHSIWHSIWHSIWHSIWPThJav5TnLxhtXcyMUTVjPJxY3VTHJxYzXfSS7eOEQsOkQsOkQs+rBMLjZZzRtWM8nFE1ZzIxeT1dzIxWQ1k1zcyMUmq9l0iFh0iFh0iFj04YtZzRNy8YTV3MjFZDU3cjHJxY3VTHIxWc1kNZNcvGE1T8jFVzpELDpELDpELPrwj7OaJ6xmkovJam6s5kYuJquZ5OJvdohYdIhYdIhY9OEfJxeT1TxhNZNcvGE1k1z8Sw4Riw4Riw4Riz58Mbn4TnIxWc0kF29YzSQXk9XcyMVkNZNcPCEX/yWHiEWHiEWHiEUfllnN38RqJrmYrGaSi8lqJrmYrGaT1fyXHSIWHSIWHSIWfXhJLv4mVjPJxY1cTFazSS5u5OJvcohYdIhYdIhY9OElq5nkYrKaTXIxycVkNTdysUkuJquZ5OINq9kkF1/pELHoELHoELHox8/fv/6wyGo2ycUmq3lCLm6sZpKLyWqekIvJat6Qi8lqbuTijUPEokPEokPEoh8/f//6wwtWcyMXb1jNJrm4sZpNcrHJap6Qi+90iFh0iFh0iFj04SW5eMNqbuTixmqesJon5OINq5nkYrKaG7nYZDWTXGw6RCw6RCw6RCz68JLV3MjFZDWTXExWM1nNG3IxWc0kFzdWcyMXk9V8Jbl4wmomufhKh4hFh4hFh4hFP37+/vWHF6zmRi4mq7mRizes5g25eMJqbuRisppJLiar2SQXk9XcyMUbh4hFh4hFh4hFP37+/vWHF6xmkovJajbJxf+T1dzIxWQ1k1w8YTWTXExWM8nFZDWTXHylQ8SiQ8SiQ8SiD8us5g25eMJqJrm4sZpJLm6sZpKLN6xmkotNVjPJxWQ1N3LxxiFi0SFi0SFi0YeX5OIJq5nkYrKaJ+RisppJLm6sZpKLSS4mq5nkYrKaG7mYrOZGLiarmeTixmomuZisZtMhYtEhYtEhYtGHl6zmCbl4Qi4mq7mRixu5mKxmspobuZisZpKLG6uZ5OLGam6sZpKLG6v5SoeIRYeIRYeIRR+WycVkNU/IxWQ1k1xMVjPJxY3VvGE1k1zcWM0kF0/IxWQ1k1xMVjPJxXc6RCw6RCw6RCz68MXkYrKaSS5u5GKymkkuvpJcbLKaf9khYtEhYtEhYtGHbyYXN1bzhtU8IRc3VnMjF5PVTHJxYzVvWM0kF5PVTHLxlQ4Riw4Riw4Riz68JBeb5OIJq5nk4sZqnpCLG6uZ5GKymkkubuTiCauZrOYJq5nk4o1DxKJDxKJDxKIPL1nNd5KLSS4mq3nCaia5uLGaJ+RisponrGaSixu5mKzmRi42HSIWHSIWHSIWfVgmF5us5sZqJrl4wmpurOYNq3lDLp6wmkkuJqu5kYs3DhGLDhGLDhGLPnwxq3lCLr6TXNzIxWQ1k1xMVjPJxY3VTFbzhlxMVjPJxVc6RCw6RCw6RCz68I+xmkkuJquZ5GKymhu5mKxmkovJam7kYrKaSS6esJpJLiaruZGLNw4Riw4Riw4Riz78Y+RisppJLiarmeTixmpurOYNuZisZpKLyWrekItNh4hFh4hFh4hFH76YXHwlubiRixu5mKzmCbl4w2omuZjk4m9yiFh0iFh0iFj0YZnVfCeruZGLyWomuZjk4gmreUMuJqt5Qy4mq5nk4isdIhYdIhYdIhb9+Pn71x8ilhwiFh0iFh0iFh0iFh0iFh0iFh0iFh0iFh0iFh0iFh0iFh0iFh0iFh0iFh0iFv0P8hEkSGWXZqAAAAAASUVORK5CYII=",
        "mentor": "66cda445335393eacefb8e7a",
        "__v": 0
      }
    })

  }

  viewInfoPet(pet: DialogPet) {
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

  calculateAge(birthDate: string): string {
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

}