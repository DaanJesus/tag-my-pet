import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfirmaDialogComponent } from './confirma-dialog.component';

describe('ConfirmaDialogComponent', () => {
  let component: ConfirmaDialogComponent;
  let fixture: ComponentFixture<ConfirmaDialogComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ConfirmaDialogComponent]
    });
    fixture = TestBed.createComponent(ConfirmaDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
