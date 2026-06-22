import axios from 'axios';

const API_BASE_URL =
  process.env.REACT_APP_API_URL || '';

// Types for sale requests
export interface SaleRequest {
  id: string;
  listingId: string;
  buyerId: string;
  sellerId: string;
  offerPrice: number;
  originalPrice: number;
  message: string;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED' | 'EXPIRED';
  paymentMethod: 'CASH' | 'UPI' | 'BANK_TRANSFER' | 'DIGITAL_WALLET' | 'OTHER';
  deliveryMethod: 'PICKUP' | 'DELIVERY' | 'MEETUP';
  deliveryCost: number;
  sellerResponse?: string;
  acceptedAt?: string;
  rejectedAt?: string;
  expiresAt: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateSaleRequestData {
  listingId: string;
  offerPrice: number;
  message: string;
  paymentMethod: string;
  deliveryMethod: string;
  deliveryCost?: number;
}

export interface SaleRequestResponse {
  sellerResponse: string;
}

class SaleRequestService {
  private getAuthHeaders() {
    const token = localStorage.getItem('accessToken');
    return {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    };
  }

  // Create a new sale request/offer
  async createSaleRequest(data: CreateSaleRequestData): Promise<SaleRequest> {
    const response = await axios.post(
      `${API_BASE_URL}/sale-requests/create`,
      data,
      { headers: this.getAuthHeaders() }
    );
    return response.data;
  }

  // Accept a sale request (seller action)
  async acceptSaleRequest(requestId: string, sellerResponse: string): Promise<any> {
    const response = await axios.post(
      `${API_BASE_URL}/sale-requests/${requestId}/accept`,
      { sellerResponse },
      { headers: this.getAuthHeaders() }
    );
    return response.data;
  }

  // Reject a sale request (seller action)
  async rejectSaleRequest(requestId: string, sellerResponse: string): Promise<SaleRequest> {
    const response = await axios.post(
      `${API_BASE_URL}/sale-requests/${requestId}/reject`,
      { sellerResponse },
      { headers: this.getAuthHeaders() }
    );
    return response.data;
  }

  // Cancel a sale request (buyer action)
  async cancelSaleRequest(requestId: string): Promise<SaleRequest> {
    const response = await axios.post(
      `${API_BASE_URL}/sale-requests/${requestId}/cancel`,
      {},
      { headers: this.getAuthHeaders() }
    );
    return response.data;
  }

  // Get pending requests for seller
  async getPendingRequestsForSeller(): Promise<SaleRequest[]> {
    const response = await axios.get(
      `${API_BASE_URL}/sale-requests/seller/pending`,
      { headers: this.getAuthHeaders() }
    );
    return response.data;
  }

  // Get pending requests for buyer
  async getPendingRequestsForBuyer(): Promise<SaleRequest[]> {
    const response = await axios.get(
      `${API_BASE_URL}/sale-requests/buyer/pending`,
      { headers: this.getAuthHeaders() }
    );
    return response.data;
  }

  // Get user's request history
  async getUserRequestHistory(): Promise<SaleRequest[]> {
    const response = await axios.get(
      `${API_BASE_URL}/sale-requests/history`,
      { headers: this.getAuthHeaders() }
    );
    return response.data;
  }

  // Get sale request by ID
  async getSaleRequestById(requestId: string): Promise<SaleRequest> {
    const response = await axios.get(
      `${API_BASE_URL}/sale-requests/${requestId}`,
      { headers: this.getAuthHeaders() }
    );
    return response.data;
  }

  // Get requests for a specific listing
  async getRequestsForListing(listingId: string): Promise<SaleRequest[]> {
    const response = await axios.get(
      `${API_BASE_URL}/sale-requests/listing/${listingId}`,
      { headers: this.getAuthHeaders() }
    );
    return response.data;
  }

  // Check if user has a pending request for a listing
  async checkPendingRequest(listingId: string): Promise<{ hasPendingRequest: boolean }> {
    const response = await axios.get(
      `${API_BASE_URL}/sale-requests/check-pending/${listingId}`,
      { headers: this.getAuthHeaders() }
    );
    return response.data;
  }

  // Get buyer request statistics
  async getBuyerRequestStats(): Promise<any> {
    const response = await axios.get(
      `${API_BASE_URL}/sale-requests/stats/buyer`,
      { headers: this.getAuthHeaders() }
    );
    return response.data;
  }

  // Get seller request statistics
  async getSellerRequestStats(): Promise<any> {
    const response = await axios.get(
      `${API_BASE_URL}/sale-requests/stats/seller`,
      { headers: this.getAuthHeaders() }
    );
    return response.data;
  }

  // Get request status options
  async getRequestStatusOptions(): Promise<string[]> {
    const response = await axios.get(
      `${API_BASE_URL}/sale-requests/status-options`,
      { headers: this.getAuthHeaders() }
    );
    return response.data;
  }
}

export const saleRequestService = new SaleRequestService(); 
