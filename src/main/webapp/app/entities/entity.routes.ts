import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'gatewayApp.adminAuthority.home.title' },
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'user-profile',
    data: { pageTitle: 'gatewayApp.userProfile.home.title' },
    loadChildren: () => import('./user-profile/user-profile.routes'),
  },
  {
    path: 'order',
    data: { pageTitle: 'gatewayApp.order.home.title' },
    loadChildren: () => import('./order/order.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
