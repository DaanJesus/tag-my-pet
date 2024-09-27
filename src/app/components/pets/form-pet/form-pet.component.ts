import { AfterViewInit, ChangeDetectorRef, Component, Inject, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DialogPet } from '../pets.component';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSelect } from '@angular/material/select';
import { PetService } from 'src/app/services/pet.service';
import * as QRCode from 'qrcode';
import { ConfirmaDialogComponent } from '../confirma-dialog/confirma-dialog.component';
import { AuthService } from 'src/app/services/auth.service';

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
    @Inject(MAT_DIALOG_DATA) public data: DialogPet
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

  formPet!: FormGroup;

  options: string[] = ['Cachorro', 'Gato', 'Coelho', 'Hamster', 'Porquinho-da-índia', 'Furão', 'Ave', 'Réptil'];
  filteredTypes: string[] = [];

  breeds: string[] = []
  filteredBreeds: string[] = [];

  currentStep = 1;
  progressValue = 20;

  qrCodeUrl = ''

  dialogWidth = window.innerWidth < 768 ? '400px' : '200px';

  ngOnInit() {
    this.filteredTypes = this.options;

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

  onFileChange(event: any) {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        const preview = document.querySelector('.photo-preview') as HTMLImageElement;
        if (preview) {
          preview.src = e.target.result;
          preview.style.display = 'block'; // Mostra a imagem
        }
      };
      reader.readAsDataURL(file);
    }
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

  // Filtra as opções com base no texto de pesquisa
  filterType(event: Event) {
    const input = event.target as HTMLInputElement | null;
    if (input) {
      const filterValue = input.value.toLowerCase();
      if (filterValue.length >= 2) {
        // Filtra as opções baseadas no valor do campo de pesquisa
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
        // Filtra as opções baseadas no valor do campo de pesquisa
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

  onNoClick(): void {
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
        return true; // Se a etapa 5 não exigir validação
      default:
        return false;
    }
  }

}