import React, { useState, useEffect } from 'react';
import { 
  Clock, 
  CheckCircle, 
  XCircle, 
  AlertCircle, 
  IndianRupee, 
  User, 
  MessageSquare, 
  Truck, 
  CreditCard,
  Calendar,
  Store,
  ShoppingCart
} from 'lucide-react';
import { saleRequestService, SaleRequest } from '../services/saleRequestService';
import { formatDateTime } from '../utils/dateUtils';
import axios from 'axios';

interface UserInfo {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
}

const SaleRequests: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'received' | 'sent' | 'history'>('received');
  const [receivedRequests, setReceivedRequests] = useState<SaleRequest[]>([]);
  const [sentRequests, setSentRequests] = useState<SaleRequest[]>([]);
  const [requestHistory, setRequestHistory] = useState<SaleRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [stats, setStats] = useState<any>(null);
  const [selectedRequest, setSelectedRequest] = useState<SaleRequest | null>(null);
  const [showResponseModal, setShowResponseModal] = useState(false);
  const [responseType, setResponseType] = useState<'accept' | 'reject' | null>(null);
  const [sellerResponse, setSellerResponse] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [userInfo, setUserInfo] = useState<Map<string, UserInfo>>(new Map());

  useEffect(() => {
    loadData();
  }, []);

  const fetchUserInfo = async (userId: string): Promise<UserInfo | null> => {
    try {
      const token = localStorage.getItem('accessToken');
      const response = await axios.get(`http://localhost:8080/api/users/${userId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      return response.data;
    } catch (error) {
      console.error(`Failed to fetch user info for ${userId}:`, error);
      return null;
    }
  };

  const loadUserInfo = async (userIds: string[]) => {
    const uniqueUserIds = Array.from(new Set(userIds));
    const newUserInfo = new Map(userInfo);
    
    for (const userId of uniqueUserIds) {
      if (!newUserInfo.has(userId)) {
        const user = await fetchUserInfo(userId);
        if (user) {
          newUserInfo.set(userId, user);
        }
      }
    }
    
    setUserInfo(newUserInfo);
  };

  const getUserDisplayName = (userId: string): string => {
    const user = userInfo.get(userId);
    if (user) {
      return `${user.firstName} ${user.lastName}`;
    }
    return `User ${userId}`;
  };

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [received, sent, history, statsData] = await Promise.all([
        saleRequestService.getPendingRequestsForSeller(),
        saleRequestService.getPendingRequestsForBuyer(),
        saleRequestService.getUserRequestHistory(),
        saleRequestService.getSellerRequestStats()
      ]);
      
      setReceivedRequests(received);
      setSentRequests(sent);
      setRequestHistory(history);
      setStats(statsData);

      // Extract all unique user IDs from the requests
      const allUserIds: string[] = [];
      [...received, ...sent, ...history].forEach(request => {
        allUserIds.push(request.buyerId, request.sellerId);
      });
      
      // Load user information
      await loadUserInfo(allUserIds);
    } catch (err: any) {
      setError('Failed to load data: ' + (err.response?.data || err.message));
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

  const handleCancel = async (request: SaleRequest) => {
    if (!window.confirm('Are you sure you want to cancel this offer?')) return;
    
    setSubmitting(true);
    try {
      await saleRequestService.cancelSaleRequest(request.id);
      loadData(); // Reload data to update the lists
    } catch (err: any) {
      setError('Failed to cancel offer: ' + (err.response?.data || err.message));
    } finally {
      setSubmitting(false);
    }
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
      loadData(); // Reload data to update the lists
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
      <div className="p-8 text-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
        <p className="mt-2 text-gray-600">Loading sale requests...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-8 text-center">
        <div className="text-red-600 mb-4">{error}</div>
        <button 
          onClick={loadData}
          className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
        >
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto py-8 px-4 pt-16">
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">Sale Requests & Offers</h1>
        <p className="text-gray-600 dark:text-gray-300">Manage your offers and requests</p>
      </div>

      {/* Statistics Cards */}
      {stats && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
          <div className="bg-white dark:bg-gray-800 p-4 rounded-lg shadow">
            <div className="flex items-center">
              <Store className="h-8 w-8 text-blue-600" />
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-300">Received Offers</p>
                <p className="text-2xl font-semibold text-gray-900 dark:text-white">{receivedRequests.length}</p>
              </div>
            </div>
          </div>
          
          <div className="bg-white dark:bg-gray-800 p-4 rounded-lg shadow">
            <div className="flex items-center">
              <ShoppingCart className="h-8 w-8 text-green-600" />
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-300">Sent Offers</p>
                <p className="text-2xl font-semibold text-gray-900 dark:text-white">{sentRequests.length}</p>
              </div>
            </div>
          </div>
          
          <div className="bg-white dark:bg-gray-800 p-4 rounded-lg shadow">
            <div className="flex items-center">
              <Clock className="h-8 w-8 text-purple-600" />
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-300">Total Requests</p>
                <p className="text-2xl font-semibold text-gray-900 dark:text-white">{requestHistory.length}</p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Tabs */}
      <div className="border-b border-gray-200 dark:border-gray-700 mb-6">
        <nav className="-mb-px flex space-x-8">
          <button
            onClick={() => setActiveTab('received')}
            className={`py-2 px-1 border-b-2 font-medium text-sm flex items-center gap-2 ${
              activeTab === 'received'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 hover:border-gray-300 dark:hover:border-gray-600'
            }`}
          >
            <Store size={16} />
            Received Offers ({receivedRequests.length})
          </button>
          <button
            onClick={() => setActiveTab('sent')}
            className={`py-2 px-1 border-b-2 font-medium text-sm flex items-center gap-2 ${
              activeTab === 'sent'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 hover:border-gray-300 dark:hover:border-gray-600'
            }`}
          >
            <ShoppingCart size={16} />
            Sent Offers ({sentRequests.length})
          </button>
          <button
            onClick={() => setActiveTab('history')}
            className={`py-2 px-1 border-b-2 font-medium text-sm flex items-center gap-2 ${
              activeTab === 'history'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 hover:border-gray-300 dark:hover:border-gray-600'
            }`}
          >
            <Clock size={16} />
            Request History ({requestHistory.length})
          </button>
        </nav>
      </div>

      {/* Tab Content */}
      <div className="space-y-4">
        {activeTab === 'received' && (
          <div>
            <div className="mb-4">
              <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">Offers Received (As Seller)</h2>
              <p className="text-sm text-gray-600 dark:text-gray-300">These are offers made by buyers for your listings</p>
            </div>
            
            {receivedRequests.length === 0 ? (
              <div className="text-center py-8 text-gray-500 dark:text-gray-400">
                <Store size={48} className="mx-auto mb-4 text-gray-300 dark:text-gray-600" />
                <p>No offers received yet</p>
                <p className="text-sm mt-2">When buyers make offers on your listings, they'll appear here</p>
              </div>
            ) : (
              <div className="space-y-4">
                {receivedRequests.map((request) => (
                  <div key={request.id} className="bg-white dark:bg-gray-800 border dark:border-gray-700 rounded-lg p-4 shadow-sm">
                    <div className="flex justify-between items-start mb-3">
                      <div>
                        <h4 className="font-semibold text-gray-900 dark:text-white">Offer for Your Item</h4>
                        <p className="text-sm text-gray-600 dark:text-gray-300">Request ID: {request.id}</p>
                      </div>
                      <span className={`px-2 py-1 rounded-full text-xs font-medium flex items-center gap-1 ${getStatusColor(request.status)}`}>
                        {getStatusIcon(request.status)}
                        {request.status}
                      </span>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <div className="flex items-center gap-2 mb-2">
                          <IndianRupee size={16} className="text-green-600" />
                          <span className="font-semibold text-gray-900 dark:text-white">₹{request.offerPrice}</span>
                          <span className="text-sm text-gray-500 dark:text-gray-400">(Original: ₹{request.originalPrice})</span>
                        </div>
                        
                        <div className="flex items-center gap-2 mb-2">
                          <CreditCard size={16} className="text-blue-600" />
                          <span className="text-sm text-gray-700 dark:text-gray-200">{request.paymentMethod.replace('_', ' ')}</span>
                        </div>
                        
                        <div className="flex items-center gap-2 mb-2">
                          <Truck size={16} className="text-purple-600" />
                          <span className="text-sm text-gray-700 dark:text-gray-200">{request.deliveryMethod}</span>
                          {request.deliveryCost > 0 && (
                            <span className="text-sm text-gray-500 dark:text-gray-400">(+₹{request.deliveryCost})</span>
                          )}
                        </div>
                      </div>

                      <div>
                        <div className="flex items-center gap-2 mb-2">
                          <User size={16} className="text-gray-600 dark:text-gray-400" />
                          <span className="text-sm text-gray-700 dark:text-gray-200">Buyer: {getUserDisplayName(request.buyerId)}</span>
                        </div>
                        
                        <div className="flex items-center gap-2 mb-2">
                          <Calendar size={16} className="text-gray-600 dark:text-gray-400" />
                          <span className="text-sm text-gray-700 dark:text-gray-200">{formatDateTime(request.createdAt)}</span>
                        </div>
                        
                        <div className="flex items-center gap-2">
                          <MessageSquare size={16} className="text-gray-600 dark:text-gray-400" />
                          <span className="text-sm text-gray-700 dark:text-gray-200">Message: {request.message}</span>
                        </div>
                      </div>
                    </div>

                    {request.status === 'PENDING' && (
                      <div className="flex gap-2 pt-3 border-t border-gray-200 dark:border-gray-700">
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
          </div>
        )}

        {activeTab === 'sent' && (
          <div>
            <div className="mb-4">
              <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">Offers Sent (As Buyer)</h2>
              <p className="text-sm text-gray-600 dark:text-gray-300">These are offers you've made to other sellers</p>
            </div>
            
            {sentRequests.length === 0 ? (
              <div className="text-center py-8 text-gray-500 dark:text-gray-400">
                <ShoppingCart size={48} className="mx-auto mb-4 text-gray-300 dark:text-gray-600" />
                <p>No offers sent yet</p>
                <p className="text-sm mt-2">When you make offers on listings, they'll appear here</p>
              </div>
            ) : (
              <div className="space-y-4">
                {sentRequests.map((request) => (
                  <div key={request.id} className="bg-white dark:bg-gray-800 border dark:border-gray-700 rounded-lg p-4 shadow-sm">
                    <div className="flex justify-between items-start mb-3">
                      <div>
                        <h4 className="font-semibold text-gray-900 dark:text-white">Your Offer</h4>
                        <p className="text-sm text-gray-600 dark:text-gray-300">Request ID: {request.id}</p>
                      </div>
                      <span className={`px-2 py-1 rounded-full text-xs font-medium flex items-center gap-1 ${getStatusColor(request.status)}`}>
                        {getStatusIcon(request.status)}
                        {request.status}
                      </span>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <div className="flex items-center gap-2 mb-2">
                          <IndianRupee size={16} className="text-green-600" />
                          <span className="font-semibold text-gray-900 dark:text-white">₹{request.offerPrice}</span>
                          <span className="text-sm text-gray-500 dark:text-gray-400">(Original: ₹{request.originalPrice})</span>
                        </div>
                        
                        <div className="flex items-center gap-2 mb-2">
                          <CreditCard size={16} className="text-blue-600" />
                          <span className="text-sm text-gray-700 dark:text-gray-200">{request.paymentMethod.replace('_', ' ')}</span>
                        </div>
                        
                        <div className="flex items-center gap-2 mb-2">
                          <Truck size={16} className="text-purple-600" />
                          <span className="text-sm text-gray-700 dark:text-gray-200">{request.deliveryMethod}</span>
                          {request.deliveryCost > 0 && (
                            <span className="text-sm text-gray-500 dark:text-gray-400">(+₹{request.deliveryCost})</span>
                          )}
                        </div>
                      </div>

                      <div>
                        <div className="flex items-center gap-2 mb-2">
                          <User size={16} className="text-gray-600 dark:text-gray-400" />
                          <span className="text-sm text-gray-700 dark:text-gray-200">Buyer: {getUserDisplayName(request.buyerId)}</span>
                        </div>
                        
                        <div className="flex items-center gap-2 mb-2">
                          <Calendar size={16} className="text-gray-600 dark:text-gray-400" />
                          <span className="text-sm text-gray-700 dark:text-gray-200">{formatDateTime(request.createdAt)}</span>
                        </div>
                        
                        <div className="flex items-center gap-2">
                          <MessageSquare size={16} className="text-gray-600 dark:text-gray-400" />
                          <span className="text-sm text-gray-700 dark:text-gray-200">Your Message: {request.message}</span>
                        </div>
                      </div>
                    </div>

                    {request.status === 'PENDING' && (
                      <div className="flex gap-2 pt-3 border-t border-gray-200 dark:border-gray-700">
                        <button
                          onClick={() => handleCancel(request)}
                          className="flex-1 px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 flex items-center justify-center gap-2"
                          disabled={submitting}
                        >
                          <XCircle size={16} />
                          Cancel Offer
                        </button>
                      </div>
                    )}

                    {request.sellerResponse && (
                      <div className="mt-3 pt-3 border-t border-gray-200 dark:border-gray-700">
                        <div className="bg-blue-50 dark:bg-blue-900/20 p-3 rounded">
                          <p className="text-sm font-medium text-blue-900 dark:text-blue-200 mb-1">Seller's Response:</p>
                          <p className="text-sm text-blue-800 dark:text-blue-300">{request.sellerResponse}</p>
                        </div>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {activeTab === 'history' && (
          <div>
            <div className="mb-4">
              <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">Request History</h2>
              <p className="text-sm text-gray-600 dark:text-gray-300">All your completed, rejected, and cancelled requests</p>
            </div>
            
            {requestHistory.length === 0 ? (
              <div className="text-center py-8 text-gray-500 dark:text-gray-400">
                <Clock size={48} className="mx-auto mb-4 text-gray-300 dark:text-gray-600" />
                <p>No request history</p>
              </div>
            ) : (
              <div className="space-y-4">
                {requestHistory.map((request) => (
                  <div key={request.id} className="bg-white dark:bg-gray-800 border dark:border-gray-700 rounded-lg p-4 shadow-sm">
                    <div className="flex justify-between items-start mb-3">
                      <div>
                        <h4 className="font-semibold text-gray-900 dark:text-white">Request #{request.id}</h4>
                        <p className="text-sm text-gray-600 dark:text-gray-300">{formatDateTime(request.createdAt)}</p>
                      </div>
                      <span className={`px-2 py-1 rounded-full text-xs font-medium flex items-center gap-1 ${getStatusColor(request.status)}`}>
                        {getStatusIcon(request.status)}
                        {request.status}
                      </span>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <div className="flex items-center gap-2 mb-2">
                          <IndianRupee size={16} className="text-green-600" />
                          <span className="font-semibold text-gray-900 dark:text-white">₹{request.offerPrice}</span>
                          <span className="text-sm text-gray-500 dark:text-gray-400">(Original: ₹{request.originalPrice})</span>
                        </div>
                        
                        <div className="flex items-center gap-2 mb-2">
                          <CreditCard size={16} className="text-blue-600" />
                          <span className="text-sm text-gray-700 dark:text-gray-200">{request.paymentMethod.replace('_', ' ')}</span>
                        </div>
                        
                        <div className="flex items-center gap-2 mb-2">
                          <Truck size={16} className="text-purple-600" />
                          <span className="text-sm text-gray-700 dark:text-gray-200">{request.deliveryMethod}</span>
                        </div>
                      </div>

                      <div>
                        <div className="flex items-center gap-2 mb-2">
                          <User size={16} className="text-gray-600 dark:text-gray-400" />
                          <span className="text-sm text-gray-700 dark:text-gray-200">Buyer: {getUserDisplayName(request.buyerId)}</span>
                        </div>
                        
                        <div className="flex items-center gap-2 mb-2">
                          <MessageSquare size={16} className="text-gray-600 dark:text-gray-400" />
                          <span className="text-sm text-gray-700 dark:text-gray-200">Message: {request.message}</span>
                        </div>
                        
                        {request.sellerResponse && (
                          <div className="flex items-center gap-2">
                            <MessageSquare size={16} className="text-gray-600 dark:text-gray-400" />
                            <span className="text-sm text-gray-700 dark:text-gray-200">Response: {request.sellerResponse}</span>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>

      {/* Response Modal */}
      {showResponseModal && selectedRequest && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-md">
            <h3 className="text-lg font-bold mb-4 text-gray-900 dark:text-white">
              {responseType === 'accept' ? 'Accept Offer' : 'Reject Offer'}
            </h3>
            
            <div className="mb-4 p-3 bg-gray-50 dark:bg-gray-700 rounded">
              <p className="text-sm text-gray-600 dark:text-gray-300">
                Offer Price: ₹{selectedRequest.offerPrice}
              </p>
              <p className="text-sm text-gray-600 dark:text-gray-300">
                Buyer Message: {selectedRequest.message}
              </p>
            </div>

            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">
                Your Response
              </label>
              <textarea
                value={sellerResponse}
                onChange={(e) => setSellerResponse(e.target.value)}
                rows={3}
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                placeholder={responseType === 'accept' ? 'Add any additional details for the buyer...' : 'Explain why you are rejecting this offer...'}
                required
              />
            </div>

            <div className="flex gap-2">
              <button
                onClick={() => setShowResponseModal(false)}
                disabled={submitting}
                className="flex-1 px-4 py-2 text-gray-700 dark:text-gray-200 bg-gray-200 dark:bg-gray-600 rounded-md hover:bg-gray-300 dark:hover:bg-gray-500 disabled:opacity-50"
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

export default SaleRequests; 