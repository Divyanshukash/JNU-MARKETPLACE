import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { formatDateTime } from '../utils/dateUtils';
import axios from 'axios';
import { saleService } from '../services/saleService';

// Create authenticated axios instance
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add request interceptor to include auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Map display names to enum keys for category and condition
const categoryDisplayToEnum: Record<string, string> = {
  'Books': 'BOOKS',
  'Electronics': 'ELECTRONICS',
  'Furniture': 'FURNITURE',
  'Clothing': 'CLOTHING',
  'Sports & Fitness': 'SPORTS',
  'Musical Instruments': 'MUSICAL_INSTRUMENTS',
  'Vehicles': 'VEHICLES',
  'Services': 'SERVICES',
  'Food & Beverages': 'FOOD',
  'Art & Collectibles': 'ART',
  'Beauty & Personal Care': 'BEAUTY',
  'Home & Garden': 'HOME',
  'Toys & Games': 'TOYS',
  'Health & Wellness': 'HEALTH',
  'Education & Training': 'EDUCATION',
  'Other': 'OTHER',
};

// Map condition display names to enum values
const conditionDisplayToEnum: Record<string, string> = {
  'New': 'NEW',
  'Like New': 'LIKE_NEW',
  'Excellent': 'EXCELLENT',
  'Good': 'GOOD',
  'Fair': 'FAIR',
  'Poor': 'POOR',
};

// Define condition options as an array of { value, label } objects for consistency
const conditionOptions = [
  { value: 'NEW', label: 'New' },
  { value: 'LIKE_NEW', label: 'Like New' },
  { value: 'EXCELLENT', label: 'Excellent' },
  { value: 'GOOD', label: 'Good' },
  { value: 'FAIR', label: 'Fair' },
  { value: 'POOR', label: 'Poor' },
];

