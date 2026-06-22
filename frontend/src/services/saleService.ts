import axios from 'axios';

const API_BASE_URL =
  process.env.REACT_APP_API_URL || '';

// Types for sales
export interface Sale {
  id: string;
  listingId: string;
  sellerId: string;
  buyerId: string;
  salePrice: number;
  originalPrice: number;
  negotiatedPrice?: number;
  saleDate: string;
  paymentMethod: 'CASH' | 'UPI' | 'BANK_TRANSFER' | 'DIGITAL_WALLET' | 'OTHER';
  deliveryMethod: 'PICKUP' | 'DELIVERY' | 'MEETUP';
  deliveryCost: number;
  totalAmount: number;
  status: 'PENDING' | 'COMPLETED' | 'CANCELLED' | 'REFUNDED';
  notes?: string;
  buyerRating?: number;
  sellerRating?: number;
  buyerFeedback?: string;
  sellerFeedback?: string;
  createdAt: string;
  updatedAt: string;
}

export interface SaleStats {
  totalSales: number;
  totalRevenue: number;
  totalDeliveryRevenue: number;
  averageSalePrice: number;
}

class SaleService {
  private getAuthHeaders() {
    const token = localStorage.getItem('accessToken');
    return {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    };
  }

  // Check if a listing has been sold
  async checkIfSold(listingId: string): Promise<{ isSold: boolean }> {
    const response = await axios.get(
      `${API_BASE_URL}/api/sales/check-sold/${listingId}`,
      { headers: this.getAuthHeaders() }
    );
    return response.data;
  }

  // Get user's sale history (both as buyer and seller)
  async getUserSaleHistory(): Promise<Sale[]> {
    const response = await axios.get(
      `${API_BASE_URL}/api/sales/history`,
      { headers: this.getAuthHeaders() }
    );
    return response.data;
  }

  // Get user's sales (as seller)
  async getUserSales(): Promise<Sale[]> {
    const response = await axios.get(
      `${API_BASE_URL}/api/sales/my-sales`,
      { headers: this.getAuthHeaders() }
    );
    return response.data;
  }

  // Get user's purchases (as buyer)
  async getUserPurchases(): Promise<Sale[]> {
    const response = await axios.get(
      `${API_BASE_URL}/api/sales/my-purchases`,
      { headers: this.getAuthHeaders() }
    );
    return response.data;
  }

  // Get sale by listing ID
  async getSaleByListingId(listingId: string): Promise<Sale | null> {
    try {
      const response = await axios.get(
        `${API_BASE_URL}/api/sales/listing/${listingId}`,
        { headers: this.getAuthHeaders() }
      );
      return response.data;
    } catch (error) {
      return null;
    }
  }

  // Add rating and feedback for a sale
  async addRating(saleId: string, rating: number, feedback: string, isBuyerRating: boolean): Promise<Sale> {
    const response = await axios.post(
      `${API_BASE_URL}/api/sales/${saleId}/rate`,
      { rating, feedback, isBuyerRating },
      { headers: this.getAuthHeaders() }
    );
    return response.data;
  }

  // Get user's revenue statistics
  async getUserRevenueStats(): Promise<SaleStats> {
    const response = await axios.get(
      `${API_BASE_URL}/api/sales/revenue-stats`,
      { headers: this.getAuthHeaders() }
    );
    return response.data;
  }

  // Cancel a sale (admin or seller only)
  async cancelSale(saleId: string, reason: string): Promise<Sale> {
    const response = await axios.post(
      `${API_BASE_URL}/api/sales/${saleId}/cancel`,
      { reason },
      { headers: this.getAuthHeaders() }
    );
    return response.data;
  }

  // Get payment methods
  async getPaymentMethods(): Promise<string[]> {
    const response = await axios.get(
      `${API_BASE_URL}/api/sales/payment-methods`,
      { headers: this.getAuthHeaders() }
    );
    return response.data;
  }

  // Get delivery methods
  async getDeliveryMethods(): Promise<string[]> {
    const response = await axios.get(
      `${API_BASE_URL}/api/sales/delivery-methods`,
      { headers: this.getAuthHeaders() }
    );
    return response.data;
  }
}

export const saleService = new SaleService(); 
