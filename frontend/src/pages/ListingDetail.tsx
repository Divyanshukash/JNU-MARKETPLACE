import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { formatDateTime } from '../utils/dateUtils';
import axios from 'axios';
import { saleService } from '../services/saleService';
import { saleRequestService } from '../services/saleRequestService';
import MakeOfferModal from '../components/MakeOfferModal';

const ListingDetail: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [listing, setListing] = useState<any>(null);
  const [showModal, setShowModal] = useState(false);
  const [message, setMessage] = useState('');
  const [sending, setSending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [wishlistStatus, setWishlistStatus] = useState<string | null>(null);
  const [isSold, setIsSold] = useState(false);
  const [hasPendingRequest, setHasPendingRequest] = useState(false);
  const [showOfferModal, setShowOfferModal] = useState(false);

  useEffect(() => {
    if (!id) return;
    axios.get(`http://localhost:8080/api/listings/${id}`)
      .then(res => setListing(res.data))
      .catch(() => setListing(null));
  }, [id]);

  useEffect(() => {
    if (!listing) return;
    
    // Check if item is sold
    const checkSoldStatus = async () => {
      try {
        const soldCheck = await saleService.checkIfSold(listing.id);
        setIsSold(soldCheck.isSold);
      } catch (error) {
        console.error('Error checking sold status:', error);
      }
    };

    // Check if user has pending request
    const checkPendingRequest = async () => {
      try {
        const token = localStorage.getItem('accessToken');
        if (token) {
          const pendingCheck = await saleRequestService.checkPendingRequest(listing.id);
          setHasPendingRequest(pendingCheck.hasPendingRequest);
        }
      } catch (error) {
        console.error('Error checking pending request:', error);
      }
    };

    checkSoldStatus();
    checkPendingRequest();
  }, [listing]);

  const handleMessageSeller = () => {
    const token = localStorage.getItem('accessToken');
    if (!token) {
      navigate('/login');
      return;
    }
    setShowModal(true);
  };

  const handleSend = async () => {
    if (!message.trim()) return;
    setSending(true);
    setError(null);
    setSuccess(null);
    const token = localStorage.getItem('accessToken');
    if (!token) {
      setError('You must be logged in to send a message.');
      setSending(false);
      return;
    }
    try {
      await axios.post(
        `http://localhost:8080/api/messages`,
        {
          recipientId: listing.sellerId,
          listingId: listing.id,
          content: message,
        },
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      setSuccess('Message sent!');
      setMessage('');
      setShowModal(false);
    } catch (err: any) {
      setError('Failed to send message.');
    } finally {
      setSending(false);
    }
  };

  const handleAddToWishlist = async () => {
    const token = localStorage.getItem('accessToken');
    if (!token) {
      setWishlistStatus('You must be logged in to add to wishlist.');
      return;
    }
    try {
      await axios.post(
        `http://localhost:8080/api/users/wishlist/${listing.id}`,
        {},
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setWishlistStatus('Added to wishlist!');
    } catch (err) {
      setWishlistStatus('Failed to add to wishlist.');
    }
  };

  const handleMakeOffer = () => {
    const token = localStorage.getItem('accessToken');
    if (!token) {
      navigate('/login');
      return;
    }
    setShowOfferModal(true);
  };

  const handleOfferSuccess = () => {
    setHasPendingRequest(true);
    setSuccess('Offer sent successfully!');
  };

  if (!listing) return <div className="p-8 text-center">Loading listing...</div>;

  return (
    <div className="max-w-3xl mx-auto py-8 px-4 pt-16">
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-2xl font-bold mb-2">{listing.title}</h2>
        <div className="text-gray-600 mb-2">Price: ₹{listing.price}</div>
        <div className="text-gray-500 mb-2">{listing.description}</div>
        <div className="text-sm text-gray-400 mb-1">Category: {listing.category}</div>
        <div className="text-sm text-gray-400 mb-1">Condition: {listing.condition}</div>
        <div className="text-sm text-gray-400 mb-1">Seller: {listing.sellerName}</div>
        <div className="text-sm text-gray-400 mb-1">Posted: {formatDateTime(listing.createdAt)}</div>
        
        {/* Sold Status */}
        {isSold && (
          <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded">
            <div className="flex items-center gap-2 text-red-700">
              <span className="font-semibold">SOLD</span>
              <span className="text-sm">This item has been sold</span>
            </div>
          </div>
        )}
        {listing.images && listing.images.length > 0 && (
          <div className="flex gap-2 mt-4 mb-4 overflow-x-auto">
            {listing.images.map((imgUrl: string, idx: number) => (
              <img
                key={idx}
                src={`http://localhost:8080${imgUrl}`}
                alt={listing.title}
                className="w-40 h-40 object-cover rounded"
              />
            ))}
          </div>
        )}
        <div className="flex flex-wrap gap-2 mt-4">
          {!isSold && (
            <>
              <button
                className="btn-primary"
                onClick={handleMessageSeller}
              >
                Message Seller
              </button>
              
              {!hasPendingRequest && (
                <button
                  className="btn-secondary"
                  onClick={handleMakeOffer}
                >
                  Make Offer
                </button>
              )}
              
              {hasPendingRequest && (
                <div className="px-4 py-2 bg-yellow-100 text-yellow-800 rounded border">
                  <span className="text-sm">You have a pending offer for this item</span>
                </div>
              )}
            </>
          )}
          
          <button
            className="btn-secondary"
            onClick={handleAddToWishlist}
          >
            Add to Wishlist
          </button>
        </div>
        
        {wishlistStatus && <div className="mt-2 text-sm text-green-600">{wishlistStatus}</div>}
        {success && <div className="mt-2 text-sm text-green-600">{success}</div>}
      </div>
      {/* Messaging Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h3 className="text-lg font-bold mb-2">Message Seller</h3>
            <textarea
              className="input-field w-full mb-2"
              rows={4}
              value={message}
              onChange={e => setMessage(e.target.value)}
              placeholder="Type your message..."
              disabled={sending}
            />
            <div className="flex gap-2 justify-end">
              <button className="btn-secondary" onClick={() => setShowModal(false)} disabled={sending}>Cancel</button>
              <button className="btn-primary" onClick={handleSend} disabled={sending || !message.trim()}>
                {sending ? 'Sending...' : 'Send'}
              </button>
            </div>
            {error && <div className="text-red-600 mt-2">{error}</div>}
            {success && <div className="text-green-600 mt-2">{success}</div>}
          </div>
        </div>
      )}
      
      {/* Make Offer Modal */}
      <MakeOfferModal
        isOpen={showOfferModal}
        onClose={() => setShowOfferModal(false)}
        listing={listing}
        onSuccess={handleOfferSuccess}
      />
    </div>
  );
};

export default ListingDetail; 