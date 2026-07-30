import { ProductResponse } from './product.models';

export interface CartLine {
  product: ProductResponse;
  quantity: number;
}
