import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Page } from '../models/product.models';
import {
  OrderRequest,
  OrderResponse,
  OrderStatus,
  OrderStatusUpdateRequest
} from '../models/order.models';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiUrl = '/api/orders';

  constructor(private http: HttpClient) {}

  placeOrder(request: OrderRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(this.apiUrl, request);
  }

  getMyOrders(page: number = 0, size: number = 20): Observable<Page<OrderResponse>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<Page<OrderResponse>>(`${this.apiUrl}/my`, { params });
  }

  getAllOrders(
    status?: OrderStatus,
    userId?: number,
    page: number = 0,
    size: number = 20
  ): Observable<Page<OrderResponse>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (status) {
      params = params.set('status', status);
    }
    if (userId) {
      params = params.set('userId', userId.toString());
    }
    return this.http.get<Page<OrderResponse>>(this.apiUrl, { params });
  }

  getOrderById(id: number): Observable<OrderResponse> {
    return this.http.get<OrderResponse>(`${this.apiUrl}/${id}`);
  }

  updateStatus(id: number, request: OrderStatusUpdateRequest): Observable<OrderResponse> {
    return this.http.patch<OrderResponse>(`${this.apiUrl}/${id}/status`, request);
  }
}
