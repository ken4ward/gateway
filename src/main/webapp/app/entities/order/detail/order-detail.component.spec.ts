import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { OrderDetailComponent } from './order-detail.component';

describe('Order Management Detail Component', () => {
  let comp: OrderDetailComponent;
  let fixture: ComponentFixture<OrderDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrderDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./order-detail.component').then(m => m.OrderDetailComponent),
              resolve: { order: () => of({ id: '725d2011-6ba1-4eda-91ca-9b09578eec1a' }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(OrderDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OrderDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load order on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', OrderDetailComponent);

      // THEN
      expect(instance.order()).toEqual(expect.objectContaining({ id: '725d2011-6ba1-4eda-91ca-9b09578eec1a' }));
    });
  });

  describe('PreviousState', () => {
    it('should navigate to previous state', () => {
      jest.spyOn(window.history, 'back');
      comp.previousState();
      expect(window.history.back).toHaveBeenCalled();
    });
  });
});
