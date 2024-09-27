import { Component, Inject } from '@angular/core';
import { DialogPet } from '../pets.component';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { FormPetComponent } from '../form-pet/form-pet.component';

@Component({
  selector: 'app-info-pet',
  templateUrl: './info-pet.component.html',
  styleUrls: ['./info-pet.component.css']
})
export class InfoPetComponent {

  constructor(
    public dialog: MatDialog,
    public dialogRef: MatDialogRef<FormPetComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogPet
  ) { }

  updatePet(pet: any) {

    const dialogRef = this.dialog.open(FormPetComponent, {
      data: pet,
      hasBackdrop: true,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(res => {
      console.log(res);
    })
  }
}
