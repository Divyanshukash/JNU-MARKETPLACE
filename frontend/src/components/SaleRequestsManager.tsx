import React, { useState, useEffect } from 'react';
import { 
  CheckCircle, 
  XCircle, 
  Clock, 
  IndianRupee, 
  User, 
  MessageSquare, 
  Truck, 
  CreditCard,
  AlertCircle,
  Calendar
} from 'lucide-react';
import { saleRequestService, SaleRequest } from '../services/saleRequestService';
import { formatDateTime } from '../utils/dateUtils';

interface SaleRequestsManagerProps {
  onRequestUpdate: () => void;
}

const SaleRequestsManager: React.FC<SaleRequestsManagerProps> = ({ onRequestUpdate }) => {
  const [requests, setRequests] = useState<SaleRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedRequest, setSelectedRequest] = useState<SaleRequest | null>(null);
  const [showResponseModal, setShowResponseModal] = useState(false);
  const [responseType, setResponseType] = useState<'accept' | 'reject' | null>(null);
  const [sellerResponse, setSellerResponse] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    loadRequests();
  }, []);

  const loadRequests = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await saleRequestService.getPendingRequestsForSeller();
      setRequests(data);
    } catch (err: any) {
      setError('Failed to load requests: ' + (err.response?.data || err.message));
    } finally {
      setLoading(false);
    }
  };

  const handleAccept = (request: SaleRequest) => {
    setSelectedRequest(request);
    setResponseType('accept');
    setSellerResponse('');
    setShowResponseModal(true);
  };

  const handleReject = (request: SaleRequest) => {
    setSelectedRequest(request);
    setResponseType('reject');
    setSellerResponse('');
    setShowResponseModal(true);
  };

  const handleSubmitResponse = async () => {
    if (!selectedRequest || !responseType || !sellerResponse.trim()) return;

    setSubmitting(true);
    try {
      if (responseType === 'accept') {
        await saleRequestService.acceptSaleRequest(selectedRequest.id, sellerResponse);
      } else {
        await saleRequestService.rejectSaleRequest(selectedRequest.id, sellerResponse);
      }
      
      setShowResponseModal(false);
      setSelectedRequest(null);
      setResponseType(null);
      setSellerResponse('');
      loadRequests();
      onRequestUpdate();
    } catch (err: any) {
      setError('Failed to respond: ' + (err.response?.data || err.message));
    } finally {
      setSubmitting(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING': return 'text-yellow-600 bg-yellow-100';
      case 'ACCEPTED': return 'text-green-600 bg-green-100';
      case 'REJECTED': return 'text-red-600 bg-red-100';
      case 'CANCELLED': return 'text-gray-600 bg-gray-100';
      case 'EXPIRED': return 'text-gray-600 bg-gray-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'PENDING': return <Clock size={16} />;
      case 'ACCEPTED': return <CheckCircle size={16} />;
      case 'REJECTED': return <XCircle size={16} />;
      case 'CANCELLED': return <XCircle size={16} />;
      case 'EXPIRED': return <AlertCircle size={16} />;
      default: return <Clock size={16} />;
    }
  };

  if (loading) {
    return (
      <div className="p-6 text-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
        <p className="mt-2 text-gray-600">Loading requests...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-6 text-center">
        <div className="text-red-600 mb-2">{error}</div>
        <button 
          onClick={loadRequests}
          className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
        >
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <h3 className="text-lg font-semibold">Pending Sale Requests</h3>
        <span className="text-sm text-gray-600">{requests.length} requests</span>
      </div>

      {requests.length === 0 ? (
        <div className="text-center py-8 text-gray-500">
          <MessageSquare size={48} className="mx-auto mb-4 text-gray-300" />
          <p>No pending requests</p>
        </div>
      ) : (
        <div className="space-y-4">
          {requests.map((request) => (
            <div key={request.id} className="bg-white border rounded-lg p-4 shadow-sm">
              <div className="flex justify-between items-start mb-3">
                <div>
                  <h4 className="font-semibold text-gray-900">Offer for Item</h4>
                  <p className="text-sm text-gray-600">Request ID: {request.id}</p>
                </div>
                <span className={`px-2 py-1 rounded-full text-xs font-medium flex items-center gap-1 ${getStatusColor(request.status)}`}>
                  {getStatusIcon(request.status)}
                  {request.status}
                </span>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                <div>
                  <div className="flex items-center gap-2 mb-2">
                    <IndianRupee size={16} className="text-green-600" />
                    <span className="font-semibold">₹{request.offerPrice}</span>
                    <span className="text-sm text-gray-500">(Original: ₹{request.originalPrice})</span>
                  </div>
                  
                  <div className="flex items-center gap-2 mb-2">
                    <CreditCard size={16} className="text-blue-600" />
                    <span className="text-sm">{request.paymentMethod.replace('_', ' ')}</span>
                  </div>
                  
                  <div className="flex items-center gap-2 mb-2">
                    <Truck size={16} className="text-purple-600" />
                    <span className="text-sm">{request.deliveryMethod}</span>
                    {request.deliveryCost > 0 && (
                      <span className="text-sm text-gray-500">(+₹{request.deliveryCost})</span>
                    )}
                  </div>
                </div>

                <div>
                  <div className="flex items-center gap-2 mb-2">
                    <User size={16} className="text-gray-600" />
                    <span className="text-sm">Buyer ID: {request.buyerId}</span>
                  </div>
                  
                  <div className="flex items-center gap-2 mb-2">
                    <Calendar size={16} className="text-gray-600" />
                    <span className="text-sm">{formatDateTime(request.createdAt)}</span>
                  </div>
                  
                  <div className="flex items-center gap-2">
                    <MessageSquare size={16} className="text-gray-600" />
                    <span className="text-sm">Message: {request.message}</span>
                  </div>
                </div>
              </div>

              {request.status === 'PENDING' && (
                <div className="flex gap-2 pt-3 border-t">
                  <button
                    onClick={() => handleAccept(request)}
                    className="flex-1 px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 flex items-center justify-center gap-2"
                  >
                    <CheckCircle size={16} />
                    Accept Offer
                  </button>
                  <button
                    onClick={() => handleReject(request)}
                    className="flex-1 px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 flex items-center justify-center gap-2"
                  >
                    <XCircle size={16} />
                    Reject Offer
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Response Modal */}
      {showResponseModal && selectedRequest && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h3 className="text-lg font-bold mb-4">
              {responseType === 'accept' ? 'Accept Offer' : 'Reject Offer'}
            </h3>
            
            <div className="mb-4 p-3 bg-gray-50 rounded">
              <p className="text-sm text-gray-600">
                Offer Price: ₹{selectedRequest.offerPrice}
              </p>
              <p className="text-sm text-gray-600">
                Buyer Message: {selectedRequest.message}
              </p>
            </div>

            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Your Response
              </label>
              <textarea
                value={sellerResponse}
                onChange={(e) => setSellerResponse(e.target.value)}
                rows={3}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder={responseType === 'accept' ? 'Add any additional details for the buyer...' : 'Explain why you are rejecting this offer...'}
                required
              />
            </div>

            <div className="flex gap-2">
              <button
                onClick={() => setShowResponseModal(false)}
                disabled={submitting}
                className="flex-1 px-4 py-2 text-gray-700 bg-gray-200 rounded-md hover:bg-gray-300 disabled:opacity-50"
              >
                Cancel
              </button>
              <button
                onClick={handleSubmitResponse}
                disabled={submitting || !sellerResponse.trim()}
                className={`flex-1 px-4 py-2 text-white rounded-md disabled:opacity-50 ${
                  responseType === 'accept' 
                    ? 'bg-green-600 hover:bg-green-700' 
                    : 'bg-red-600 hover:bg-red-700'
                }`}
              >
                {submitting ? 'Processing...' : (responseType === 'accept' ? 'Accept Offer' : 'Reject Offer')}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default SaleRequestsManager; 