import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { UserProfileDetailComponent } from './user-profile-detail.component';

describe('UserProfile Management Detail Component', () => {
  let comp: UserProfileDetailComponent;
  let fixture: ComponentFixture<UserProfileDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserProfileDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./user-profile-detail.component').then(m => m.UserProfileDetailComponent),
              resolve: { userProfile: () => of({ id: 'a402ca77-da2d-40e0-a939-16b56471fa9c' }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(UserProfileDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UserProfileDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load userProfile on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', UserProfileDetailComponent);

      // THEN
      expect(instance.userProfile()).toEqual(expect.objectContaining({ id: 'a402ca77-da2d-40e0-a939-16b56471fa9c' }));
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
