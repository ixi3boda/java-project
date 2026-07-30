import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { UserLayoutComponent } from './layout/user-layout/user-layout.component';
import { AdminLayoutComponent } from './layout/admin-layout/admin-layout.component';
import { ProductListComponent } from './features/products/product-list/product-list.component';
import { CategoryBrowseComponent } from './features/catalog/category-browse/category-browse.component';
import { ProductManagementComponent } from './features/admin/product-management/product-management.component';
import { CategoryManagementComponent } from './features/admin/category-management/category-management.component';
import { AdminDashboardComponent } from './features/admin/dashboard/admin-dashboard.component';
import { OrderManagementComponent } from './features/admin/order-management/order-management.component';
import { UserManagementComponent } from './features/admin/user-management/user-management.component';
import { CartComponent } from './features/cart/cart.component';
import { OrderHistoryComponent } from './features/orders/order-history/order-history.component';
import { ProfileComponent } from './features/account/profile/profile.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },

  {
    path: '',
    component: UserLayoutComponent,
    children: [
      { path: '', redirectTo: 'products', pathMatch: 'full' },
      { path: 'products', component: ProductListComponent },
      { path: 'categories', component: CategoryBrowseComponent },
      { path: 'cart', component: CartComponent, canActivate: [authGuard] },
      { path: 'orders', component: OrderHistoryComponent, canActivate: [authGuard] },
      { path: 'profile', component: ProfileComponent, canActivate: [authGuard] }
    ]
  },

  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [authGuard, adminGuard],
    children: [
      { path: '', component: AdminDashboardComponent },
      { path: 'products', component: ProductManagementComponent },
      { path: 'categories', component: CategoryManagementComponent },
      { path: 'orders', component: OrderManagementComponent },
      { path: 'users', component: UserManagementComponent }
    ]
  },

  { path: '**', redirectTo: 'products' }
];