const Search: React.FC = () => {
  const [categories, setCategories] = useState<string[]>([]);
  const [conditions, setConditions] = useState<string[]>([]);
  const [listings, setListings] = useState<any[]>([]);
  const [filters, setFilters] = useState({ category: '', minPrice: '', maxPrice: '', condition: '' });
  const [loading, setLoading] = useState(false);
  const [soldItems, setSoldItems] = useState<Set<string>>(new Set());
  const navigate = useNavigate();
  const { user } = useAuth();
  const location = useLocation();

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const query = params.get('query');
    if (query && query.trim()) {
      fetchQueryListings(query.trim());
    } else {
      api.get('/listings/categories')
        .then(res => setCategories(res.data))
        .catch(() => setCategories([]));
      api.get('/listings/conditions')
        .then(res => setConditions(res.data))
        .catch(() => setConditions([]));
      fetchAllListings();
    }
    // eslint-disable-next-line
  }, [location.search]);

  const fetchAllListings = () => {
    setLoading(true);
    api.get('/listings', { params: { page: 0, size: 40 } })
      .then(res => {
        const listingsData = res.data.content || [];
        setListings(listingsData);
        // Check sold status for all listings
        if (listingsData.length > 0) {
          checkSoldStatus(listingsData.map((l: any) => l.id));
        }
        setLoading(false);
      })
      .catch(() => {
        setListings([]);
        setLoading(false);
      });
  };

  const fetchFilteredListings = (filterParams: typeof filters) => {
    setLoading(true);
    
    // Build the search request object - match the working approach from Home page
    const searchRequest: any = {};
    
    if (filterParams.category) {
      searchRequest.category = filterParams.category.toUpperCase();
    }
    
    if (filterParams.condition) {
      // Send condition as a string that can be deserialized to enum
      searchRequest.condition = filterParams.condition;
    }
    
    if (filterParams.minPrice) {
      searchRequest.minPrice = parseFloat(filterParams.minPrice);
    }
    
    if (filterParams.maxPrice) {
      searchRequest.maxPrice = parseFloat(filterParams.maxPrice);
    }

    console.log('Sending search request:', searchRequest);

    api.post('/listings/search', searchRequest, {
      params: { page: 0, size: 40 },
    })
      .then(res => {
        console.log('Search response:', res.data);
        const listingsData = res.data.content || [];
        setListings(listingsData);
        // Check sold status for filtered listings
        if (listingsData.length > 0) {
          checkSoldStatus(listingsData.map((l: any) => l.id));
        }
        setLoading(false);
      })
      .catch((error) => {
        console.error('Search error:', error);
        setListings([]);
        setLoading(false);
      });
  };

  const fetchQueryListings = (query: string) => {
    setLoading(true);
    api.post('/listings/search', {
      keyword: query,
    }, {
      params: { page: 0, size: 40 },
    })
      .then(res => {
        const listingsData = res.data.content || [];
        setListings(listingsData);
        // Check sold status for search results
        if (listingsData.length > 0) {
          checkSoldStatus(listingsData.map((l: any) => l.id));
        }
        setLoading(false);
      })
      .catch(() => {
        setListings([]);
        setLoading(false);
      });
  };

  const handleFilterChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    const newFilters = { ...filters, [name]: value };
    setFilters(newFilters);
    
    console.log('Filter changed:', { name, value, newFilters });
    
    // If all filters are empty, fetch all listings
    if (!newFilters.category && !newFilters.minPrice && !newFilters.maxPrice && !newFilters.condition) {
      console.log('No filters applied, fetching all listings');
      fetchAllListings();
    } else {
      console.log('Filters applied, fetching filtered listings');
      fetchFilteredListings(newFilters);
    }
  };

  const clearFilters = () => {
    setFilters({ category: '', minPrice: '', maxPrice: '', condition: '' });
    fetchAllListings();
  };

  // Check sold status for listings
  const checkSoldStatus = async (listingIds: string[]) => {
    const soldSet = new Set<string>();
    
    for (const id of listingIds) {
      try {
        const soldCheck = await saleService.checkIfSold(id);
        if (soldCheck.isSold) {
          soldSet.add(id);
        }
      } catch (error) {
        console.error('Error checking sold status for listing:', id, error);
      }
    }
    
    setSoldItems(soldSet);
  };

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 py-8 px-4 pt-16">
      <div className="max-w-7xl mx-auto">
        <h2 className="text-3xl font-bold text-gray-900 dark:text-white mb-8 text-center">Browse All Items</h2>
        <div className="flex gap-8">
          {/* Sidebar Filters */}
          <aside className="w-full md:w-64 bg-white dark:bg-gray-800 rounded-lg shadow p-6 mb-8 md:mb-0">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-xl font-bold text-gray-900 dark:text-white">Filters</h3>
              <button
                onClick={clearFilters}
                className="text-sm text-blue-600 dark:text-blue-400 hover:underline"
              >
                Clear All
              </button>
            </div>
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Category</label>
              <select name="category" value={filters.category} onChange={handleFilterChange} className="input-field">
                <option value="">All Categories</option>
                {categories.map(cat => (
                  <option key={cat} value={cat}>{cat}</option>
                ))}
              </select>
            </div>
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Price Range</label>
              <div className="flex gap-2">
                <input
                  type="number"
                  name="minPrice"
                  value={filters.minPrice}
                  onChange={handleFilterChange}
                  placeholder="Min"
                  className="input-field w-1/2"
                  min="0"
                />
                <input
                  type="number"
                  name="maxPrice"
                  value={filters.maxPrice}
                  onChange={handleFilterChange}
                  placeholder="Max"
                  className="input-field w-1/2"
                  min="0"
                />
              </div>
            </div>
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Condition</label>
              <select name="condition" value={filters.condition} onChange={handleFilterChange} className="input-field">
                <option value="">All Conditions</option>
                {conditionOptions.map(opt => (
                  <option key={opt.value} value={opt.value}>{opt.label}</option>
                ))}
              </select>
            </div>
          </aside>
          {/* Listings Grid */}
          <main className="flex-1">
            {loading ? (
              <div className="flex justify-center items-center h-64">
                <div className="text-gray-600 dark:text-gray-400">Loading...</div>
              </div>
            ) : listings.length === 0 ? (
              <div className="text-center py-12">
                <div className="text-gray-500 dark:text-gray-400 text-lg mb-2">No items found</div>
                <div className="text-gray-400 dark:text-gray-500 text-sm">Try adjusting your filters or search terms</div>
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-8">
                {listings
                  .filter(listing => !user || listing.sellerId !== user.id)
                  .map(listing => {
                    const isSold = soldItems.has(listing.id);
                    return (
                      <div key={listing.id} className={`border border-gray-200 dark:border-gray-700 rounded-lg p-4 flex flex-col bg-white dark:bg-gray-800 shadow hover:shadow-lg transition-shadow relative ${isSold ? 'opacity-75' : ''}`}>
                        {/* Sold Badge */}
                        {isSold && (
                          <div className="absolute top-2 right-2 bg-red-500 text-white px-2 py-1 rounded text-xs font-semibold z-10">
                            SOLD
                          </div>
                        )}
                        
                        {listing.images && listing.images.length > 0 ? (
                          <img
                            src={`http://localhost:8080${listing.images[0]}`}
                            alt={listing.title}
                            className="w-full h-48 object-cover rounded mb-2"
                          />
                        ) : (
                          <div className="w-full h-48 bg-gray-200 dark:bg-gray-700 flex items-center justify-center rounded mb-2 text-gray-400 dark:text-gray-500">
                            No Image
                          </div>
                        )}
                        <div className="font-bold text-primary-700 dark:text-primary-200 text-lg mb-1 truncate">{listing.title}</div>
                        <div className="text-gray-600 dark:text-gray-300 mb-1">Price: ₹{listing.price}</div>
                        <div className="text-gray-500 dark:text-gray-400 text-xs mb-2 truncate">{listing.description}</div>
                        <div className="text-xs text-gray-400 dark:text-gray-500 mb-1">Category: {listing.category}</div>
                        <div className="text-xs text-gray-400 dark:text-gray-500 mb-1">Condition: {listing.condition}</div>
                        <div className="text-xs text-gray-400 dark:text-gray-500">Posted: {formatDateTime(listing.createdAt)}</div>
                        <button
                          className={`btn-primary mt-2 text-center ${isSold ? 'opacity-50 cursor-not-allowed' : ''}`}
                          onClick={() => !isSold && navigate(`/listings/${listing.id}`)}
                          disabled={isSold}
                        >
                          {isSold ? 'Sold' : 'View Details'}
                        </button>
                      </div>
                    );
                  })}
              </div>
            )}
          </main>
        </div>
      </div>
    </div>
  );
};

export default Search; 