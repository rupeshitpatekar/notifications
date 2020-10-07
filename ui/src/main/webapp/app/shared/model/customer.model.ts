import { Moment } from 'moment';

export interface ICustomer {
  id?: number;
  name?: string;
  mobile?: number;
  email?: string;
  address?: string;
  city?: string;
  firm_name?: string;
  birthday?: string;
  aniversary?: string;
}

export const defaultValue: Readonly<ICustomer> = {};
