import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PetInfoQrcodeComponent } from './pet-info-qrcode.component';

describe('PetInfoQrcodeComponent', () => {
  let component: PetInfoQrcodeComponent;
  let fixture: ComponentFixture<PetInfoQrcodeComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PetInfoQrcodeComponent]
    });
    fixture = TestBed.createComponent(PetInfoQrcodeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
