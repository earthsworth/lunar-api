export type RestBean<T> = {
  code: number;
  data: T | null;
  message: string;
}

export type AuthorizeVO = {
  username: string;
  token: string;
  expire: number;
  roles: string[];
}

export type UserVO = {
  id: string;
  username: string;
  uuid: string;
  roles: string[];
  logoColor: number;
}

export type SongVO = {
  id: string
  name: string
  thumbnail: string
  songName: string
  artist: string
  album: string
  durationMillis: number
  uploadId: string
  createdAt: number
}