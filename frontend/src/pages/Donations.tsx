import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const Donations: React.FC = () => {
  const [donations, setDonations] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    setLoading(true);
    axios.get(`${process.env.REACT_APP_API_URL}/listings/donations?page=0&size=20')
      .then(res => {
        setDonations(res.data.content || []);
        setLoading(false);
      })
      .catch(() => {
        setError('Failed to load donation listings.');
        setLoading(false);
      });
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-gray-800 pt-16">
      <div className="max-w-4xl mx-auto py-8 px-4">
        <h1 className="text-2xl font-bold mb-6 text-primary-700 dark:text-primary-300">Items for Donation</h1>
        {loading && <div>Loading...</div>}
        {error && <div className="text-red-600 dark:text-red-400">{error}</div>}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {donations.map(listing => (
            <div key={listing.id} className="bg-white dark:bg-gray-800 rounded-lg shadow p-4 flex flex-col hover:shadow-lg transition">
              {listing.images && listing.images.length > 0 && (
                <img src={`${process.env.REACT_APP_BASE_URL}${listing.images[0]}`} alt={listing.title} className="w-full h-40 object-cover rounded mb-3" />
              )}
              <div className="flex items-center mb-2">
                <span className="bg-green-100 text-green-800 text-xs font-semibold px-2.5 py-0.5 rounded mr-2">Free/Donation</span>
                <span className="text-lg font-semibold text-gray-900 dark:text-primary-200">{listing.title}</span>
              </div>
              <p className="text-gray-700 dark:text-gray-300 text-sm mb-2">{listing.description}</p>
              <div className="text-xs text-gray-500 dark:text-gray-400 mb-4">Category: {listing.category}</div>
              <button
                className="mt-auto bg-primary-600 hover:bg-primary-700 text-white px-4 py-2 rounded-lg font-semibold transition-colors"
                onClick={() => navigate(`/listings/${listing.id}`)}
              >
                View Details & Contact Owner
              </button>
            </div>
          ))}
        </div>
        {!loading && donations.length === 0 && <div className="text-gray-600 dark:text-gray-400 mt-8">No donation items available right now.</div>}
      </div>
    </div>
  );
};

export default Donations; 
