import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { formatDateTime } from '../utils/dateUtils';
import axios from 'axios';
import { saleService } from '../services/saleService';
import { saleRequestService } from '../services/saleRequestService';
import MakeOfferModal from '../components/MakeOfferModal';
import { Star } from 'lucide-react';

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
  const [reviews, setReviews] = useState<any[]>([]);
  const [reviewRating, setReviewRating] = useState(0);
  const [reviewComment, setReviewComment] = useState('');
  const [reviewError, setReviewError] = useState<string | null>(null);
  const [reviewSuccess, setReviewSuccess] = useState<string | null>(null);
  const [submittingReview, setSubmittingReview] = useState(false);
  const [showDonationOfferModal, setShowDonationOfferModal] = useState(false);
  const [donationOfferMessage, setDonationOfferMessage] = useState('');
  const [donationOfferSuccess, setDonationOfferSuccess] = useState<string | null>(null);
  const [donationOfferError, setDonationOfferError] = useState<string | null>(null);
  const [submittingDonationOffer, setSubmittingDonationOffer] = useState(false);

  useEffect(() => {
    if (!id) return;
    axios.get(`http://localhost:8080/api/listings/${id}`)
      .then(res => setListing(res.data))
      .catch(() => setListing(null));
    // Fetch reviews
    axios.get(`http://localhost:8080/api/listings/${id}/reviews`)
      .then(res => setReviews(res.data))
      .catch(() => setReviews([]));
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

  const handleReviewSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setReviewError(null);
    setReviewSuccess(null);
    setSubmittingReview(true);
    const token = localStorage.getItem('accessToken');
    if (!token) {
      setReviewError('You must be logged in to submit a review.');
      setSubmittingReview(false);
      return;
    }
    try {
      // Get user info from token or prompt for name
      const reviewerName = localStorage.getItem('userName') || 'Anonymous';
      const reviewerId = localStorage.getItem('userId') || '';
      await axios.post(
        `http://localhost:8080/api/listings/${listing.id}/reviews`,
        {
          reviewerName,
          reviewerId,
          rating: reviewRating,
          comment: reviewComment,
        },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setReviewSuccess('Review submitted!');
      setReviewRating(0);
      setReviewComment('');
      // Refresh reviews
      const res = await axios.get(`http://localhost:8080/api/listings/${listing.id}/reviews`);
      setReviews(res.data);
    } catch (err: any) {
      setReviewError(err?.response?.data || 'Failed to submit review.');
    } finally {
      setSubmittingReview(false);
    }
  };

  const handleDonationOffer = () => {
    const token = localStorage.getItem('accessToken');
    if (!token) {
      navigate('/login');
      return;
    }
    setShowDonationOfferModal(true);
  };

  const handleDonationOfferSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setDonationOfferError(null);
    setDonationOfferSuccess(null);
    setSubmittingDonationOffer(true);
    const token = localStorage.getItem('accessToken');
    if (!token) {
      setDonationOfferError('You must be logged in to request a donation.');
      setSubmittingDonationOffer(false);
      return;
    }
    try {
      await axios.post(
        `http://localhost:8080/api/sale-requests/create`,
        {
          listingId: listing.id,
          offerPrice: 0,
          message: donationOfferMessage,
          paymentMethod: 'NONE',
          deliveryMethod: 'NONE',
          deliveryCost: 0
        },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setDonationOfferSuccess('Request sent!');
      setDonationOfferMessage('');
      setShowDonationOfferModal(false);
    } catch (err: any) {
      setDonationOfferError(err?.response?.data || 'Failed to send request.');
    } finally {
      setSubmittingDonationOffer(false);
    }
  };

  const isDonation = listing && (listing.price === 0 || listing.donation === true || listing.isDonation === true);
  const isAccommodation = listing && (listing.category === 'Accommodation' || listing.category === 'ACCOMMODATION');

  if (!listing) return <div className="p-8 text-center">Loading listing...</div>;

  return (
    <div className="max-w-3xl mx-auto py-8 px-4 pt-16">
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-2xl font-bold mb-2">{listing.title}</h2>
        <div className="text-gray-600 mb-2">Price: ₹{listing.price}</div>
        <div className="text-gray-500 mb-2">{listing.description}</div>
        <div className="text-sm text-gray-400 mb-1">Category: {listing.category}</div>
        <div className="text-sm text-gray-400 mb-1">Condition: {listing.condition}</div>
        {listing.lifeOfItem && (
          <div className="text-sm text-gray-400 mb-1">Life of Item: {listing.lifeOfItem}</div>
        )}
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
              {/* Only show Make Offer for non-donation listings */}
              {!isDonation && !hasPendingRequest && (
                <button
                  className="btn-secondary"
                  onClick={handleMakeOffer}
                >
                  Make Offer
                </button>
              )}
              {/* Only show pending offer message for non-donation listings */}
              {!isDonation && hasPendingRequest && (
                <div className="px-4 py-2 bg-yellow-100 text-yellow-800 rounded border">
                  <span className="text-sm">You have a pending offer for this item</span>
                </div>
              )}
              {/* Show Request Donation for donation listings */}
              {isDonation && (
                <button
                  className="btn-secondary"
                  onClick={handleDonationOffer}
                >
                  Request Donation
                </button>
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
      {!isDonation && (
        <MakeOfferModal
          isOpen={showOfferModal}
          onClose={() => setShowOfferModal(false)}
          listing={listing}
          onSuccess={handleOfferSuccess}
        />
      )}

      {/* Donation Offer Modal */}
      {showDonationOfferModal && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h3 className="text-lg font-bold mb-2">Request Donation</h3>
            <form onSubmit={handleDonationOfferSubmit}>
              <textarea
                className="input-field w-full mb-2"
                rows={4}
                value={donationOfferMessage}
                onChange={e => setDonationOfferMessage(e.target.value)}
                placeholder="Write a message to the owner..."
                required
                disabled={submittingDonationOffer}
              />
              <div className="flex gap-2 justify-end">
                <button className="btn-secondary" type="button" onClick={() => setShowDonationOfferModal(false)} disabled={submittingDonationOffer}>Cancel</button>
                <button className="btn-primary" type="submit" disabled={submittingDonationOffer || !donationOfferMessage.trim()}>
                  {submittingDonationOffer ? 'Sending...' : 'Send Request'}
                </button>
              </div>
              {donationOfferError && <div className="text-red-600 mt-2">{donationOfferError}</div>}
              {donationOfferSuccess && <div className="text-green-600 mt-2">{donationOfferSuccess}</div>}
            </form>
          </div>
        </div>
      )}

      {/* Accommodation Reviews */}
      {isAccommodation && (
        <div className="mt-8">
          <h3 className="text-lg font-bold mb-2 flex items-center gap-2">
            <Star className="h-5 w-5 text-yellow-400" />
            Reviews
            {reviews.length > 0 && (
              <span className="ml-2 text-base font-normal text-gray-500">({reviews.length})</span>
            )}
          </h3>
          {reviews.length > 0 && (
            <div className="mb-4">
              <span className="font-semibold text-yellow-600">
                Average Rating: {(
                  reviews.reduce((sum, r) => sum + (r.rating || 0), 0) / reviews.length
                ).toFixed(1)} / 5
              </span>
            </div>
          )}
          <form onSubmit={handleReviewSubmit} className="mb-6 bg-gray-50 dark:bg-gray-800 p-4 rounded-lg">
            <div className="flex items-center mb-2">
              <span className="mr-2 font-medium">Your Rating:</span>
              {[1,2,3,4,5].map(star => (
                <button
                  type="button"
                  key={star}
                  onClick={() => setReviewRating(star)}
                  className={star <= reviewRating ? 'text-yellow-400' : 'text-gray-300'}
                  aria-label={`Rate ${star}`}
                >
                  <Star className="h-5 w-5" />
                </button>
              ))}
            </div>
            <textarea
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 dark:bg-gray-700 dark:text-white mb-2"
              rows={3}
              placeholder="Write your review..."
              value={reviewComment}
              onChange={e => setReviewComment(e.target.value)}
              required
              disabled={submittingReview}
            />
            <button
              type="submit"
              className="bg-primary-600 text-white px-4 py-2 rounded-lg font-semibold hover:bg-primary-700 transition-colors"
              disabled={submittingReview || reviewRating === 0 || !reviewComment.trim()}
            >
              {submittingReview ? 'Submitting...' : 'Submit Review'}
            </button>
            {reviewError && <div className="text-red-600 mt-2">{reviewError}</div>}
            {reviewSuccess && <div className="text-green-600 mt-2">{reviewSuccess}</div>}
          </form>
          <div className="space-y-4">
            {reviews.map((r, idx) => (
              <div key={idx} className="bg-white dark:bg-gray-900 rounded-lg p-4 shadow border border-gray-100 dark:border-gray-800">
                <div className="flex items-center gap-2 mb-1">
                  <span className="font-semibold text-gray-800 dark:text-gray-100">{r.reviewerName || 'Anonymous'}</span>
                  <span className="flex items-center">
                    {[1,2,3,4,5].map(star => (
                      <Star key={star} className={`h-4 w-4 ${star <= r.rating ? 'text-yellow-400' : 'text-gray-300'}`} />
                    ))}
                  </span>
                  <span className="text-xs text-gray-400 ml-2">{r.createdAt ? new Date(r.createdAt).toLocaleString() : ''}</span>
                </div>
                <div className="text-gray-700 dark:text-gray-200">{r.comment}</div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default ListingDetail; 