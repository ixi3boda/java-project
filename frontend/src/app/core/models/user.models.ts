export interface UserSelfUpdateRequest {
  email?: string;
  firstName?: string;
  lastName?: string;
}

export interface AdminUserCreateRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  roles: string[];
}

export interface AdminUserUpdateRequest {
  email?: string;
  firstName?: string;
  lastName?: string;
  roles?: string[];
}
