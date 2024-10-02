import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, Inject, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSelect } from '@angular/material/select';
import { PetService } from 'src/app/services/pet.service';
import * as QRCode from 'qrcode';
import { ConfirmaDialogComponent } from '../confirma-dialog/confirma-dialog.component';
import { AuthService } from 'src/app/services/auth.service';
import { Pet } from 'src/app/models/Pet';

@Component({
  selector: 'app-form-pet',
  templateUrl: './form-pet.component.html',
  styleUrls: ['./form-pet.component.css']
})
export class FormPetComponent implements OnInit, AfterViewInit {

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<FormPetComponent>,
    public dialog: MatDialog,
    private petService: PetService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    @Inject(MAT_DIALOG_DATA) public data: Pet
  ) {
    this.formPet = this.fb.group({
      type: ['Gato', Validators.required],
      breed: ['Siames', Validators.required],
      birthDate: ['', Validators.required],
      furColor: ['Curto/Cinza', Validators.required],
      sex: ['Fêmea', Validators.required],
      castrated: ['Não', Validators.required],
      name: ['Luna', Validators.required],
      photo: ['https://p2.trrsf.com/image/fget/cf/1200/1200/middle/images.terra.com/2023/12/20/1527502278-golden-retriever.jpg', Validators.required],
      weight: ['2', Validators.required],
      medicalInfo: ['É doidona'],
      qrCode: [''],
      mentor: [this.authService.getCurrentUserId(), Validators.required]
    });
  }

  @ViewChild('select') select: MatSelect | undefined;
  @ViewChild('fileInput') fileInput!: ElementRef;

  formPet!: FormGroup;

  options: string[] = ['Cachorro', 'Gato', 'Coelho', 'Hamster', 'Porquinho-da-índia', 'Furão', 'Ave', 'Réptil'];
  filteredTypes: string[] = [];

  breeds: string[] = []
  filteredBreeds: string[] = [];

  currentStep = 6;
  progressValue = 20;

  qrCodeUrl = ''

  dialogWidth = window.innerWidth < 768 ? '100%' : '400px'

  ngOnInit() {
    this.filteredTypes = this.options;

    const currentPhoto = this.formPet.get('photo')?.value;
    if (currentPhoto) {
      this.cdr.detectChanges(); // Isso vai forçar a atualização da UI
    }

    this.formPet.statusChanges.subscribe(() => {
      this.isStepValid(this.currentStep);
    });

  }

  closeDialog() {
    this.dialogRef.close();
  }

  ngAfterViewInit(): void {
    if (this.data) {
      setTimeout(() => {
        this.loadBreeds(this.data.type);

        this.formPet = this.fb.group({
          type: [this.data.type],
          breed: [this.data.breed],
          birthDate: [this.data.birthDate],
          furColor: [this.data.furColor],
          weight: [this.data.weight],
          name: [this.data.name],
          photo: [this.data.photo],
          sex: [this.data.sex],
          medicalInfo: [this.data.medicalInfo],
          castrated: [this.data.castrated],
          qrCode: [this.data.qrCode],
        });
      });
    }
  }

  nextStep() {
    if (this.currentStep < 6) {
      this.currentStep++;
      this.updateProgress();
    }
  }

  previousStep() {
    if (this.currentStep > 1) {
      this.currentStep--;
      this.updateProgress();
    }
  }

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      const reader = new FileReader();

      reader.onload = () => {
        const img = new Image();
        img.src = reader.result as string;

        img.onload = () => {
          const MAX_WIDTH = 300;
          const MAX_HEIGHT = 300;
          let width = img.width;
          let height = img.height;

          // Redimensiona a imagem
          if (width > height) {
            if (width > MAX_WIDTH) {
              height *= MAX_WIDTH / width;
              width = MAX_WIDTH;
            }
          } else {
            if (height > MAX_HEIGHT) {
              width *= MAX_HEIGHT / height;
              height = MAX_HEIGHT;
            }
          }

          const canvas = document.createElement('canvas');
          canvas.width = width;
          canvas.height = height;

          const ctx = canvas.getContext('2d');
          if (ctx) {
            ctx.drawImage(img, 0, 0, width, height);
            this.formPet.patchValue({ photo: canvas.toDataURL('image/jpeg') });
            this.cdr.detectChanges();
          }
        };
      };

      reader.readAsDataURL(file);
    }
  }

  resetFileInput(): void {
    const fileInput = this.fileInput.nativeElement;
    if (fileInput) {
      fileInput.value = '';
    }
    this.formPet.patchValue({ photo: null });
  }


  onSubmit() {

    if (this.formPet.valid) {

      this.authService.getCurrentUserId().subscribe((mentorId: any) => {
        if (mentorId) {
          const petData = {
            ...this.formPet.value,
            mentor: mentorId
          };

          this.petService.createPet(petData).subscribe(res => {
            if (res) {
              this.dialog.open(ConfirmaDialogComponent, {
                width: this.dialogWidth,
                data: res
              });
              this.resetFileInput();
              this.onCloseDialog()
            }
          });
        } else {
          console.error('Usuário não logado');
        }
      });
    } else {
      console.log(this.formPet.errors);
    }
  }

  updateProgress() {
    this.progressValue = (this.currentStep / 6) * 100;
  }

  filterType(event: Event) {
    const input = event.target as HTMLInputElement | null;
    if (input) {
      const filterValue = input.value.toLowerCase();
      if (filterValue.length >= 2) {
        this.filteredTypes = this.filteredTypes.filter(option =>
          option.toLowerCase().includes(filterValue)
        );
      }
    }
  }

  filterBreed(event: Event) {
    const input = event.target as HTMLInputElement | null;
    if (input) {
      const filterValue = input.value.toLowerCase();
      if (filterValue.length >= 2) {
        this.filteredBreeds = this.filteredBreeds.filter(option =>
          option.toLowerCase().includes(filterValue)
        );
      }
    }
  }

  loadBreeds(petType: any) {
    this.petService.getBreedsByType(petType).subscribe(breeds => {
      this.breeds = breeds;
      this.filteredBreeds = this.breeds;
    })

  }

  onCloseDialog(): void {
    this.dialogRef.close();
  }

  isStepValid(step: number): boolean {
    switch (step) {
      case 1:
        return !!(this.formPet.get('type')?.valid ?? false) && !!(this.formPet.get('breed')?.valid ?? false);
      case 2:
        return !!(this.formPet.get('birthDate')?.valid ?? false) && !!(this.formPet.get('furColor')?.valid ?? false);
      case 3:
        return !!(this.formPet.get('sex')?.valid ?? false) && !!(this.formPet.get('castrated')?.valid ?? false);
      case 4:
        return !!(this.formPet.get('name')?.valid ?? false);
      case 5:
        return true;
      default:
        return false;
    }
  }

}