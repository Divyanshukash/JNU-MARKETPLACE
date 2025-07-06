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
  notifications: NotificationPreferences;
  privacy: PrivacySettings;
  theme: 'light' | 'dark';
  language: string;
}

export interface NotificationPreferences {
  email: boolean;
  push: boolean;
  sms: boolean;
  newMessages: boolean;
  newOffers: boolean;
  priceChanges: boolean;
  systemUpdates: boolean;
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

export interface Transaction {
  id: string;
  listing: Listing;
  buyer: User;
  seller: User;
  amount: number;
  currency: string;
  status: TransactionStatus;
  paymentMethod: PaymentMethod;
  paymentDetails: PaymentDetails;
  deliveryDetails: DeliveryDetails;
  disputeDetails?: DisputeDetails;
  refundDetails?: RefundDetails;
  review?: Review;
  createdAt: string;
  updatedAt: string;
}

export enum TransactionStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  SHIPPED = 'SHIPPED',
  DELIVERED = 'DELIVERED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  DISPUTED = 'DISPUTED',
  REFUNDED = 'REFUNDED'
}

export enum PaymentMethod {
  STRIPE = 'STRIPE',
  PAYPAL = 'PAYPAL',
  UPI = 'UPI',
  CASH = 'CASH'
}

export interface PaymentDetails {
  transactionId: string;
  paymentStatus: string;
  paymentDate: string;
  processingFee: number;
}

export interface DeliveryDetails {
  trackingNumber?: string;
  carrier?: string;
  estimatedDelivery?: string;
  actualDelivery?: string;
  deliveryAddress: Location;
}

export interface DisputeDetails {
  reason: string;
  description: string;
  evidence: string[];
  status: 'OPEN' | 'RESOLVED' | 'CLOSED';
  resolution?: string;
}

export interface RefundDetails {
  amount: number;
  reason: string;
  processedDate: string;
  refundMethod: string;
}

export interface Review {
  id: string;
  rating: number;
  comment: string;
  anonymous: boolean;
  createdAt: string;
}

export interface Notification {
  id: string;
  recipient: User;
  type: NotificationType;
  title: string;
  message: string;
  priority: NotificationPriority;
  read: boolean;
  delivered: boolean;
  metadata: Record<string, any>;
  createdAt: string;
  updatedAt: string;
}

export enum NotificationType {
  MESSAGE = 'MESSAGE',
  OFFER = 'OFFER',
  TRANSACTION = 'TRANSACTION',
  SYSTEM = 'SYSTEM',
  SECURITY = 'SECURITY'
}

export enum NotificationPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  URGENT = 'URGENT'
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
  accessToken: string;
  refreshToken: string;
  user: User;
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