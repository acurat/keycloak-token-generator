export interface Tokens {
  accessToken: string;
  refreshToken?: string;
  idToken?: string;
}

export interface Keys {
  [key: string]: string | JSON;
}
