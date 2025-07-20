import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { 
  Search, ArrowRight, Book, Monitor, Sofa, Shirt, Trophy, Wrench, GraduationCap, Truck, Utensils, Package, MessageCircle, Music, Car, Palette, Sparkles, Home as HomeIcon, ToyBrick, HeartPulse, BookOpen, Star, UserCheck
} from 'lucide-react';
import axios from 'axios';

const iconMap: Record<string, any> = {
  'Books': Book,
  'Electronics': Monitor,
  'Furniture': Sofa,
  'Clothing': Shirt,
  'Sports & Fitness': Trophy,
  'Musical Instruments': Music,
  'Vehicles': Car,
  'Services': Wrench,
  'Food & Beverages': Utensils,
  'Art & Collectibles': Palette,
  'Beauty & Personal Care': Sparkles,
  'Home & Garden': HomeIcon,
  'Toys & Games': ToyBrick,
  'Health & Wellness': HeartPulse,
  'Education & Training': BookOpen,
  'Other': Package,
};
const colorMap: Record<string, string> = {
  'Books': 'bg-blue-500',
  'Electronics': 'bg-green-500',
  'Furniture': 'bg-yellow-500',
  'Clothing': 'bg-purple-500',
  'Sports & Fitness': 'bg-red-500',
  'Musical Instruments': 'bg-pink-500',
  'Vehicles': 'bg-orange-500',
  'Services': 'bg-indigo-500',
  'Food & Beverages': 'bg-teal-500',
  'Art & Collectibles': 'bg-rose-500',
  'Beauty & Personal Care': 'bg-fuchsia-500',
  'Home & Garden': 'bg-lime-500',
  'Toys & Games': 'bg-cyan-500',
  'Health & Wellness': 'bg-emerald-500',
  'Education & Training': 'bg-sky-500',
  'Other': 'bg-gray-500',
};

const getCategoryDescription = (category: string): string => {
  const descriptions: Record<string, string> = {
    'Books': 'Textbooks, novels, and academic resources',
    'Electronics': 'Laptops, phones, and gadgets',
    'Furniture': 'Chairs, tables, and home decor',
    'Clothing': 'Fashion, accessories, and shoes',
    'Sports & Fitness': 'Equipment, gear, and activewear',
    'Musical Instruments': 'Guitars, pianos, and accessories',
    'Vehicles': 'Cars, bikes, and transportation',
    'Services': 'Tutoring, repairs, and professional help',
    'Food & Beverages': 'Homemade food and beverages',
    'Art & Collectibles': 'Paintings, crafts, and rare items',
    'Beauty & Personal Care': 'Cosmetics, skincare, and wellness',
    'Home & Garden': 'Plants, tools, and household items',
    'Toys & Games': 'Board games, toys, and entertainment',
    'Health & Wellness': 'Supplements, fitness, and wellness',
    'Education & Training': 'Courses, workshops, and learning',
    'Other': 'Miscellaneous items and services',
  };
  return descriptions[category] || 'Various items and services';
};

const jnuImages = [
  '/jnu.jpg', // existing image
  '/jnu1.jpg',
  '/jnu2.jpg',
  '/jnu3.jpg',
  '/jnu4.jpg',
  '/jnu5.jpg',
];

