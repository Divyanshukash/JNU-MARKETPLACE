import React, { useEffect, useState, useRef } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService } from '../services/authService';
import axios from 'axios';
import { User } from '../types';
import { formatDate } from '../utils/dateUtils';
import { 
  Plus, 
  MessageCircle, 
  Heart, 
  User as UserIcon, 
  Edit, 
  Trash2, 
  Eye, 
  Calendar, 
  IndianRupee, 
  TrendingUp, 
  Package, 
  Star,
  Search,
  Filter,
  Upload,
  Camera,
  X,
  AlertCircle,
  Clock,
  CheckCircle,
  XCircle
} from 'lucide-react';

interface UploadingImage {
  file: File;
  progress: number;
  url?: string;
  error?: string;
}

interface Listing {
  _id?: string;
  id?: string;
  title: string;
  price: number;
  images?: string[];
  createdAt: string;
  status: string;
  description?: string;
  category?: string;
  condition?: string;
  donation?: boolean;
  isDonation?: boolean;
}

const Dashboard: React.FC = () => {
  const [user, setUser] = useState<User | null>(null);
  const [listings, setListings] = useState<Listing[]>([]);
  const [userError, setUserError] = useState<string | null>(null);
  const [listingsError, setListingsError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [deleteLoading, setDeleteLoading] = useState<string | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [deleteSuccess, setDeleteSuccess] = useState<string | null>(null);
  const [editingId, setEditingId] = useState<string | undefined>(undefined);
  const [editForm, setEditForm] = useState<Partial<Listing>>({});
  const [editLoading, setEditLoading] = useState(false);
  const [editError, setEditError] = useState<string | null>(null);
  const [editSuccess, setEditSuccess] = useState<string | null>(null);
  const [categories, setCategories] = useState<string[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [uploadingImages, setUploadingImages] = useState<UploadingImage[]>([]);
  const [filteredListings, setFilteredListings] = useState<Listing[]>([]);
  const [showFilters, setShowFilters] = useState(false);
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [categoryFilter, setCategoryFilter] = useState<string>('all');
  const [priceRange, setPriceRange] = useState<string>('all');
  const [messagesCount, setMessagesCount] = useState<number>(0);
  const filterRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      setUserError(null);
      setListingsError(null);
      let userOk = false;
      let listingsOk = false;
      try {
        // Fetch user profile
        const userData = await authService.getCurrentUser();
        setUser(userData);
        userOk = true;
      } catch (err: any) {
        setUserError('Failed to load user info. ' + (err?.response?.data?.message || err.message));
        console.error('User profile error:', err);
      }
      try {
        // Fetch recent listings for the current user
        const res = await axios.get('/api/listings/my-listings', {
          baseURL: 'http://localhost:8080',
          headers: {
            Authorization: `Bearer ${localStorage.getItem('accessToken')}`,
          },
        });
        setListings(res.data || []);
        listingsOk = true;
      } catch (err: any) {
        setListingsError('Failed to load listings. ' + (err?.response?.data?.message || err.message));
        console.error('Listings error:', err);
      }
      
      try {
        // Fetch messages count
        const messagesRes = await axios.get('/api/messages/conversations', {
          baseURL: 'http://localhost:8080',
          headers: {
            Authorization: `Bearer ${localStorage.getItem('accessToken')}`,
          },
        });
        setMessagesCount(messagesRes.data?.length || 0);
      } catch (err: any) {
        console.error('Messages count error:', err);
        setMessagesCount(0);
      }
      setLoading(false);
      if (!userOk && !listingsOk) {
        setUserError('Failed to load dashboard data.');
      }
    };
    fetchData();
  }, []);

  useEffect(() => {
    axios.get('http://localhost:8080/api/listings/categories')
      .then(res => setCategories(res.data))
      .catch(() => setCategories([]));
  }, []);

  // Filter listings based on search query and filters
  useEffect(() => {
    let filtered = listings;

    // Apply search filter
    if (searchQuery.trim()) {
      filtered = filtered.filter(listing => 
        listing.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        listing.description?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        listing.category?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        listing.condition?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        listing.price.toString().includes(searchQuery)
      );
    }

    // Apply status filter
    if (statusFilter !== 'all') {
      filtered = filtered.filter(listing => listing.status === statusFilter);
    }

    // Apply category filter
    if (categoryFilter !== 'all') {
      filtered = filtered.filter(listing => listing.category === categoryFilter);
    }

    // Apply price range filter
    if (priceRange !== 'all') {
      filtered = filtered.filter(listing => {
        const price = listing.price || 0;
        switch (priceRange) {
          case '0-500':
            return price >= 0 && price <= 500;
          case '500-1000':
            return price > 500 && price <= 1000;
          case '1000-5000':
            return price > 1000 && price <= 5000;
          case '5000+':
            return price > 5000;
          default:
            return true;
        }
      });
    }

    setFilteredListings(filtered);
  }, [searchQuery, statusFilter, categoryFilter, priceRange, listings]);

  // Separate donation and regular listings
  const donationListings = filteredListings.filter(l => l.price === 0 || l.donation === true || l.isDonation === true);
  const regularListings = filteredListings.filter(l => !(l.price === 0 || l.donation === true || l.isDonation === true));

  // Close filter dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (filterRef.current && !filterRef.current.contains(event.target as Node)) {
        setShowFilters(false);
      }
    };

    if (showFilters) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showFilters]);

  const handleDelete = async (id: string | undefined) => {
    if (!id) return;
    setDeleteLoading(id);
    setDeleteError(null);
    setDeleteSuccess(null);
    try {
      await axios.delete(`/api/listings/${id}`, {
        baseURL: 'http://localhost:8080',
        headers: {
          Authorization: `Bearer ${localStorage.getItem('accessToken')}`,
        },
      });
      setListings(prev => prev.filter(listing => (listing.id || listing._id) !== id));
      setDeleteSuccess('Listing deleted successfully.');
    } catch (err: any) {
      setDeleteError('Failed to delete listing.');
    } finally {
      setDeleteLoading(null);
    }
  };

  const startEdit = (listing: Listing) => {
    setEditingId(listing.id || listing._id);
    setEditForm({ ...listing });
    setEditError(null);
    setEditSuccess(null);
  };

  const cancelEdit = () => {
    setEditingId(undefined);
    setEditForm({});
    setEditError(null);
    setEditSuccess(null);
    setUploadingImages([]);
  };

  const handleEditChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    setEditForm({ ...editForm, [e.target.name]: e.target.value });
  };

  const handleEditSave = async () => {
    if (!editingId) return;
    setEditLoading(true);
    setEditError(null);
    setEditSuccess(null);
    try {
      // Wait for all image uploads to complete
      const uploadPromises = uploadingImages
        .filter(img => !img.url && !img.error)
        .map((img, index) => uploadImage(img.file, index));
      
      if (uploadPromises.length > 0) {
        await Promise.all(uploadPromises);
      }

      // Get the final images array (existing + newly uploaded)
      const existingImages = editForm.images || [];
      const newImages = uploadingImages
        .filter(img => img.url)
        .map(img => img.url!);
      const allImages = [...existingImages, ...newImages];

      const res = await axios.put(
        `/api/listings/${editingId}`,
        {
          title: editForm.title,
          price: editForm.price,
          description: editForm.description,
          category: editForm.category,
          condition: editForm.condition,
          images: allImages,
          negotiable: false,
        },
        {
          baseURL: 'http://localhost:8080',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${localStorage.getItem('accessToken')}`,
          },
        }
      );
      setListings(prev => prev.map(listing => ((listing.id || listing._id) === editingId ? { ...listing, ...res.data } : listing)));
      setEditSuccess('Listing updated successfully!');
      setEditingId(undefined);
      setEditForm({});
      setUploadingImages([]);
    } catch (err: any) {
      setEditError('Failed to update listing.');
    } finally {
      setEditLoading(false);
    }
  };

  const handleImageChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files || e.target.files.length === 0) return;
    setEditError(null);
    const files = Array.from(e.target.files);
    const newUploading: UploadingImage[] = files.map(file => ({ file, progress: 0 }));
    setUploadingImages(prev => [...prev, ...newUploading]);
    files.forEach((file, idx) => uploadImage(file, uploadingImages.length + idx));
  };

  const uploadImage = async (file: File, index: number) => {
    const formData = new FormData();
    formData.append('file', file);
    try {
      const res = await axios.post('/api/listings/upload', formData, {
        baseURL: 'http://localhost:8080',
        headers: {
          Authorization: `Bearer ${localStorage.getItem('accessToken')}`,
        },
        onUploadProgress: (progressEvent) => {
          const percent = Math.round((progressEvent.loaded * 100) / (progressEvent.total || 1));
          setUploadingImages(prev => prev.map((img, i) => i === index ? { ...img, progress: percent } : img));
        },
      });
      setUploadingImages(prev => prev.map((img, i) => i === index ? { ...img, url: res.data, progress: 100 } : img));
    } catch (err: any) {
      setUploadingImages(prev => prev.map((img, i) => i === index ? { ...img, error: 'Failed to upload' } : img));
      setEditError('Failed to upload one or more images.');
    }
  };

  const handleRemoveImage = (url: string, isExisting: boolean = false) => {
    if (isExisting) {
      // Remove from existing images
      setEditForm(prev => ({
        ...prev,
        images: prev.images?.filter(img => img !== url) || []
      }));
    } else {
      // Remove from uploading images
      setUploadingImages(prev => prev.filter(img => img.url !== url));
    }
  };

  const handleRemoveUploadingImage = (index: number) => {
    setUploadingImages(prev => prev.filter((_, i) => i !== index));
  };

  if (loading) return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-gray-800 flex items-center justify-center">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto mb-4"></div>
        <p className="text-gray-600 dark:text-gray-300">Loading your dashboard...</p>
      </div>
    </div>
  );

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-gray-800 pt-16">
      {/* Header Section */}
      <div className="bg-white/80 dark:bg-gray-800/80 backdrop-blur-sm border-b border-gray-200 dark:border-gray-700">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <div className="w-12 h-12 bg-gradient-to-r from-primary-600 to-primary-700 rounded-xl flex items-center justify-center">
                <span className="text-white font-bold text-lg">JNU</span>
              </div>
              <div>
                <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
                  Welcome back{user ? `, ${user.firstName}!` : '!'}
                </h1>
                <p className="text-gray-600 dark:text-gray-300">Manage your listings and activities</p>
              </div>
            </div>

          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="bg-white dark:bg-gray-800 rounded-xl p-6 shadow-lg border border-gray-100 dark:border-gray-700">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total Listings</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{listings.length}</p>
              </div>
              <div className="w-12 h-12 bg-blue-100 dark:bg-blue-900 rounded-lg flex items-center justify-center">
                <Package className="h-6 w-6 text-blue-600 dark:text-blue-400" />
              </div>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-xl p-6 shadow-lg border border-gray-100 dark:border-gray-700">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Active Listings</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">
                  {listings.filter(l => l.status === 'ACTIVE').length}
                </p>
              </div>
              <div className="w-12 h-12 bg-green-100 dark:bg-green-900 rounded-lg flex items-center justify-center">
                <TrendingUp className="h-6 w-6 text-green-600 dark:text-green-400" />
              </div>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-xl p-6 shadow-lg border border-gray-100 dark:border-gray-700">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total Value</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">
                  ₹{listings.reduce((sum, l) => sum + (l.price || 0), 0).toLocaleString()}
                </p>
              </div>
                                <div className="w-12 h-12 bg-yellow-100 dark:bg-yellow-900 rounded-lg flex items-center justify-center">
                    <IndianRupee className="h-6 w-6 text-yellow-600 dark:text-yellow-400" />
                  </div>
            </div>
          </div>

          <div 
            className="bg-white dark:bg-gray-800 rounded-xl p-6 shadow-lg border border-gray-100 dark:border-gray-700 cursor-pointer hover:shadow-xl transition-all duration-200 hover:scale-105"
            onClick={() => navigate('/messages')}
          >
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Messages</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{messagesCount}</p>
              </div>
              <div className="w-12 h-12 bg-purple-100 dark:bg-purple-900 rounded-lg flex items-center justify-center">
                <MessageCircle className="h-6 w-6 text-purple-600 dark:text-purple-400" />
              </div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* User Profile Card */}
          <div className="lg:col-span-1">
            <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-100 dark:border-gray-700 overflow-hidden">
              <div className="bg-gradient-to-r from-primary-600 to-primary-700 p-6 text-white">
                <div className="flex items-center space-x-4">
                  {user?.profilePicture ? (
                    <img 
                      src={`http://localhost:8080${user.profilePicture}`} 
                      alt="Profile" 
                      className="w-16 h-16 rounded-full object-cover border-2 border-white/20" 
                    />
                  ) : (
                    <div className="w-16 h-16 bg-white/20 rounded-full flex items-center justify-center">
                      <UserIcon className="h-8 w-8 text-white" />
                    </div>
                  )}
                  <div>
                    <h3 className="text-xl font-semibold">{user?.firstName} {user?.lastName}</h3>
                    <p className="text-primary-100">{user?.email}</p>
                  </div>
                </div>
              </div>
              
              <div className="p-6">
                <div className="space-y-4">
                  <div className="flex items-center space-x-3 text-gray-600 dark:text-gray-300">
                    <Calendar className="h-4 w-4" />
                    <span>Member since {user?.createdAt ? formatDate(user.createdAt) : 'Recently'}</span>
                  </div>
                </div>

                <div className="mt-6 space-y-3">
                  <button 
                    onClick={() => navigate('/create-listing')}
                    className="w-full bg-gradient-to-r from-primary-600 to-primary-700 text-white py-3 px-4 rounded-lg font-semibold hover:from-primary-700 hover:to-primary-800 transition-all duration-200 flex items-center justify-center space-x-2"
                  >
                    <Plus className="h-5 w-5" />
                    <span>Create New Listing</span>
                  </button>
                  
                  <div className="grid grid-cols-2 gap-3">
                    <button 
                      onClick={() => navigate('/messages')}
                      className="bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 py-2 px-4 rounded-lg font-medium hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors flex items-center justify-center space-x-2"
                    >
                      <MessageCircle className="h-4 w-4" />
                      <span>Messages</span>
                    </button>
                    <button 
                      onClick={() => navigate('/wishlist')}
                      className="bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 py-2 px-4 rounded-lg font-medium hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors flex items-center justify-center space-x-2"
                    >
                      <Heart className="h-4 w-4" />
                      <span>Wishlist</span>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Listings Section */}
          <div className="lg:col-span-2">
            <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-100 dark:border-gray-700">
              <div className="p-6 border-b border-gray-200 dark:border-gray-700">
                <div className="flex items-center justify-between">
                  <h2 className="text-xl font-bold text-gray-900 dark:text-white">Your Listings</h2>
                  <div className="flex items-center space-x-2">
                    <div className="relative">
                      <Search className="h-4 w-4 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                      <input
                        type="text"
                        placeholder="Search listings..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 dark:bg-gray-700 dark:text-white"
                      />
                      {searchQuery && (
                        <button
                          onClick={() => setSearchQuery('')}
                          className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                        >
                          ×
                        </button>
                      )}
                    </div>
                    <div className="relative" ref={filterRef}>
                      <button 
                        onClick={() => setShowFilters(!showFilters)}
                        className={`p-2 rounded-lg transition-colors ${
                          showFilters 
                            ? 'bg-primary-100 text-primary-600 dark:bg-primary-900 dark:text-primary-400' 
                            : 'text-gray-400 hover:text-gray-600 dark:hover:text-gray-300'
                        }`}
                      >
                        <Filter className="h-4 w-4" />
                      </button>
                      
                      {/* Filter Dropdown */}
                      {showFilters && (
                        <div className="absolute right-0 top-full mt-2 w-80 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 p-4 z-10">
                          <div className="space-y-4">
                            <div>
                              <h4 className="font-medium text-gray-900 dark:text-white mb-2">Status</h4>
                              <select
                                value={statusFilter}
                                onChange={(e) => setStatusFilter(e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 dark:bg-gray-700 dark:text-white"
                              >
                                <option value="all">All Status</option>
                                <option value="ACTIVE">Active</option>
                                <option value="SOLD">Sold</option>
                                <option value="EXPIRED">Expired</option>
                                <option value="DRAFT">Draft</option>
                              </select>
                            </div>

                            <div>
                              <h4 className="font-medium text-gray-900 dark:text-white mb-2">Category</h4>
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
                              <h4 className="font-medium text-gray-900 dark:text-white mb-2">Price Range</h4>
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

                            <div className="flex gap-2 pt-2">
                              <button
                                onClick={() => {
                                  setStatusFilter('all');
                                  setCategoryFilter('all');
                                  setPriceRange('all');
                                }}
                                className="flex-1 px-3 py-2 text-sm text-gray-600 dark:text-gray-300 hover:text-gray-800 dark:hover:text-gray-100 transition-colors"
                              >
                                Clear All
                              </button>
                              <button
                                onClick={() => setShowFilters(false)}
                                className="flex-1 px-3 py-2 bg-primary-600 text-white text-sm rounded-lg hover:bg-primary-700 transition-colors"
                              >
                                Apply
                              </button>
                            </div>
                          </div>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </div>

              <div className="p-6">
                {/* Show donations first if any */}
                {donationListings.length > 0 && (
                  <div className="mb-8">
                    <h3 className="text-lg font-bold text-green-700 dark:text-green-300 mb-4">Your Donation Listings</h3>
                    <div className="space-y-4">
                      {donationListings.map(listing => (
                        <div key={listing.id || listing._id} className="border border-green-200 dark:border-green-700 rounded-lg p-4 bg-green-50 dark:bg-green-900/20 flex items-start space-x-4">
                          {listing.images && listing.images.length > 0 ? (
                            <img
                              src={`http://localhost:8080${listing.images[0]}`}
                              alt={listing.title}
                              className="w-20 h-20 object-cover rounded-lg"
                            />
                          ) : (
                            <div className="w-20 h-20 bg-gray-200 dark:bg-gray-700 rounded-lg flex items-center justify-center">
                              <Package className="h-8 w-8 text-gray-400" />
                            </div>
                          )}
                          <div className="flex-1">
                            <div className="flex items-center space-x-2 mb-1">
                              <span className="px-2 py-1 bg-green-200 text-green-800 dark:bg-green-800 dark:text-green-200 text-xs rounded-full font-semibold">Donation</span>
                              <h4 className="font-semibold text-gray-900 dark:text-white">{listing.title}</h4>
                            </div>
                            <p className="text-gray-600 dark:text-gray-300 text-sm mb-2">{listing.description}</p>
                            <div className="flex items-center space-x-4 text-sm text-gray-500 dark:text-gray-400">
                              <span className="flex items-center space-x-1">
                                <Calendar className="h-4 w-4" />
                                <span>{formatDate(listing.createdAt)}</span>
                              </span>
                              <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                                listing.status === 'ACTIVE' ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' :
                                listing.status === 'SOLD' ? 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200' :
                                'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200'
                              }`}>
                                {listing.status}
                              </span>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
                {listingsError ? (
                  <div className="text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-900/20 p-4 rounded-lg">
                    {listingsError}
                  </div>
                ) : regularListings.length === 0 ? (
                  <div className="text-center py-12">
                    {searchQuery ? (
                      <>
                        <Search className="h-16 w-16 text-gray-400 mx-auto mb-4" />
                        <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">No listings found</h3>
                        <p className="text-gray-500 dark:text-gray-400 mb-6">Try adjusting your search terms</p>
                        <button 
                          onClick={() => setSearchQuery('')}
                          className="bg-primary-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-primary-700 transition-colors"
                        >
                          Clear Search
                        </button>
                      </>
                    ) : (
                      <>
                        <Package className="h-16 w-16 text-gray-400 mx-auto mb-4" />
                        <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">No listings yet</h3>
                        <p className="text-gray-500 dark:text-gray-400 mb-6">Start selling by creating your first listing</p>
                        <button 
                          onClick={() => navigate('/create-listing')}
                          className="bg-primary-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-primary-700 transition-colors"
                        >
                          Create Your First Listing
                        </button>
                      </>
                    )}
                  </div>
                ) : (
                  <div className="space-y-4">
                    {(searchQuery || statusFilter !== 'all' || categoryFilter !== 'all' || priceRange !== 'all') && (
                      <div className="mb-4 p-3 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                        <p className="text-sm text-blue-600 dark:text-blue-400">
                          Showing {regularListings.length} of {listings.length} listings
                          {searchQuery && ` for "${searchQuery}"`}
                          {(statusFilter !== 'all' || categoryFilter !== 'all' || priceRange !== 'all') && ' with filters applied'}
                        </p>
                        {(statusFilter !== 'all' || categoryFilter !== 'all' || priceRange !== 'all') && (
                          <div className="flex flex-wrap gap-2 mt-2">
                            {statusFilter !== 'all' && (
                              <span className="px-2 py-1 bg-blue-100 dark:bg-blue-800 text-blue-800 dark:text-blue-200 text-xs rounded-full">
                                Status: {statusFilter}
                              </span>
                            )}
                            {categoryFilter !== 'all' && (
                              <span className="px-2 py-1 bg-green-100 dark:bg-green-800 text-green-800 dark:text-green-200 text-xs rounded-full">
                                Category: {categoryFilter}
                              </span>
                            )}
                            {priceRange !== 'all' && (
                              <span className="px-2 py-1 bg-yellow-100 dark:bg-yellow-800 text-yellow-800 dark:text-yellow-200 text-xs rounded-full">
                                Price: {priceRange}
                              </span>
                            )}
                          </div>
                        )}
                      </div>
                    )}
                    {regularListings.map(listing => (
                      <div key={listing.id || listing._id} className="border border-gray-200 dark:border-gray-700 rounded-lg p-4 hover:shadow-md transition-shadow">
                        {editingId === (listing.id || listing._id) ? (
                          <div className="space-y-4">
                            <input
                              type="text"
                              name="title"
                              value={editForm.title as string || ''}
                              onChange={handleEditChange}
                              className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 dark:bg-gray-700 dark:text-white"
                              placeholder="Title"
                            />
                            <div className="grid grid-cols-2 gap-4">
                              <input
                                type="number"
                                name="price"
                                value={editForm.price as number || ''}
                                onChange={handleEditChange}
                                className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 dark:bg-gray-700 dark:text-white"
                                placeholder="Price"
                              />
                              <select
                                name="condition"
                                value={editForm.condition as string || ''}
                                onChange={handleEditChange}
                                className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 dark:bg-gray-700 dark:text-white"
                              >
                                <option value="NEW">New</option>
                                <option value="LIKE_NEW">Like New</option>
                                <option value="EXCELLENT">Excellent</option>
                                <option value="GOOD">Good</option>
                                <option value="FAIR">Fair</option>
                                <option value="POOR">Poor</option>
                              </select>
                            </div>
                            <textarea
                              name="description"
                              value={editForm.description as string || ''}
                              onChange={handleEditChange}
                              className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 dark:bg-gray-700 dark:text-white"
                              placeholder="Description"
                              rows={3}
                            />
                            <select
                              name="category"
                              value={editForm.category as string || ''}
                              onChange={handleEditChange}
                              className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 dark:bg-gray-700 dark:text-white"
                            >
                              {categories.length > 0 ? categories.map(cat => (
                                <option key={cat} value={cat}>{cat}</option>
                              )) : (
                                <option value={editForm.category as string || ''}>{editForm.category as string || 'Select Category'}</option>
                              )}
                            </select>

                            {/* Image Management Section */}
                            <div className="space-y-4">
                              <div className="flex items-center justify-between">
                                <label className="block text-sm font-medium text-gray-700 dark:text-gray-200">
                                  Images ({editForm.images?.length || 0} + {uploadingImages.filter(img => img.url).length})
                                </label>
                                <input 
                                  type="file" 
                                  accept="image/*" 
                                  multiple 
                                  onChange={handleImageChange} 
                                  disabled={uploadingImages.some(img => !img.url && !img.error)}
                                  className="hidden"
                                  id="edit-image-upload"
                                />
                                <label 
                                  htmlFor="edit-image-upload"
                                  className="bg-primary-500 hover:bg-primary-600 text-white px-3 py-1 rounded text-sm cursor-pointer transition-colors inline-flex items-center space-x-1"
                                >
                                  <Upload className="h-4 w-4" />
                                  <span>Add Images</span>
                                </label>
                              </div>

                              {/* Existing Images */}
                              {editForm.images && editForm.images.length > 0 && (
                                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
                                  {editForm.images.map((imgUrl, idx) => (
                                    <div key={idx} className="relative">
                                      <img 
                                        src={`http://localhost:8080${imgUrl}`} 
                                        alt={`Listing ${idx + 1}`} 
                                        className="w-full h-20 object-cover rounded-lg" 
                                      />
                                      <button
                                        type="button"
                                        className="absolute top-1 right-1 bg-red-500 text-white rounded-full w-5 h-5 flex items-center justify-center text-xs hover:bg-red-600 transition-colors"
                                        onClick={() => handleRemoveImage(imgUrl, true)}
                                        title="Remove image"
                                      >
                                        <X className="h-3 w-3" />
                                      </button>
                                    </div>
                                  ))}
                                </div>
                              )}

                              {/* Uploading Images */}
                              {uploadingImages.length > 0 && (
                                <div className="space-y-3">
                                  <h4 className="text-sm font-medium text-gray-700 dark:text-gray-200">
                                    Uploading Images
                                  </h4>
                                  <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
                                    {uploadingImages.map((img, idx) => (
                                      <div key={idx} className="relative">
                                        {img.url ? (
                                          <div className="relative">
                                            <img 
                                              src={`http://localhost:8080${img.url}`} 
                                              alt="Uploading" 
                                              className="w-full h-20 object-cover rounded-lg" 
                                            />
                                            <button
                                              type="button"
                                              className="absolute top-1 right-1 bg-red-500 text-white rounded-full w-5 h-5 flex items-center justify-center text-xs hover:bg-red-600 transition-colors"
                                              onClick={() => handleRemoveUploadingImage(idx)}
                                              title="Remove image"
                                            >
                                              <X className="h-3 w-3" />
                                            </button>
                                          </div>
                                        ) : (
                                          <div className="w-full h-20 bg-gray-200 dark:bg-gray-700 rounded-lg flex items-center justify-center">
                                            {img.progress < 100 && !img.error ? (
                                              <div className="text-center">
                                                <div className="text-xs text-blue-600 dark:text-blue-400 mb-1">{img.progress}%</div>
                                                <div className="w-6 h-1 bg-gray-300 dark:bg-gray-600 rounded-full overflow-hidden">
                                                  <div 
                                                    className="h-full bg-blue-500 transition-all duration-300"
                                                    style={{ width: `${img.progress}%` }}
                                                  />
                                                </div>
                                              </div>
                                            ) : img.error ? (
                                              <div className="text-center">
                                                <AlertCircle className="h-4 w-4 text-red-500 mx-auto mb-1" />
                                                <div className="text-xs text-red-600 dark:text-red-400">Error</div>
                                              </div>
                                            ) : null}
                                          </div>
                                        )}
                                      </div>
                                    ))}
                                  </div>
                                </div>
                              )}
                            </div>

                            <div className="flex gap-2">
                              <button 
                                onClick={handleEditSave} 
                                disabled={editLoading} 
                                className="bg-primary-600 text-white px-4 py-2 rounded-lg font-medium hover:bg-primary-700 disabled:opacity-50 transition-colors"
                              >
                                {editLoading ? 'Saving...' : 'Save Changes'}
                              </button>
                              <button 
                                onClick={cancelEdit} 
                                className="bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 px-4 py-2 rounded-lg font-medium hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors"
                              >
                                Cancel
                              </button>
                            </div>
                            {editError && <div className="text-red-600 text-sm">{editError}</div>}
                            {editSuccess && <div className="text-green-600 text-sm">{editSuccess}</div>}
                          </div>
                        ) : (
                          <div className="flex items-start space-x-4">
                            {listing.images && listing.images.length > 0 ? (
                              <img
                                src={`http://localhost:8080${listing.images[0]}`}
                                alt={listing.title}
                                className="w-20 h-20 object-cover rounded-lg"
                              />
                            ) : (
                              <div className="w-20 h-20 bg-gray-200 dark:bg-gray-700 rounded-lg flex items-center justify-center">
                                <Package className="h-8 w-8 text-gray-400" />
                              </div>
                            )}
                            
                            <div className="flex-1">
                              <div className="flex items-start justify-between">
                                <div>
                                  <h3 className="font-semibold text-gray-900 dark:text-white mb-1">{listing.title}</h3>
                                  <p className="text-gray-600 dark:text-gray-300 text-sm mb-2">{listing.description}</p>
                                  <div className="flex items-center space-x-4 text-sm text-gray-500 dark:text-gray-400">
                                    <span className="flex items-center space-x-1">
                                      <IndianRupee className="h-4 w-4" />
                                      <span>₹{listing.price}</span>
                                    </span>
                                    <span className="flex items-center space-x-1">
                                      <Calendar className="h-4 w-4" />
                                      <span>{formatDate(listing.createdAt)}</span>
                                    </span>
                                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                                      listing.status === 'ACTIVE' ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' :
                                      listing.status === 'SOLD' ? 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200' :
                                      'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200'
                                    }`}>
                                      {listing.status}
                                    </span>
                                  </div>
                                </div>
                                
                                <div className="flex items-center space-x-2">
                                  <button 
                                    onClick={() => startEdit(listing)}
                                    className="p-2 text-gray-400 hover:text-blue-600 dark:hover:text-blue-400 transition-colors"
                                    title="Edit"
                                  >
                                    <Edit className="h-4 w-4" />
                                  </button>
                                  <button
                                    onClick={() => handleDelete(listing.id || listing._id)}
                                    disabled={deleteLoading === (listing.id || listing._id)}
                                    className="p-2 text-gray-400 hover:text-red-600 dark:hover:text-red-400 transition-colors"
                                    title="Delete"
                                  >
                                    <Trash2 className="h-4 w-4" />
                                  </button>
                                </div>
                              </div>
                            </div>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Sale Requests Section */}
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6 mb-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-semibold text-gray-900 dark:text-white flex items-center space-x-2">
                <Package className="h-6 w-6 text-blue-600" />
                <span>Sale Requests</span>
              </h2>
              <Link
                to="/sale-requests"
                className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors text-sm"
              >
                View All
              </Link>
            </div>
            
            <div className="text-center py-8 text-gray-500">
              <Package size={48} className="mx-auto mb-4 text-gray-300" />
              <p className="mb-2">Manage your sale requests and offers</p>
              <p className="text-sm">View pending offers, accept/reject requests, and track completed sales</p>
            </div>
          </div>
        </div>

        {/* Success/Error Messages */}
        {deleteSuccess && (
          <div className="fixed bottom-4 right-4 bg-green-500 text-white px-6 py-3 rounded-lg shadow-lg">
            {deleteSuccess}
          </div>
        )}
        {deleteError && (
          <div className="fixed bottom-4 right-4 bg-red-500 text-white px-6 py-3 rounded-lg shadow-lg">
            {deleteError}
          </div>
        )}
      </div>
    </div>
  );
};

export default Dashboard; 