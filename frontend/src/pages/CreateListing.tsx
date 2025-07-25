import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { 
  Upload, 
  Camera, 
  X, 
  Check, 
  ArrowRight, 
  ArrowLeft, 
  Tag, 
  FileText, 
  Image as ImageIcon,
  Eye,
  Settings,
  Sparkles,
  TrendingUp,
  Package,
  MapPin,
  Clock,
  Star,
  Plus,
  Minus,
  AlertCircle,
  CheckCircle
} from 'lucide-react';

interface UploadingImage {
  file: File;
  progress: number;
  url?: string;
  error?: string;
}

interface ListingPreview {
  title: string;
  price: number;
  description: string;
  category: string;
  condition: string;
  images: string[];
  negotiable: boolean;
}

const CreateListing: React.FC = () => {
  const [currentStep, setCurrentStep] = useState(1);
  const [title, setTitle] = useState('');
  const [price, setPrice] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState('Books');
  const [condition, setCondition] = useState('NEW');
  const [location, setLocation] = useState('');
  const [images, setImages] = useState<string[]>([]);
  const [uploadingImages, setUploadingImages] = useState<UploadingImage[]>([]);
  const [success, setSuccess] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [categories, setCategories] = useState<string[]>([]);
  const [showPreview, setShowPreview] = useState(false);
  const [isDonation, setIsDonation] = useState(false);
  const [lifeOfItem, setLifeOfItem] = useState('');

  useEffect(() => {
    axios.get('http://localhost:8080/api/listings/categories')
      .then(res => {
        setCategories(res.data);
        if (res.data.length > 0) setCategory(res.data[0]);
      })
      .catch(() => setCategories([]));
  }, []);

  const handleImageChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files || e.target.files.length === 0) return;
    setError(null);
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
      setImages(prev => [...prev, res.data]);
      setUploadingImages(prev => prev.map((img, i) => i === index ? { ...img, url: res.data, progress: 100 } : img));
    } catch (err: any) {
      setUploadingImages(prev => prev.map((img, i) => i === index ? { ...img, error: 'Failed to upload' } : img));
      setError('Failed to upload one or more images.');
    }
  };

  const handleRemoveImage = (url: string) => {
    setImages(prev => prev.filter(img => img !== url));
    setUploadingImages(prev => prev.filter(img => img.url !== url));
  };

  const handleDonationChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setIsDonation(e.target.checked);
    if (e.target.checked) {
      setPrice('0');
    }
  };


  const allUploadsDone = uploadingImages.every(img => img.url || img.error);

  const canProceedToNext = () => {
    switch (currentStep) {
      case 1:
        return title.trim() && price && description.trim().length >= 10;
      case 2:
        return category && condition;
      case 3:
        return images.length > 0;
      case 4:
        return true;
      default:
        return false;
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSuccess(null);
    setError(null);
    setLoading(true);
    
    const token = localStorage.getItem('accessToken');
    if (!token || token.split('.').length !== 3) {
      setError('You must be logged in with a valid account to create a listing.');
      setLoading(false);
      return;
    }
    
    try {
      const res = await axios.post(
        '/api/listings',
        {
          title,
          price: parseFloat(price),
          description,
          category,
          condition,
          images,
          location,
          isDonation,
          lifeOfItem,
        },
        {
          baseURL: 'http://localhost:8080',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
          },
        }
      );
      setSuccess('Listing created successfully!');
      // Reset form
      setTitle('');
      setPrice('');
      setDescription('');
      setCategory('Books');
              setCondition('NEW');
        setLocation('');
        setImages([]);
      setUploadingImages([]);
      setCurrentStep(1);
      setLifeOfItem('');
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to create listing.');
    } finally {
      setLoading(false);
    }
  };

  const steps = [
    { number: 1, title: 'Basic Info', icon: FileText },
    { number: 2, title: 'Details', icon: Settings },
    { number: 3, title: 'Photos', icon: Camera },
    { number: 4, title: 'Review', icon: Eye }
  ];

  const renderStepContent = () => {
    switch (currentStep) {
      case 1:
        return (
          <div className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">
                Listing Title *
              </label>
              <input 
                type="text" 
                value={title} 
                onChange={e => setTitle(e.target.value)} 
                className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 dark:bg-gray-700 dark:text-white"
                placeholder="What are you selling?"
                required 
              />
              <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                Be specific and descriptive
              </p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">
                Life of the Item (How old is it?)
              </label>
              <input
                type="text"
                value={lifeOfItem}
                onChange={e => setLifeOfItem(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 dark:bg-gray-700 dark:text-white"
                placeholder="e.g. 2 years, 6 months, brand new, etc."
              />
            </div>
            <div className="flex items-center space-x-3 mt-2">
              <input
                id="donation-checkbox"
                type="checkbox"
                checked={isDonation}
                onChange={handleDonationChange}
                className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
              />
              <label htmlFor="donation-checkbox" className="text-sm font-medium text-gray-700 dark:text-gray-200">
                This item is for donation (free)
              </label>
            </div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">
              Price *
            </label>
            <div className="relative">
              <span className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 font-medium text-lg">₹</span>
              <input 
                type="number" 
                value={price} 
                onChange={e => setPrice(e.target.value)} 
                className="w-full pl-10 pr-16 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 dark:bg-gray-700 dark:text-white"
                placeholder="0.00"
                required 
                min="0.00" 
                step="0.01" 
                disabled={isDonation}
              />
              <div className="absolute right-1 top-1/2 transform -translate-y-1/2 flex flex-col">
                  <button
                    type="button"
                    onClick={() => {
                      const currentPrice = parseFloat(price) || 0;
                      setPrice((currentPrice + 1).toFixed(2));
                    }}
                    className="w-6 h-6 flex items-center justify-center text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-600 rounded-t transition-colors"
                  >
                    <Plus className="h-3 w-3" />
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      const currentPrice = parseFloat(price) || 0;
                      if (currentPrice > 1) {
                        setPrice((currentPrice - 1).toFixed(2));
                      }
                    }}
                    className="w-6 h-6 flex items-center justify-center text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-600 rounded-b transition-colors"
                  >
                    <Minus className="h-3 w-3" />
                  </button>
                </div>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">
                Description *
              </label>
              <textarea 
                value={description} 
                onChange={e => setDescription(e.target.value)} 
                className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 dark:bg-gray-700 dark:text-white"
                rows={4}
                placeholder="Describe your item in detail..."
                required 
              />
              <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                {description.length}/1000 characters (minimum 10)
              </p>
            </div>
          </div>
        );
      
      case 2:
        return (
          <div className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">
                Category *
              </label>
              <select 
                value={category} 
                onChange={e => setCategory(e.target.value)} 
                className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 dark:bg-gray-700 dark:text-white"
              >
                {categories.map(cat => (
                  <option key={cat} value={cat}>{cat}</option>
                ))}
              </select>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">
                Condition *
              </label>
              <select 
                value={condition} 
                onChange={e => setCondition(e.target.value)} 
                className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 dark:bg-gray-700 dark:text-white"
              >
                <option value="NEW">New</option>
                <option value="LIKE_NEW">Like New</option>
                <option value="EXCELLENT">Excellent</option>
                <option value="GOOD">Good</option>
                <option value="FAIR">Fair</option>
                <option value="POOR">Poor</option>
              </select>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">
                Location
              </label>
              <div className="relative">
                <MapPin className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
                <input 
                  type="text" 
                  value={location} 
                  onChange={e => setLocation(e.target.value)} 
                  className="w-full pl-10 pr-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 dark:bg-gray-700 dark:text-white"
                  placeholder="Where can buyers pick up the item?"
                />
              </div>
            </div>
            

            

          </div>
        );
      
      case 3:
        return (
          <div className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">
                Photos *
              </label>
              <div className="border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg p-6 text-center">
                <Camera className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                <p className="text-gray-600 dark:text-gray-400 mb-2">
                  Upload photos of your item
                </p>
                <p className="text-sm text-gray-500 dark:text-gray-500 mb-4">
                  First photo will be the main image
                </p>
                <input 
                  type="file" 
                  accept="image/*" 
                  multiple 
                  onChange={handleImageChange} 
                  disabled={uploadingImages.some(img => !img.url && !img.error)}
                  className="hidden"
                  id="image-upload"
                />
                <label 
                  htmlFor="image-upload"
                  className="bg-primary-500 hover:bg-primary-600 text-white px-6 py-3 rounded-lg cursor-pointer transition-colors inline-block"
                >
                  <Upload className="h-5 w-5 inline mr-2" />
                  Choose Photos
                </label>
              </div>
            </div>
            
            {uploadingImages.length > 0 && (
              <div>
                <h4 className="text-sm font-medium text-gray-700 dark:text-gray-200 mb-3">
                  Uploading Images
                </h4>
                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
                  {uploadingImages.map((img, idx) => (
                    <div key={idx} className="relative">
                      {img.url ? (
                        <div className="relative">
                          <img 
                            src={`http://localhost:8080${img.url}`} 
                            alt="Listing" 
                            className="w-full h-24 object-cover rounded-lg" 
                          />
                          <button
                            type="button"
                            className="absolute top-1 right-1 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-xs hover:bg-red-600 transition-colors"
                            onClick={() => handleRemoveImage(img.url!)}
                          >
                            <X className="h-3 w-3" />
                          </button>
                        </div>
                      ) : (
                        <div className="w-full h-24 bg-gray-200 dark:bg-gray-700 rounded-lg flex items-center justify-center">
                          {img.progress < 100 && !img.error ? (
                            <div className="text-center">
                              <div className="text-xs text-blue-600 dark:text-blue-400 mb-1">{img.progress}%</div>
                              <div className="w-8 h-1 bg-gray-300 dark:bg-gray-600 rounded-full overflow-hidden">
                                <div 
                                  className="h-full bg-blue-500 transition-all duration-300"
                                  style={{ width: `${img.progress}%` }}
                                />
                              </div>
                            </div>
                          ) : img.error ? (
                            <div className="text-center">
                              <AlertCircle className="h-6 w-6 text-red-500 mx-auto mb-1" />
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
        );
      
      case 4:
        return (
          <div className="space-y-6">
            <div className="bg-gray-50 dark:bg-gray-800 rounded-lg p-6">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
                Listing Preview
              </h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <h4 className="font-medium text-gray-900 dark:text-white mb-2">{title}</h4>
                  <p className="text-2xl font-bold text-primary-600 dark:text-primary-400 mb-2">
                    ₹{parseFloat(price).toLocaleString()}
                  </p>
                  <p className="text-gray-600 dark:text-gray-300 text-sm mb-4">{description}</p>
                  <div className="space-y-2 text-sm text-gray-500 dark:text-gray-400">
                    <div className="flex items-center">
                      <Package className="h-4 w-4 mr-2" />
                      {category}
                    </div>
                    <div className="flex items-center">
                      <Star className="h-4 w-4 mr-2" />
                      {condition}
                    </div>
                    {location && (
                      <div className="flex items-center">
                        <MapPin className="h-4 w-4 mr-2" />
                        {location}
                      </div>
                    )}
                  </div>
                </div>
                <div>
                  {images.length > 0 && (
                    <img 
                      src={`http://localhost:8080${images[0]}`} 
                      alt="Main image" 
                      className="w-full h-48 object-cover rounded-lg" 
                    />
                  )}
                </div>
              </div>
            </div>
            
            <div className="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
              <div className="flex items-start">
                <Sparkles className="h-5 w-5 text-blue-600 dark:text-blue-400 mr-3 mt-0.5" />
                <div>
                  <h4 className="font-medium text-blue-900 dark:text-blue-100 mb-1">
                    Tips for a successful listing
                  </h4>
                  <ul className="text-sm text-blue-800 dark:text-blue-200 space-y-1">
                    <li>• Use clear, well-lit photos</li>
                    <li>• Write a detailed description</li>
                    <li>• Set a competitive price</li>
                    <li>• Respond quickly to inquiries</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        );
      
      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-gray-800 pt-16">
      <div className="max-w-4xl mx-auto py-8 px-4">
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg overflow-hidden">
          {/* Header */}
          <div className="bg-gradient-to-r from-primary-600 to-primary-700 p-6 text-white">
            <h1 className="text-2xl font-bold mb-2">Create New Listing</h1>
            <p className="text-primary-100">Sell your items to the JNU community</p>
          </div>

          {/* Progress Steps */}
          <div className="p-6 border-b border-gray-200 dark:border-gray-700">
            <div className="flex items-center justify-between">
              {steps.map((step, index) => {
                const Icon = step.icon;
                const isActive = currentStep === step.number;
                const isCompleted = currentStep > step.number;
                
                return (
                  <div key={step.number} className="flex items-center">
                    <div className={`flex items-center justify-center w-10 h-10 rounded-full border-2 ${
                      isActive 
                        ? 'bg-primary-500 border-primary-500 text-white' 
                        : isCompleted 
                          ? 'bg-green-500 border-green-500 text-white'
                          : 'bg-gray-200 dark:bg-gray-700 border-gray-300 dark:border-gray-600 text-gray-500 dark:text-gray-400'
                    }`}>
                      {isCompleted ? (
                        <Check className="h-5 w-5" />
                      ) : (
                        <Icon className="h-5 w-5" />
                      )}
                    </div>
                    <div className="ml-3">
                      <div className={`text-sm font-medium ${
                        isActive ? 'text-primary-600 dark:text-primary-400' : 'text-gray-500 dark:text-gray-400'
                      }`}>
                        {step.title}
                      </div>
                    </div>
                    {index < steps.length - 1 && (
                      <div className={`w-16 h-0.5 mx-4 ${
                        isCompleted ? 'bg-green-500' : 'bg-gray-300 dark:bg-gray-600'
                      }`} />
                    )}
                  </div>
                );
              })}
            </div>
          </div>

          {/* Form Content */}
          <div className="p-6">
            {renderStepContent()}
            
            {/* Navigation */}
            <div className="flex items-center justify-between mt-8 pt-6 border-t border-gray-200 dark:border-gray-700">
              <button
                type="button"
                onClick={() => setCurrentStep(prev => Math.max(1, prev - 1))}
                disabled={currentStep === 1}
                className="flex items-center px-4 py-2 text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-200 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                <ArrowLeft className="h-4 w-4 mr-2" />
                Previous
              </button>
              
              <div className="flex items-center space-x-3">
                {currentStep < 4 ? (
                  <button
                    type="button"
                    onClick={() => setCurrentStep(prev => prev + 1)}
                    disabled={!canProceedToNext()}
                    className="flex items-center px-6 py-2 bg-primary-500 text-white rounded-lg hover:bg-primary-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                  >
                    Next
                    <ArrowRight className="h-4 w-4 ml-2" />
                  </button>
                ) : (
                  <button
                    type="submit"
                    onClick={handleSubmit}
                    disabled={loading || !allUploadsDone}
                    className="flex items-center px-6 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                  >
                    {loading ? (
                      <>
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2" />
                        Creating...
                      </>
                    ) : (
                      <>
                        <CheckCircle className="h-4 w-4 mr-2" />
                        Create Listing
                      </>
                    )}
                  </button>
                )}
              </div>
            </div>
            
            {/* Messages */}
            {success && (
              <div className="mt-4 p-4 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg">
                <div className="flex items-center">
                  <CheckCircle className="h-5 w-5 text-green-600 dark:text-green-400 mr-3" />
                  <span className="text-green-800 dark:text-green-200">{success}</span>
                </div>
              </div>
            )}
            
            {error && (
              <div className="mt-4 p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
                <div className="flex items-center">
                  <AlertCircle className="h-5 w-5 text-red-600 dark:text-red-400 mr-3" />
                  <span className="text-red-800 dark:text-red-200">{error}</span>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default CreateListing; 