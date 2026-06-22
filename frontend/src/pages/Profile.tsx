import React, { useEffect, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import { formatDate } from '../utils/dateUtils';
import axios from 'axios';
import { 
  User, 
  Camera, 
  Edit, 
  Save, 
  X, 
  Upload, 
  Calendar, 
  Mail, 
  Phone, 
  MapPin,
  Package,
  MessageCircle,
  Heart,
  TrendingUp,
  Shield,
  Settings,
  Bell,
  Globe,
  Award
} from 'lucide-react';

const Profile: React.FC = () => {
  const { user, updateUser } = useAuth();
  const navigate = useNavigate();
  const [profile, setProfile] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    bio: '',
    address: '',
    qrCodeImage: '',
  });
  const [editing, setEditing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [success, setSuccess] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [profilePicture, setProfilePicture] = useState<string | undefined>(undefined);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const [uploadSuccess, setUploadSuccess] = useState<string | null>(null);
  const [stats, setStats] = useState({
    listings: 0,
    messages: 0,
    wishlist: 0
  });
  const [qrFile, setQrFile] = useState<File | null>(null);
  const [qrUploading, setQrUploading] = useState(false);
  const [qrUploadError, setQrUploadError] = useState<string | null>(null);
  const [qrUploadSuccess, setQrUploadSuccess] = useState<string | null>(null);

  useEffect(() => {
    const fetchProfile = async () => {
      setLoading(true);
      setError(null);
      try {
        const res = await axios.get('/api/users/profile', {
          baseURL: process.env.REACT_APP_BASE_URL,
          headers: {
            Authorization: `Bearer ${localStorage.getItem('accessToken')}`,
          },
        });
        setProfile({
          firstName: res.data.firstName || '',
          lastName: res.data.lastName || '',
          email: res.data.email || '',
          phone: res.data.phoneNumber || '',
          bio: res.data.bio || '',
          address: res.data.address || '',
          qrCodeImage: res.data.qrCodeImage || '',
        });
        setProfilePicture(res.data.profilePicture);
        
        // Fetch user stats
        fetchUserStats();
      } catch (err: any) {
        setError('Failed to load profile.');
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, []);

  const fetchUserStats = async () => {
    try {
      const token = localStorage.getItem('accessToken');
      const [listingsRes, messagesRes, wishlistRes] = await Promise.all([
        axios.get('/api/listings/my-listings', {
          baseURL:process.env.REACT_APP_BASE_URL,
          headers: { Authorization: `Bearer ${token}` },
        }),
        axios.get('/api/messages/conversations', {
          baseURL: process.env.REACT_APP_BASE_URL,
          headers: { Authorization: `Bearer ${token}` },
        }),
        axios.get('/api/users/wishlist', {
          baseURL: process.env.REACT_APP_BASE_URL,
          headers: { Authorization: `Bearer ${token}` },
        })
      ]);
      
      setStats({
        listings: listingsRes.data?.length || 0,
        messages: messagesRes.data?.length || 0,
        wishlist: wishlistRes.data?.length || 0
      });
    } catch (error) {
      console.error('Failed to fetch stats:', error);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setProfile({ ...profile, [e.target.name]: e.target.value });
  };

  const handleSave = async () => {
    setSuccess(null);
    setError(null);
    setLoading(true);
    try {
      const res = await axios.put(
        '/api/users/profile',
        {
          firstName: profile.firstName,
          lastName: profile.lastName,
          phoneNumber: profile.phone,
          bio: profile.bio,
          address: profile.address,
        },
        {
          baseURL: process.env.REACT_APP_BASE_URL,
          headers: {
            Authorization: `Bearer ${localStorage.getItem('accessToken')}`,
          },
        }
      );
      setProfile({
        firstName: res.data.firstName || '',
        lastName: res.data.lastName || '',
        email: res.data.email || '',
        phone: res.data.phoneNumber || '',
        bio: res.data.bio || '',
        address: res.data.address || '',
        qrCodeImage: res.data.qrCodeImage || '',
      });
      setSuccess('Profile updated successfully!');
      setEditing(false);
      updateUser(res.data);
    } catch (err: any) {
      setError('Failed to update profile.');
    } finally {
      setLoading(false);
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
      setUploadError(null);
      setUploadSuccess(null);
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) return;
    setUploading(true);
    setUploadError(null);
    setUploadSuccess(null);
    try {
      const formData = new FormData();
      formData.append('file', selectedFile);
      const res = await axios.post('/api/users/upload-profile-picture', formData, {
        baseURL: process.env.REACT_APP_BASE_URL,
        headers: {
          Authorization: `Bearer ${localStorage.getItem('accessToken')}`,
        },
      });
      setProfilePicture(res.data);
      setUploadSuccess('Profile picture updated!');
      setSelectedFile(null);
    } catch (err: any) {
      setUploadError('Failed to upload profile picture.');
    } finally {
      setUploading(false);
    }
  };

  const handleQrFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setQrFile(e.target.files[0]);
      setQrUploadError(null);
      setQrUploadSuccess(null);
    }
  };

  const handleQrUpload = async () => {
    if (!qrFile) return;
    setQrUploading(true);
    setQrUploadError(null);
    setQrUploadSuccess(null);
    try {
      const formData = new FormData();
      formData.append('file', qrFile);
      const res = await axios.post('/api/users/upload-qr-code', formData, {
        baseURL: process.env.REACT_APP_BASE_URL,
        headers: {
          Authorization: `Bearer ${localStorage.getItem('accessToken')}`,
        },
      });
      setProfile((prev) => ({ ...prev, qrCodeImage: res.data }));
      setQrUploadSuccess('QR code uploaded!');
      setQrFile(null);
    } catch (err: any) {
      setQrUploadError('Failed to upload QR code.');
    } finally {
      setQrUploading(false);
    }
  };

  const handleQrRemove = () => {
    setProfile((prev) => ({ ...prev, qrCodeImage: '' }));
    setQrFile(null);
    setQrUploadError(null);
    setQrUploadSuccess(null);
  };

  if (loading) return (
    <div className="min-h-screen pt-16 bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-gray-800 flex items-center justify-center">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto mb-4"></div>
        <p className="text-gray-600 dark:text-gray-300">Loading your profile...</p>
      </div>
    </div>
  );

  return (
    <div className="min-h-screen pt-16 bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-gray-800">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">
            Your Profile
          </h1>
          <p className="text-gray-600 dark:text-gray-300">
            Manage your account and view your activity
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Profile Card */}
          <div className="lg:col-span-1">
            <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg overflow-hidden">
              {/* Profile Header */}
              <div className="bg-gradient-to-r from-primary-600 to-primary-700 p-6 text-white">
                <div className="text-center">
                  <div className="relative inline-block">
                    {profilePicture ? (
                      <img
                        src={`${process.env.REACT_APP_BASE_URL}${profilePicture}`}
                        alt="Profile"
                        className="w-24 h-24 rounded-full object-cover border-4 border-white/20"
                      />
                    ) : (
                      <div className="w-24 h-24 bg-white/20 rounded-full flex items-center justify-center border-4 border-white/20">
                        <User className="h-12 w-12 text-white" />
                      </div>
                    )}
                    <label className="absolute bottom-0 right-0 bg-white rounded-full p-2 cursor-pointer hover:bg-gray-100 transition-colors">
                      <Camera className="h-4 w-4 text-gray-600" />
                      <input
                        type="file"
                        accept="image/*"
                        onChange={handleFileChange}
                        className="hidden"
                      />
                    </label>
                  </div>
                  <h2 className="text-xl font-semibold mt-4">
                    {profile.firstName} {profile.lastName}
                  </h2>
                  <p className="text-primary-100">{profile.email}</p>
                </div>
              </div>

              {/* Profile Info */}
              <div className="p-6">
                <div className="space-y-4">
                  <div className="flex items-center space-x-3 text-gray-600 dark:text-gray-300">
                    <Calendar className="h-4 w-4" />
                    <span>Member since {user?.createdAt ? formatDate(user.createdAt) : 'Recently'}</span>
                  </div>
                  <div className="flex items-center space-x-3 text-gray-600 dark:text-gray-300">
                    <Mail className="h-4 w-4" />
                    <span>{profile.email}</span>
                  </div>
                  {profile.phone && (
                    <div className="flex items-center space-x-3 text-gray-600 dark:text-gray-300">
                      <Phone className="h-4 w-4" />
                      <span>{profile.phone}</span>
                    </div>
                  )}
                </div>

                {/* Upload Section */}
                {selectedFile && (
                  <div className="mt-6 p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-gray-600 dark:text-gray-300">
                        {selectedFile.name}
                      </span>
                      <div className="flex space-x-2">
                        <button
                          onClick={handleUpload}
                          disabled={uploading}
                          className="px-3 py-1 bg-primary-500 text-white text-sm rounded hover:bg-primary-600 disabled:opacity-50"
                        >
                          {uploading ? 'Uploading...' : 'Upload'}
                        </button>
                        <button
                          onClick={() => setSelectedFile(null)}
                          className="px-3 py-1 bg-gray-300 text-gray-700 text-sm rounded hover:bg-gray-400"
                        >
                          Cancel
                        </button>
                      </div>
                    </div>
                    {uploadError && <div className="text-red-600 text-sm mt-2">{uploadError}</div>}
                    {uploadSuccess && <div className="text-green-600 text-sm mt-2">{uploadSuccess}</div>}
                  </div>
                )}

                {/* QR Code Upload Section */}
                <div className="mt-6 p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
                  <div className="mb-2 text-sm font-semibold text-gray-700 dark:text-gray-200">Your QR Code (optional)</div>
                  {profile.qrCodeImage && !qrFile && (
                    <div className="mb-2 flex flex-col items-center">
                      <img
                        src={`${process.env.REACT_APP_BASE_URL}${profile.qrCodeImage}`}
                        alt="QR Code"
                        className="w-32 h-32 object-contain border border-gray-300 rounded mb-2 bg-white"
                      />
                      {editing && (
                        <button
                          onClick={handleQrRemove}
                          className="px-3 py-1 bg-red-500 text-white text-xs rounded hover:bg-red-600"
                        >
                          Remove QR Code
                        </button>
                      )}
                    </div>
                  )}
                  {editing && (
                    <div className="flex flex-col items-center">
                      <input
                        type="file"
                        accept="image/*"
                        onChange={handleQrFileChange}
                        className="mb-2"
                      />
                      {qrFile && (
                        <>
                          <img
                            src={URL.createObjectURL(qrFile)}
                            alt="QR Preview"
                            className="w-32 h-32 object-contain border border-gray-300 rounded mb-2 bg-white"
                          />
                          <button
                            onClick={handleQrUpload}
                            disabled={qrUploading}
                            className="px-3 py-1 bg-primary-500 text-white text-xs rounded hover:bg-primary-600 disabled:opacity-50"
                          >
                            {qrUploading ? 'Uploading...' : 'Upload QR Code'}
                          </button>
                        </>
                      )}
                      {qrUploadError && <div className="text-red-600 text-xs mt-2">{qrUploadError}</div>}
                      {qrUploadSuccess && <div className="text-green-600 text-xs mt-2">{qrUploadSuccess}</div>}
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* Main Content */}
          <div className="lg:col-span-2 space-y-8">
            {/* Stats Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div 
                className="bg-white dark:bg-gray-800 rounded-xl p-4 shadow-lg cursor-pointer hover:shadow-xl transition-all duration-200 hover:scale-105"
                onClick={() => navigate('/dashboard')}
              >
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Listings</p>
                    <p className="text-2xl font-bold text-gray-900 dark:text-white">{stats.listings}</p>
                  </div>
                  <div className="w-10 h-10 bg-blue-100 dark:bg-blue-900 rounded-lg flex items-center justify-center">
                    <Package className="h-5 w-5 text-blue-600 dark:text-blue-400" />
                  </div>
                </div>
              </div>

              <div 
                className="bg-white dark:bg-gray-800 rounded-xl p-4 shadow-lg cursor-pointer hover:shadow-xl transition-all duration-200 hover:scale-105"
                onClick={() => navigate('/messages')}
              >
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Messages</p>
                    <p className="text-2xl font-bold text-gray-900 dark:text-white">{stats.messages}</p>
                  </div>
                  <div className="w-10 h-10 bg-purple-100 dark:bg-purple-900 rounded-lg flex items-center justify-center">
                    <MessageCircle className="h-5 w-5 text-purple-600 dark:text-purple-400" />
                  </div>
                </div>
              </div>

              <div 
                className="bg-white dark:bg-gray-800 rounded-xl p-4 shadow-lg cursor-pointer hover:shadow-xl transition-all duration-200 hover:scale-105"
                onClick={() => navigate('/wishlist')}
              >
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Wishlist</p>
                    <p className="text-2xl font-bold text-gray-900 dark:text-white">{stats.wishlist}</p>
                  </div>
                  <div className="w-10 h-10 bg-red-100 dark:bg-red-900 rounded-lg flex items-center justify-center">
                    <Heart className="h-5 w-5 text-red-600 dark:text-red-400" />
                  </div>
                </div>
              </div>
            </div>

            {/* Profile Form */}
            <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6">
              <div className="flex items-center justify-between mb-6">
                <h3 className="text-xl font-semibold text-gray-900 dark:text-white">
                  Personal Information
                </h3>
                <button
                  onClick={() => setEditing(!editing)}
                  className="flex items-center space-x-2 px-4 py-2 bg-primary-500 text-white rounded-lg hover:bg-primary-600 transition-colors"
                >
                  {editing ? <X className="h-4 w-4" /> : <Edit className="h-4 w-4" />}
                  <span>{editing ? 'Cancel' : 'Edit'}</span>
                </button>
              </div>

              <div className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">
                      First Name
                    </label>
                    <input
                      type="text"
                      name="firstName"
                      value={profile.firstName}
                      onChange={handleChange}
                      className={`w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 transition-colors ${
                        editing 
                          ? 'border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white' 
                          : 'border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 text-gray-500 dark:text-gray-400'
                      }`}
                      readOnly={!editing}
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">
                      Last Name
                    </label>
                    <input
                      type="text"
                      name="lastName"
                      value={profile.lastName}
                      onChange={handleChange}
                      className={`w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 transition-colors ${
                        editing 
                          ? 'border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white' 
                          : 'border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 text-gray-500 dark:text-gray-400'
                      }`}
                      readOnly={!editing}
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">
                    Email
                  </label>
                  <input
                    type="email"
                    name="email"
                    value={profile.email}
                    className="w-full px-4 py-3 border border-gray-200 dark:border-gray-700 rounded-lg bg-gray-50 dark:bg-gray-800 text-gray-500 dark:text-gray-400"
                    readOnly
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">
                    Phone Number
                  </label>
                  <input
                    type="tel"
                    name="phone"
                    value={profile.phone}
                    onChange={handleChange}
                    className={`w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 transition-colors ${
                      editing 
                        ? 'border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white' 
                        : 'border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 text-gray-500 dark:text-gray-400'
                    }`}
                    readOnly={!editing}
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">
                    Bio
                  </label>
                  <textarea
                    name="bio"
                    value={profile.bio}
                    onChange={handleChange}
                    rows={4}
                    className={`w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 transition-colors ${
                      editing 
                        ? 'border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white' 
                        : 'border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 text-gray-500 dark:text-gray-400'
                    }`}
                    readOnly={!editing}
                    placeholder="Tell us about yourself..."
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">
                    Address (optional)
                  </label>
                  <input
                    type="text"
                    name="address"
                    value={profile.address}
                    onChange={handleChange}
                    className={`w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 transition-colors ${
                      editing 
                        ? 'border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white' 
                        : 'border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 text-gray-500 dark:text-gray-400'
                    }`}
                    readOnly={!editing}
                    placeholder="Enter your address (optional)"
                  />
                </div>

                {editing && (
                  <div className="flex justify-end space-x-4">
                    <button
                      onClick={() => setEditing(false)}
                      className="px-6 py-2 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
                    >
                      Cancel
                    </button>
                    <button
                      onClick={handleSave}
                      disabled={loading}
                      className="px-6 py-2 bg-primary-500 text-white rounded-lg hover:bg-primary-600 disabled:opacity-50 transition-colors flex items-center space-x-2"
                    >
                      {loading ? (
                        <>
                          <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                          <span>Saving...</span>
                        </>
                      ) : (
                        <>
                          <Save className="h-4 w-4" />
                          <span>Save Changes</span>
                        </>
                      )}
                    </button>
                  </div>
                )}
              </div>

              {/* Messages */}
              {success && (
                <div className="mt-4 p-4 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg">
                  <div className="flex items-center">
                    <div className="w-5 h-5 bg-green-500 rounded-full flex items-center justify-center mr-3">
                      <div className="w-2 h-2 bg-white rounded-full"></div>
                    </div>
                    <span className="text-green-800 dark:text-green-200">{success}</span>
                  </div>
                </div>
              )}
              
              {error && (
                <div className="mt-4 p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
                  <div className="flex items-center">
                    <div className="w-5 h-5 bg-red-500 rounded-full flex items-center justify-center mr-3">
                      <div className="w-2 h-2 bg-white rounded-full"></div>
                    </div>
                    <span className="text-red-800 dark:text-red-200">{error}</span>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile; 
