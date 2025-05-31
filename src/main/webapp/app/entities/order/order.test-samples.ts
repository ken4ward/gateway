import dayjs from 'dayjs/esm';

import { IOrder, NewOrder } from './order.model';

export const sampleWithRequiredData: IOrder = {
  id: 'ca41e08f-fbee-4803-8f4f-67340147c97a',
  orderNumber: 'why meanwhile editor',
  amount: 16708.06,
  status: 'PENDING',
  createdAt: dayjs('2025-05-31T07:22'),
};

export const sampleWithPartialData: IOrder = {
  id: '9a24a73a-e75c-4fc7-b763-7bbdb55b537a',
  orderNumber: 'punctually regularly nervously',
  amount: 6767.71,
  status: 'CANCELLED',
  createdAt: dayjs('2025-05-31T09:21'),
};

export const sampleWithFullData: IOrder = {
  id: 'd7316eea-1023-4f9d-812e-54cfb50e8167',
  orderNumber: 'duh',
  amount: 12034.51,
  status: 'PROCESSING',
  createdAt: dayjs('2025-05-30T18:53'),
};

export const sampleWithNewData: NewOrder = {
  orderNumber: 'boohoo dishonor',
  amount: 27520.17,
  status: 'CANCELLED',
  createdAt: dayjs('2025-05-30T19:28'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
