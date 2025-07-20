import React, { useState, useEffect } from 'react';
import { X, IndianRupee, CreditCard, Truck, MessageSquare } from 'lucide-react';
import { saleRequestService, CreateSaleRequestData } from '../services/saleRequestService';

interface MakeOfferModalProps {
  isOpen: boolean;
  onClose: () => void;
  listing: any;
  onSuccess: () => void;
}

const MakeOfferModal: React.FC<MakeOfferModalProps> = ({ isOpen, onClose, listing, onSuccess }) => {
  const [formData, setFormData] = useState<CreateSaleRequestData>({
    listingId: listing?.id || '',
    offerPrice: listing?.price || 0,
    message: '',
    paymentMethod: 'CASH',
    deliveryMethod: 'PICKUP',
    deliveryCost: 0,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [paymentMethods, setPaymentMethods] = useState<string[]>([]);
  const [deliveryMethods, setDeliveryMethods] = useState<string[]>([]);

  useEffect(() => {
    if (listing) {
      setFormData(prev => ({
        ...prev,
        listingId: listing.id,
        offerPrice: listing.price,
      }));
    }
  }, [listing]);

  useEffect(() => {
    // Load payment and delivery methods
    const loadMethods = async () => {
      try {
        const [payMethods, delMethods] = await Promise.all([
          saleRequestService.getRequestStatusOptions(), // This will be updated to actual payment methods
          saleRequestService.getRequestStatusOptions(), // This will be updated to actual delivery methods
        ]);
        setPaymentMethods(['CASH', 'UPI', 'BANK_TRANSFER', 'DIGITAL_WALLET', 'OTHER']);
        setDeliveryMethods(['PICKUP', 'DELIVERY', 'MEETUP']);
      } catch (error) {
        console.error('Error loading methods:', error);
        setPaymentMethods(['CASH', 'UPI', 'BANK_TRANSFER', 'DIGITAL_WALLET', 'OTHER']);
        setDeliveryMethods(['PICKUP', 'DELIVERY', 'MEETUP']);
      }
    };
    loadMethods();
  }, []);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'offerPrice' || name === 'deliveryCost' ? parseFloat(value) || 0 : value,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      await saleRequestService.createSaleRequest(formData);
      setSuccess('Offer sent successfully! The seller will review your offer.');
      setTimeout(() => {
        onSuccess();
        onClose();
      }, 2000);
    } catch (err: any) {
      setError(err.response?.data || 'Failed to send offer. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-md max-h-[90vh] overflow-y-auto">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-lg font-bold text-gray-900 dark:text-white">Make an Offer</h3>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
            disabled={loading}
          >
            <X size={20} />
          </button>
        </div>

        {listing && (
          <div className="mb-4 p-3 bg-gray-50 dark:bg-gray-700 rounded">
            <h4 className="font-semibold text-gray-900 dark:text-white">{listing.title}</h4>
            <p className="text-sm text-gray-600 dark:text-gray-300">Original Price: ₹{listing.price}</p>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Offer Price */}
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-1">
              <IndianRupee size={16} className="inline mr-1" />
              Offer Price
            </label>
            <input
              type="number"
              name="offerPrice"
              value={formData.offerPrice}
              onChange={handleInputChange}
              min="0"
              step="0.01"
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
              required
            />
          </div>

          {/* Payment Method */}
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-1">
              <CreditCard size={16} className="inline mr-1" />
              Payment Method
            </label>
            <select
              name="paymentMethod"
              value={formData.paymentMethod}
              onChange={handleInputChange}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
              required
            >
              {paymentMethods.map(method => (
                <option key={method} value={method} className="dark:bg-gray-700 dark:text-white">
                  {method.replace('_', ' ')}
                </option>
              ))}
            </select>
          </div>

          {/* Delivery Method */}
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-1">
              <Truck size={16} className="inline mr-1" />
              Delivery Method
            </label>
            <select
              name="deliveryMethod"
              value={formData.deliveryMethod}
              onChange={handleInputChange}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
              required
            >
              {deliveryMethods.map(method => (
                <option key={method} value={method} className="dark:bg-gray-700 dark:text-white">
                  {method}
                </option>
              ))}
            </select>
          </div>

          {/* Delivery Cost */}
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-1">
              <IndianRupee size={16} className="inline mr-1" />
              Delivery Cost (₹)
            </label>
            <input
              type="number"
              name="deliveryCost"
              value={formData.deliveryCost}
              onChange={handleInputChange}
              min="0"
              step="0.01"
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
            />
          </div>

          {/* Message */}
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-1">
              <MessageSquare size={16} className="inline mr-1" />
              Message to Seller
            </label>
            <textarea
              name="message"
              value={formData.message}
              onChange={handleInputChange}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
              placeholder="Tell the seller why you want to buy this item..."
              required
            />
          </div>

          {/* Total Amount */}
          <div className="p-3 bg-blue-50 dark:bg-blue-900/20 rounded">
            <p className="text-sm font-medium text-gray-700 dark:text-gray-200">
              Total Amount: ₹{formData.offerPrice + (formData.deliveryCost || 0)}
            </p>
          </div>

          {/* Error and Success Messages */}
          {error && (
            <div className="p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded text-red-700 dark:text-red-400 text-sm">
              {error}
            </div>
          )}

          {success && (
            <div className="p-3 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded text-green-700 dark:text-green-400 text-sm">
              {success}
            </div>
          )}

          {/* Action Buttons */}
          <div className="flex gap-2 pt-2">
            <button
              type="button"
              onClick={onClose}
              disabled={loading}
              className="flex-1 px-4 py-2 text-gray-700 dark:text-gray-200 bg-gray-200 dark:bg-gray-600 rounded-md hover:bg-gray-300 dark:hover:bg-gray-500 disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 px-4 py-2 text-white bg-blue-600 rounded-md hover:bg-blue-700 disabled:opacity-50"
            >
              {loading ? 'Sending Offer...' : 'Send Offer'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default MakeOfferModal; 