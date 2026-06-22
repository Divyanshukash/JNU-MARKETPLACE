import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { saleService } from '../services/saleService';
import { 
  Heart, 
  Search, 
  Filter, 
  Grid, 
  List, 
  Eye, 
  MessageCircle, 
  Trash2, 
  Star,
  TrendingUp,
  DollarSign,
  Package,
  Calendar,
  MapPin,
  User,
  ShoppingCart,
  ArrowRight,
  Plus,
  Minus
} from 'lucide-react';

interface Listing {
  id: string;
  title: string;
  price: number;
  description: string;
  images: string[];
  category: string;
  condition: string;
  sellerId?: string;
  sellerName?: string;
  createdAt?: string;
  views?: number;
  location?: string;
}

const Wishlist: React.FC = () => {
  const [wishlist, setWishlist] = useState<Listing[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [showFilters, setShowFilters] = useState(false);
  const [categoryFilter, setCategoryFilter] = useState<string>('all');
  const [priceRange, setPriceRange] = useState<string>('all');
  const [sortBy, setSortBy] = useState<string>('name');
  const [soldItems, setSoldItems] = useState<Set<string>>(new Set());
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    if (!token) {
      setError('You must be logged in to view your wishlist.');
      setLoading(false);
      return;
    }
    axios.get(`${process.env.REACT_APP_API_URL}/users/wishlist`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then(async res => {
        const ids: string[] = res.data;
        if (ids.length === 0) {
          setWishlist([]);
          setLoading(false);
          return;
        }
        // Fetch all listings in parallel
        const listings = await Promise.all(
          ids.map(id =>
            axios.get(`${process.env.REACT_APP_API_URL}/listings/${id}`)
              .then(r => r.data)
              .catch(() => null)
          )
        );
        const validListings = listings.filter(Boolean);
        setWishlist(validListings);
        
        // Check sold status for all listings
        if (validListings.length > 0) {
          checkSoldStatus(validListings.map((l: any) => l.id));
        }
        
        setLoading(false);
      })
      .catch(() => {
        setError('Failed to load wishlist.');
        setLoading(false);
      });
  }, []);

  const removeFromWishlist = async (listingId: string) => {
    try {
      const token = localStorage.getItem('accessToken');
      await axios.delete(`http://jnu-marketplace-final.onrender.com/api/users/wishlist/${listingId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setWishlist(prev => prev.filter(item => item.id !== listingId));
    } catch (error) {
      console.error('Failed to remove from wishlist:', error);
    }
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

  const filteredAndSortedWishlist = wishlist
    .filter(item => {
      const matchesSearch = item.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          item.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          item.category.toLowerCase().includes(searchQuery.toLowerCase());
      
      const matchesCategory = categoryFilter === 'all' || item.category === categoryFilter;
      
      const matchesPrice = priceRange === 'all' || (() => {
        const price = item.price;
        switch (priceRange) {
          case '0-500': return price >= 0 && price <= 500;
          case '500-1000': return price > 500 && price <= 1000;
          case '1000-5000': return price > 1000 && price <= 5000;
          case '5000+': return price > 5000;
          default: return true;
        }
      })();
      
      return matchesSearch && matchesCategory && matchesPrice;
    })
    .sort((a, b) => {
      switch (sortBy) {
        case 'price-low':
          return a.price - b.price;
        case 'price-high':
          return b.price - a.price;
        case 'name':
          return a.title.localeCompare(b.title);
        default:
          return 0;
      }
    });

  const totalValue = wishlist.reduce((sum, item) => sum + item.price, 0);
  const categories = Array.from(new Set(wishlist.map(item => item.category)));

  if (!localStorage.getItem('accessToken')) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-gray-800 pt-16">
        <div className="max-w-4xl mx-auto py-8 px-4">
          <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-8 text-center">
            <Heart className="h-16 w-16 text-gray-400 mx-auto mb-4" />
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">Access Denied</h2>
            <p className="text-gray-600 dark:text-gray-300">You must be logged in to view your wishlist.</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-gray-800 pt-16">
      <div className="max-w-7xl mx-auto py-8 px-4">
        {/* Header Section */}
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6 mb-8">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">
                My Wishlist
              </h1>
              <p className="text-gray-600 dark:text-gray-300">
                {wishlist.length} items • Total value: ₹{totalValue.toLocaleString()}
              </p>
            </div>
            <div className="flex items-center space-x-3">
              <button 
                onClick={() => setViewMode('grid')}
                className={`p-2 rounded-lg transition-colors ${
                  viewMode === 'grid' 
                    ? 'bg-primary-100 text-primary-600 dark:bg-primary-900 dark:text-primary-400' 
                    : 'text-gray-500 hover:text-gray-700 dark:hover:text-gray-300'
                }`}
              >
                <Grid className="h-5 w-5" />
              </button>
              <button 
                onClick={() => setViewMode('list')}
                className={`p-2 rounded-lg transition-colors ${
                  viewMode === 'list' 
                    ? 'bg-primary-100 text-primary-600 dark:bg-primary-900 dark:text-primary-400' 
                    : 'text-gray-500 hover:text-gray-700 dark:hover:text-gray-300'
                }`}
              >
                <List className="h-5 w-5" />
              </button>
            </div>
          </div>

          {/* Stats Cards */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
            <div className="bg-gradient-to-r from-pink-500 to-red-500 rounded-lg p-4 text-white">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm opacity-90">Total Items</p>
                  <p className="text-2xl font-bold">{wishlist.length}</p>
                </div>
                <Heart className="h-8 w-8 opacity-80" />
              </div>
            </div>
            <div className="bg-gradient-to-r from-green-500 to-emerald-500 rounded-lg p-4 text-white">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm opacity-90">Total Value</p>
                  <p className="text-2xl font-bold">₹{totalValue.toLocaleString()}</p>
                </div>
                <DollarSign className="h-8 w-8 opacity-80" />
              </div>
            </div>
            <div className="bg-gradient-to-r from-blue-500 to-indigo-500 rounded-lg p-4 text-white">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm opacity-90">Categories</p>
                  <p className="text-2xl font-bold">{categories.length}</p>
                </div>
                <Package className="h-8 w-8 opacity-80" />
              </div>
            </div>
            <div className="bg-gradient-to-r from-purple-500 to-pink-500 rounded-lg p-4 text-white">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm opacity-90">Avg Price</p>
                  <p className="text-2xl font-bold">
                    ₹{wishlist.length > 0 ? Math.round(totalValue / wishlist.length).toLocaleString() : 0}
                  </p>
                </div>
                <TrendingUp className="h-8 w-8 opacity-80" />
              </div>
            </div>
          </div>

          {/* Search and Filters */}
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="flex-1 relative">
              <Search className="h-5 w-5 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
              <input
                type="text"
                placeholder="Search your wishlist..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-10 pr-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 dark:bg-gray-700 dark:text-white"
              />
            </div>
            <div className="flex items-center space-x-2">
              <button 
                onClick={() => setShowFilters(!showFilters)}
                className={`px-4 py-3 rounded-lg transition-colors ${
                  showFilters 
                    ? 'bg-primary-100 text-primary-600 dark:bg-primary-900 dark:text-primary-400' 
                    : 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
                }`}
              >
                <Filter className="h-5 w-5 mr-2" />
                Filters
              </button>
              <select
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
                className="px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 dark:bg-gray-700 dark:text-white"
              >
                                 <option value="price-low">Price: Low to High</option>
                 <option value="price-high">Price: High to Low</option>
                 <option value="name">Name A-Z</option>
              </select>
            </div>
          </div>

          {/* Filter Panel */}
          {showFilters && (
            <div className="mt-4 p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Category
                  </label>
                  <select
                    value={categoryFilter}
                    onChange={(e) => setCategoryFilter(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 dark:bg-gray-700 dark:text-white"
                  >
                    <option value="all">All Categories</option>
                    {categories.map(category => (
                      <option key={category} value={category}>{category}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Price Range
                  </label>
                  <select
                    value={priceRange}
                    onChange={(e) => setPriceRange(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 dark:bg-gray-700 dark:text-white"
                  >
                    <option value="all">All Prices</option>
                    <option value="0-500">₹0 - ₹500</option>
                    <option value="500-1000">₹500 - ₹1,000</option>
                    <option value="1000-5000">₹1,000 - ₹5,000</option>
                    <option value="5000+">₹5,000+</option>
                  </select>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Content */}
        {loading ? (
          <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-8 text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-500 mx-auto mb-4"></div>
            <p className="text-gray-600 dark:text-gray-300">Loading your wishlist...</p>
          </div>
        ) : error ? (
          <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-8 text-center">
            <div className="text-red-600 dark:text-red-400">{error}</div>
          </div>
        ) : filteredAndSortedWishlist.length === 0 ? (
          <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-8 text-center">
            <Heart className="h-16 w-16 text-gray-400 mx-auto mb-4" />
            <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">
              {searchQuery || categoryFilter !== 'all' || priceRange !== 'all' ? 'No matching items' : 'Your wishlist is empty'}
            </h3>
            <p className="text-gray-600 dark:text-gray-300 mb-4">
              {searchQuery || categoryFilter !== 'all' || priceRange !== 'all' 
                ? 'Try adjusting your search or filters' 
                : 'Start adding items to your wishlist to see them here!'}
            </p>
            {!searchQuery && categoryFilter === 'all' && priceRange === 'all' && (
              <button 
                onClick={() => navigate('/search')}
                className="bg-primary-500 hover:bg-primary-600 text-white px-6 py-3 rounded-lg font-semibold transition-colors"
              >
                Browse Listings
              </button>
            )}
          </div>
        ) : (
          <div className={`grid gap-6 ${
            viewMode === 'grid' 
              ? 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4' 
              : 'grid-cols-1'
          }`}>
            {filteredAndSortedWishlist.map(item => {
              const isSold = soldItems.has(item.id);
              return (
                <div key={item.id} className={`bg-white dark:bg-gray-800 rounded-xl shadow-lg overflow-hidden hover:shadow-xl transition-shadow relative ${isSold ? 'opacity-75' : ''}`}>
                {/* Image */}
                <div className="relative">
                  {item.images && item.images.length > 0 ? (
                    <img
                      src={`${process.env.REACT_APP_BASE_URL}${item.images[0]}`}
                      alt={item.title}
                      className="w-full h-48 object-cover"
                    />
                  ) : (
                    <div className="w-full h-48 bg-gray-200 dark:bg-gray-700 flex items-center justify-center text-gray-400">
                      <Package className="h-12 w-12" />
                    </div>
                  )}
                  
                  {/* Sold Badge */}
                  {isSold && (
                    <div className="absolute top-3 right-3 bg-red-500 text-white px-2 py-1 rounded text-xs font-semibold z-10">
                      SOLD
                    </div>
                  )}
                  
                  {/* Price Badge */}
                  <div className={`absolute ${isSold ? 'top-12' : 'top-3'} right-3 bg-primary-500 text-white px-3 py-1 rounded-full text-sm font-semibold`}>
                    ₹{item.price.toLocaleString()}
                  </div>
                  
                  {/* Remove Button */}
                  <button
                    onClick={() => removeFromWishlist(item.id)}
                    className="absolute top-3 left-3 bg-red-500 hover:bg-red-600 text-white p-2 rounded-full transition-colors"
                  >
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>

                {/* Content */}
                <div className="p-4">
                  <h3 className="font-bold text-gray-900 dark:text-white text-lg mb-2 line-clamp-2">
                    {item.title}
                  </h3>
                  
                  <p className="text-gray-600 dark:text-gray-300 text-sm mb-3 line-clamp-2">
                    {item.description}
                  </p>

                  {/* Details */}
                  <div className="space-y-2 mb-4">
                    <div className="flex items-center text-xs text-gray-500 dark:text-gray-400">
                      <Package className="h-3 w-3 mr-1" />
                      {item.category}
                    </div>
                    <div className="flex items-center text-xs text-gray-500 dark:text-gray-400">
                      <Star className="h-3 w-3 mr-1" />
                      {item.condition}
                    </div>
                    {item.sellerName && (
                      <div className="flex items-center text-xs text-gray-500 dark:text-gray-400">
                        <User className="h-3 w-3 mr-1" />
                        {item.sellerName}
                      </div>
                    )}
                    
                  </div>

                  {/* Actions */}
                  <div className="flex items-center justify-between">
                    <button 
                      onClick={() => !isSold && navigate(`/listings/${item.id}`)}
                      disabled={isSold}
                      className={`flex items-center text-sm font-medium transition-colors ${
                        isSold 
                          ? 'text-gray-400 cursor-not-allowed' 
                          : 'text-primary-600 dark:text-primary-400 hover:text-primary-700 dark:hover:text-primary-300'
                      }`}
                    >
                      <Eye className="h-4 w-4 mr-1" />
                      {isSold ? 'Sold' : 'View Details'}
                    </button>
                    <div className="flex items-center space-x-2">
                      <button 
                        onClick={() => navigate('/messages', { state: { selectedUserId: item.sellerId, selectedUserName: item.sellerName } })}
                        className="p-2 text-gray-500 hover:text-gray-700 dark:hover:text-gray-300 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
                        title="Message seller"
                      >
                        <MessageCircle className="h-4 w-4" />
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
          </div>
        )}
      </div>
    </div>
  );
};

export default Wishlist; 
