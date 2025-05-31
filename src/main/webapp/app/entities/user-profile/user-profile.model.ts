import dayjs from 'dayjs/esm';

export interface IUserProfile {
  id: string;
  username?: string | null;
  email?: string | null;
  age?: number | null;
  createdAt?: dayjs.Dayjs | null;
}

export type NewUserProfile = Omit<IUserProfile, 'id'> & { id: null };
