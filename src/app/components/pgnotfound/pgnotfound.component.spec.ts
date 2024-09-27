import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PgnotfoundComponent } from './pgnotfound.component';

describe('PgnotfoundComponent', () => {
  let component: PgnotfoundComponent;
  let fixture: ComponentFixture<PgnotfoundComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PgnotfoundComponent]
    });
    fixture = TestBed.createComponent(PgnotfoundComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
