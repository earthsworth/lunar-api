import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function intToHexColor(argb: number): string {
  const rgb = argb & 0x00ffffff;
  return `#${rgb.toString(16).padStart(6, '0')}`;
}
