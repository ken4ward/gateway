import dayjs from 'dayjs/esm';

import { IUserProfile, NewUserProfile } from './user-profile.model';

export const sampleWithRequiredData: IUserProfile = {
  id: '1e62951c-3577-473e-9b5c-0c413ad74cb9',
  username: 'gah after',
  email: '9m.$$@PT.zu',
  createdAt: dayjs('2025-05-30T20:02'),
};

export const sampleWithPartialData: IUserProfile = {
  id: '42f88af6-3793-4b8c-89f0-fd096d73ffce',
  username: 'but dishonor',
  email: '!@SQ.fkQ`u',
  age: 15453,
  createdAt: dayjs('2025-05-31T01:59'),
};

export const sampleWithFullData: IUserProfile = {
  id: '4ce540d0-5919-45d0-959a-6e8d2701a82a',
  username: 'inconsequential yum',
  email: '8@`rU!N.ii1U;b',
  age: 5153,
  createdAt: dayjs('2025-05-31T13:06'),
};

export const sampleWithNewData: NewUserProfile = {
  username: 'ick astride awesome',
  email: "lx@xr6D]F.'",
  createdAt: dayjs('2025-05-30T19:59'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
