export interface RestBean<T> {
  code: number;
  data: T | null;
  message: string;
}

export interface AuthorizeVO {
  username: string;
  token: string;
  expire: number;
  roles: string[];
}
