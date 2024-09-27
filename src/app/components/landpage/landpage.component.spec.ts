import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LandpageComponent } from './landpage.component';

describe('LandpageComponent', () => {
  let component: LandpageComponent;
  let fixture: ComponentFixture<LandpageComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [LandpageComponent]
    });
    fixture = TestBed.createComponent(LandpageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
