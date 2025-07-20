export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  bio?: string;
  profilePicture?: string;
  role: UserRole;
  status: UserStatus;
  preferences: UserPreferences;
  wishlist: string[];
  followers: string[];
  following: string[];
  rating: number;
  totalRatings: number;
  createdAt: string;
  updatedAt: string;
}

export enum UserRole {
  USER = 'USER',
  ADMIN = 'ADMIN',
  MODERATOR = 'MODERATOR'
}

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED',
  PENDING_VERIFICATION = 'PENDING_VERIFICATION'
}

export interface UserPreferences {
  privacy: PrivacySettings;
  theme: 'light' | 'dark';
  language: string;
}

export interface PrivacySettings {
  profileVisibility: 'public' | 'private' | 'friends';
  showEmail: boolean;
  showPhone: boolean;
  allowMessages: boolean;
}

export interface Listing {
  id: string;
  title: string;
  description: string;
  category: string;
  condition: string;
  price: number;
  negotiable: boolean;
  images: string[];
  location: Location;
  seller: User;
  status: ListingStatus;
  views: number;
  favorites: string[];
  tags: string[];
  createdAt: string;
  updatedAt: string;
}

export enum ListingStatus {
  ACTIVE = 'ACTIVE',
  SOLD = 'SOLD',
  EXPIRED = 'EXPIRED',
  SUSPENDED = 'SUSPENDED',
  DRAFT = 'DRAFT'
}

export interface Location {
  address: string;
  city: string;
  state: string;
  zipCode: string;
  coordinates?: {
    latitude: number;
    longitude: number;
  };
}

export interface Message {
  id: string;
  conversationId: string;
  sender: User;
  recipient: User;
  content: string;
  messageType: MessageType;
  attachments: Attachment[];
  read: boolean;
  delivered: boolean;
  replyTo?: Message;
  systemMessage: boolean;
  createdAt: string;
  updatedAt: string;
}

export enum MessageType {
  TEXT = 'TEXT',
  IMAGE = 'IMAGE',
  FILE = 'FILE',
  SYSTEM = 'SYSTEM'
}

export interface Attachment {
  id: string;
  fileName: string;
  fileUrl: string;
  fileSize: number;
  mimeType: string;
}

export interface Review {
  id: string;
  rating: number;
  comment: string;
  anonymous: boolean;
  createdAt: string;
}

// API Request/Response Types
export interface AuthRequest {
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  user: User;
  accessToken?: string; // for backward compatibility, but not used
}

export interface ListingRequest {
  title: string;
  description: string;
  category: string;
  condition: string;
  price: number;
  negotiable: boolean;
  images: string[];
  location: Location;
}

export interface SearchRequest {
  query?: string;
  category?: string;
  minPrice?: number;
  maxPrice?: number;
  condition?: string;
  location?: string;
  sortBy?: 'price' | 'date' | 'relevance';
  sortOrder?: 'asc' | 'desc';
}

export interface ApiResponse<T> {
  data: T;
  message: string;
  success: boolean;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
} 