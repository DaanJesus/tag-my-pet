import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PetService } from 'src/app/services/pet.service';

@Component({
  selector: 'app-pet-info-qrcode',
  templateUrl: './pet-info-qrcode.component.html',
  styleUrls: ['./pet-info-qrcode.component.css']
})
export class PetInfoQrcodeComponent {
  pet: any;

  constructor(
    private route: ActivatedRoute,
    private petService: PetService
  ) { }

  ngOnInit(): void {
    const petId = this.route.snapshot.paramMap.get('id');

    if (petId) {
      this.petService.getPetById(petId).subscribe(petData => {
        this.pet = petData;
      });
    } else {
      console.error('ID do pet não encontrado.');
    }
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
}