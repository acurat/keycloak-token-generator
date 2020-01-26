export interface Tokens {
  accessToken: string;
  refreshToken?: string;
  idToken?: string;
}

export interface Keys {
  [key: string]: string | JSON;
}

export enum SupportedTypes {
    String = 'string',
    Number = 'number',
    Date = 'date',
    Object = 'object',
}