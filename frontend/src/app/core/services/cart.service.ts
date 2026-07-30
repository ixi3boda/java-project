import { Injectable, computed, signal } from '@angular/core';
import { ProductResponse } from '../models/product.models';
import { CartLine } from '../models/cart.models';
import { OrderRequest } from '../models/order.models';

const STORAGE_KEY = 'cart_lines';

interface StoredLine {
  product: ProductResponse;
  quantity: number;
}

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private linesSignal = signal<CartLine[]>(this.readFromStorage());

  readonly lines = this.linesSignal.asReadonly();

  readonly itemCount = computed(() =>
    this.linesSignal().reduce((sum, line) => sum + line.quantity, 0)
  );

  readonly subtotal = computed(() =>
    this.linesSignal().reduce((sum, line) => sum + line.product.price * line.quantity, 0)
  );

  addItem(product: ProductResponse, quantity: number = 1): void {
    const lines = [...this.linesSignal()];
    const existing = lines.find((l) => l.product.id === product.id);

    if (existing) {
      existing.quantity = Math.min(existing.quantity + quantity, product.stockQuantity);
    } else {
      lines.push({ product, quantity: Math.min(quantity, product.stockQuantity) });
    }

    this.commit(lines);
  }

  updateQuantity(productId: number, quantity: number): void {
    const lines = this.linesSignal()
      .map((l) => (l.product.id === productId ? { ...l, quantity } : l))
      .filter((l) => l.quantity > 0);

    this.commit(lines);
  }

  removeItem(productId: number): void {
    this.commit(this.linesSignal().filter((l) => l.product.id !== productId));
  }

  clear(): void {
    this.commit([]);
  }

  toOrderRequest(): OrderRequest {
    return {
      items: this.linesSignal().map((l) => ({
        productId: l.product.id,
        quantity: l.quantity
      }))
    };
  }

  private commit(lines: CartLine[]): void {
    this.linesSignal.set(lines);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(lines as StoredLine[]));
  }

  private readFromStorage(): CartLine[] {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? (JSON.parse(raw) as StoredLine[]) : [];
    } catch {
      return [];
    }
  }
}
