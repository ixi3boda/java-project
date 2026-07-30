import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { CartService } from '../../core/services/cart.service';
import { OrderService } from '../../core/services/order.service';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.css'
})
export class CartComponent {
  isPlacingOrder = false;
  error: string | null = null;
  placedOrderId: number | null = null;

  constructor(
    public cartService: CartService,
    private orderService: OrderService,
    private router: Router
  ) {}

  increment(productId: number, currentQuantity: number, stock: number): void {
    if (currentQuantity >= stock) return;
    this.cartService.updateQuantity(productId, currentQuantity + 1);
  }

  decrement(productId: number, currentQuantity: number): void {
    this.cartService.updateQuantity(productId, currentQuantity - 1);
  }

  remove(productId: number): void {
    this.cartService.removeItem(productId);
  }

  placeOrder(): void {
    if (this.cartService.lines().length === 0) return;

    this.isPlacingOrder = true;
    this.error = null;

    this.orderService.placeOrder(this.cartService.toOrderRequest()).subscribe({
      next: (order) => {
        this.isPlacingOrder = false;
        this.placedOrderId = order.id;
        this.cartService.clear();
      },
      error: (err) => {
        this.isPlacingOrder = false;
        this.error = err.error?.message || 'Failed to place order. Please check stock levels and try again.';
      }
    });
  }

  goToOrders(): void {
    this.router.navigate(['/orders']);
  }
}
