import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../../core/services/order.service';
import { OrderResponse, OrderStatus } from '../../../core/models/order.models';

@Component({
  selector: 'app-order-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './order-management.component.html',
  styleUrl: './order-management.component.css'
})
export class OrderManagementComponent implements OnInit {
  readonly allStatuses = Object.values(OrderStatus);

  orders: OrderResponse[] = [];
  isLoading = true;
  error: string | null = null;

  statusFilter: OrderStatus | null = null;
  userIdFilter: number | null = null;

  page = 0;
  pageSize = 20;
  totalPages = 0;

  expandedOrderId: number | null = null;
  updatingOrderId: number | null = null;
  updateError: string | null = null;

  constructor(private orderService: OrderService) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.isLoading = true;
    this.error = null;

    this.orderService
      .getAllOrders(this.statusFilter ?? undefined, this.userIdFilter ?? undefined, this.page, this.pageSize)
      .subscribe({
        next: (result) => {
          this.orders = result.content;
          this.totalPages = result.totalPages;
          this.isLoading = false;
        },
        error: () => {
          this.error = 'Failed to load orders.';
          this.isLoading = false;
        }
      });
  }

  applyFilters(): void {
    this.page = 0;
    this.loadOrders();
  }

  clearFilters(): void {
    this.statusFilter = null;
    this.userIdFilter = null;
    this.applyFilters();
  }

  goToPage(newPage: number): void {
    if (newPage < 0 || newPage >= this.totalPages) return;
    this.page = newPage;
    this.loadOrders();
  }

  toggleExpand(orderId: number): void {
    this.expandedOrderId = this.expandedOrderId === orderId ? null : orderId;
  }

  updateStatus(order: OrderResponse, newStatus: OrderStatus): void {
    if (newStatus === order.status) return;

    this.updatingOrderId = order.id;
    this.updateError = null;

    this.orderService.updateStatus(order.id, { status: newStatus }).subscribe({
      next: (updated) => {
        this.updatingOrderId = null;
        const index = this.orders.findIndex((o) => o.id === updated.id);
        if (index !== -1) {
          this.orders[index] = updated;
        }
      },
      error: (err) => {
        this.updatingOrderId = null;
        this.updateError = err.error?.message || `Failed to update order #${order.id}.`;
      }
    });
  }
}