const Home: React.FC = () => {
  const { user } = useAuth();
  const [categories, setCategories] = useState<string[]>([]);
  const [conditions, setConditions] = useState<string[]>([]);
  const [listings, setListings] = useState<any[]>([]);
  const [filters, setFilters] = useState({ category: '', minPrice: '', maxPrice: '', condition: '' });
  const [searchQuery, setSearchQuery] = useState('');
  const navigate = useNavigate();
  const [currentImage, setCurrentImage] = useState(0);
  const [imagesLoaded, setImagesLoaded] = useState(false);

  // Preload images
  useEffect(() => {
    let isMounted = true;
    let loaded = 0;
    jnuImages.forEach((src) => {
      const img = new window.Image();
      img.src = src;
      img.onload = () => {
        loaded++;
        if (loaded === jnuImages.length && isMounted) {
          setImagesLoaded(true);
        }
      };
      img.onerror = () => {
        loaded++;
        if (loaded === jnuImages.length && isMounted) {
          setImagesLoaded(true);
        }
      };
    });
    return () => { isMounted = false; };
  }, []);

  // Start slideshow only after images are loaded
  useEffect(() => {
    if (!imagesLoaded) return;
    const interval = setInterval(() => {
      setCurrentImage((prev) => (prev + 1) % jnuImages.length);
    }, 3000); // 3 seconds
    return () => clearInterval(interval);
  }, [imagesLoaded]);

  useEffect(() => {
    axios.get('http://localhost:8080/api/listings/categories')
      .then(res => setCategories(res.data))
      .catch(() => setCategories([]));
    axios.get('http://localhost:8080/api/listings/conditions')
      .then(res => setConditions(res.data))
      .catch(() => setConditions([]));
    fetchListings();
  }, []);

  const fetchListings = (filterParams = filters) => {
    axios.post('http://localhost:8080/api/listings/search', {
      category: filterParams.category ? filterParams.category.toUpperCase() : undefined,
      minPrice: filterParams.minPrice ? parseFloat(filterParams.minPrice) : undefined,
      maxPrice: filterParams.maxPrice ? parseFloat(filterParams.maxPrice) : undefined,
      condition: filterParams.condition ? filterParams.condition.toUpperCase() : undefined,
    }, { params: { page: 0, size: 20 } })
      .then(res => setListings(res.data.content || []))
      .catch(() => setListings([]));
  };

  const handleFilterChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    const newFilters = { ...filters, [name]: value };
    setFilters(newFilters);
    fetchListings(newFilters);
  };

  return (
    <div className="min-h-screen pt-16">
      {/* Hero Section with Slideshow */}
      <section
        className="relative text-white bg-gray-900" // fallback bg
        style={{
          minHeight: '440px',
          maxHeight: '520px',
          height: '48vw',
          borderRadius: '1rem',
          border: '2px solid #fff',
          overflow: 'hidden',
        }}
      >
        {/* Static background image (always visible) */}
        <img
          src={jnuImages[0]}
          alt="JNU view 1"
          className="absolute inset-0 w-full h-full object-cover"
          style={{ borderRadius: '1rem', zIndex: 0 }}
          draggable={false}
        />
        {/* Slideshow Images (layered on top) */}
        <div className="absolute inset-0 w-full h-full" style={{ borderRadius: '1rem', zIndex: 1 }}>
          {jnuImages.map((img, idx) => (
            idx === 0 ? null : (
              <img
                key={img}
                src={img}
                alt={`JNU view ${idx + 1}`}
                className={`absolute inset-0 w-full h-full object-cover transition-opacity duration-1000 ${currentImage === idx && imagesLoaded ? 'opacity-100' : 'opacity-0'}`}
                style={{ borderRadius: '1rem', transitionProperty: 'opacity' }}
                draggable={false}
              />
            )
          ))}
        </div>
        {/* Overlay */}
        <div className="absolute inset-0 bg-black z-10" style={{ opacity: 0.35, borderRadius: '1rem' }} />
        {/* Hero Content (always on top) */}
        <div className="relative z-20 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 flex flex-col items-center justify-center">
          <h1 className="text-4xl md:text-6xl font-bold mb-6 drop-shadow-lg">
            Welcome to the BAZAAR
          </h1>
          <p className="text-xl md:text-2xl mb-8 text-primary-100 drop-shadow">
            Buy, sell, and trade within your campus or local community
          </p>
          {/* Search Bar */}
          <div className="max-w-2xl mx-auto mb-8 w-full">
            <form className="flex" onSubmit={e => {
              e.preventDefault();
              if (searchQuery.trim()) {
                navigate(`/search?query=${encodeURIComponent(searchQuery.trim())}`);
              }
            }}>
              <input
                type="text"
                placeholder="Search for items, services, or categories..."
                className="flex-1 px-6 py-4 text-gray-900 rounded-l-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
              />
              <button
                type="submit"
                className="px-8 py-4 bg-primary-700 hover:bg-primary-800 rounded-r-lg transition-colors flex items-center space-x-2"
              >
                <Search className="h-5 w-5" />
                <span>Search</span>
              </button>
            </form>
          </div>
          {/* CTA Buttons */}
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              to={user ? "/create-listing" : "/register"}
              className="bg-white text-primary-600 px-8 py-3 rounded-lg font-semibold hover:bg-gray-100 transition-colors"
            >
              {user ? "Start Selling" : "Get Started"}
            </Link>
            <Link
              to="/search"
              className="border-2 border-white text-white px-8 py-3 rounded-lg font-semibold hover:bg-white hover:text-primary-600 transition-colors"
            >
              Browse Items
            </Link>
          </div>
        </div>
      </section>

      {/* Categories Section */}
      <section className="py-20 bg-gradient-to-br from-gray-50 to-blue-50 dark:from-gray-900 dark:to-gray-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-gray-900 dark:text-white mb-4">
              Explore Categories
            </h2>
            <p className="text-xl text-gray-600 dark:text-gray-300 max-w-3xl mx-auto">
              Discover amazing items and services across our diverse categories. 
              From textbooks to electronics, find exactly what you need.
            </p>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-6">
            {categories.map((name) => {
              const Icon = iconMap[name] || Package;
              const color = colorMap[name] || 'bg-gray-400';
              const description = getCategoryDescription(name);
              return (
                <Link
                  key={name}
                  to={`/search?category=${encodeURIComponent(name)}`}
                  className="group"
                >
                  <div className="bg-white dark:bg-gray-800 rounded-xl p-6 text-center hover:shadow-xl transition-all duration-300 transform hover:-translate-y-2 border border-gray-100 dark:border-gray-700">
                    <div className={`${color} w-16 h-16 rounded-xl flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform duration-300 shadow-lg`}>
                      <Icon className="h-8 w-8 text-white" />
                    </div>
                    <h3 className="font-bold text-gray-900 dark:text-white group-hover:text-primary-600 dark:group-hover:text-primary-400 transition-colors text-lg mb-2">
                      {name}
                    </h3>
                    <p className="text-sm text-gray-500 dark:text-gray-400 group-hover:text-gray-700 dark:group-hover:text-gray-300 transition-colors">
                      {description}
                    </p>
                    <div className="mt-4 opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                      <div className="w-full bg-gray-200 rounded-full h-1">
                        <div className={`${color.replace('bg-', 'bg-')} h-1 rounded-full transition-all duration-300`} style={{width: '0%'}}></div>
                      </div>
                    </div>
                  </div>
                </Link>
              );
            })}
          </div>

          {/* Featured Categories Banner */}
          <div className="mt-16 bg-gradient-to-r from-primary-600 to-primary-700 rounded-2xl p-8 text-white">
            <div className="grid md:grid-cols-2 gap-8 items-center">
              <div>
                <h3 className="text-2xl font-bold mb-4">
                  Popular This Week
                </h3>
                <p className="text-primary-100 mb-6">
                  Check out the most trending categories and discover amazing deals from your fellow students.
                </p>
                <div className="flex flex-wrap gap-3">
                  {categories.slice(0, 4).map((category) => (
                    <span key={category} className="bg-white bg-opacity-20 px-3 py-1 rounded-full text-sm">
                      {category}
                    </span>
                  ))}
                </div>
              </div>
              <div className="text-center">
                <div className="w-24 h-24 bg-white bg-opacity-20 rounded-full flex items-center justify-center mx-auto mb-4">
                  <Star className="h-12 w-12 text-white" />
                </div>
                <p className="text-primary-100 font-semibold">
                  Trending Now
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Main Content with Sidebar */}
      {/* Removed filter sidebar and listings grid. */}

      {/* Features Section */}
      <section className="py-16 bg-white dark:bg-gray-900">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-4">
              Why Choose Our BAZAAR?
            </h2>
            <p className="text-lg text-gray-600 dark:text-gray-300">
              Built for students, faculty, and local residents
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            <div className="text-center bg-gray-50 dark:bg-gray-800 rounded-lg p-6">
              <div className="w-16 h-16 bg-primary-100 dark:bg-primary-900 rounded-full flex items-center justify-center mx-auto mb-4">
                <Search className="h-8 w-8 text-primary-600" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-2">
                Easy Discovery
              </h3>
              <p className="text-gray-600 dark:text-gray-300">
                Find exactly what you need with our advanced search and filtering options.
              </p>
            </div>

            <div className="text-center bg-gray-50 dark:bg-gray-800 rounded-lg p-6">
              <div className="w-16 h-16 bg-yellow-100 dark:bg-yellow-900 rounded-full flex items-center justify-center mx-auto mb-4">
                <UserCheck className="h-8 w-8 text-yellow-600" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-2">
                Verified Users
              </h3>
              <p className="text-gray-600 dark:text-gray-300">
                All users are verified for a safer bazaar experience.
              </p>
            </div>

            <div className="text-center bg-gray-50 dark:bg-gray-800 rounded-lg p-6">
              <div className="w-16 h-16 bg-purple-100 dark:bg-purple-900 rounded-full flex items-center justify-center mx-auto mb-4">
                <MessageCircle className="h-8 w-8 text-purple-600" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-2">
                Direct Communication
              </h3>
              <p className="text-gray-600 dark:text-gray-300">
                Chat directly with buyers and sellers within the platform.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-16 bg-primary-600 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-3xl font-bold mb-4">
            Ready to Start Trading?
          </h2>
          <p className="text-xl text-primary-100 mb-8">
            Join thousands of JNU students, faculty, and staff in our bazaar
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              to={user ? "/create-listing" : "/register"}
              className="bg-white text-primary-600 px-8 py-3 rounded-lg font-semibold hover:bg-gray-100 transition-colors flex items-center justify-center space-x-2"
            >
              <span>{user ? "Start Selling" : "Get Started"}</span>
              <ArrowRight className="h-5 w-5" />
            </Link>
            <Link
              to={user ? "/dashboard" : "/about"}
              className="border-2 border-white text-white px-8 py-3 rounded-lg font-semibold hover:bg-white hover:text-primary-600 transition-colors"
            >
              {user ? "Dashboard" : "Learn More"}
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
};

// Temporary Shield component since it's not in lucide-react
const Shield: React.FC<{ className?: string }> = ({ className }) => (
  <svg className={className} fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
  </svg>
);

export default Home; 