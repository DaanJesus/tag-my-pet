import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { DialogPet } from '../pets.component';

@Component({
  selector: 'app-confirma-dialog',
  templateUrl: './confirma-dialog.component.html',
  styleUrls: ['./confirma-dialog.component.css']
})
export class ConfirmaDialogComponent {

  qrCodeUrl = '';

  constructor(
    public dialogRef: MatDialogRef<ConfirmaDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogPet
  ) { }
}
