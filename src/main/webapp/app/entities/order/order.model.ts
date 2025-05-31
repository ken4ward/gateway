import dayjs from 'dayjs/esm';
import { IUserProfile } from 'app/entities/user-profile/user-profile.model';
import { OrderStatus } from 'app/entities/enumerations/order-status.model';

export interface IOrder {
  id: string;
  orderNumber?: string | null;
  amount?: number | null;
  status?: keyof typeof OrderStatus | null;
  createdAt?: dayjs.Dayjs | null;
  userProfile?: Pick<IUserProfile, 'id' | 'username'> | null;
}

export type NewOrder = Omit<IOrder, 'id'> & { id: null };
