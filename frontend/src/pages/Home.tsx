import React from 'react';
import { Link } from 'react-router-dom';
import { Search, ArrowRight, Book, Monitor, Couch, Shirt, Trophy, Wrench, GraduationCap, Truck, Utensils, Package } from 'lucide-react';

const Home: React.FC = () => {
  const categories = [
    { name: 'Books', icon: Book, color: 'bg-blue-500' },
    { name: 'Electronics', icon: Monitor, color: 'bg-green-500' },
    { name: 'Furniture', icon: Couch, color: 'bg-yellow-500' },
    { name: 'Clothing', icon: Shirt, color: 'bg-purple-500' },
    { name: 'Sports', icon: Trophy, color: 'bg-red-500' },
    { name: 'Services', icon: Wrench, color: 'bg-indigo-500' },
    { name: 'Tutoring', icon: GraduationCap, color: 'bg-pink-500' },
    { name: 'Transportation', icon: Truck, color: 'bg-orange-500' },
    { name: 'Food', icon: Utensils, color: 'bg-teal-500' },
    { name: 'Other', icon: Package, color: 'bg-gray-500' },
  ];

  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <section className="bg-gradient-to-r from-primary-600 to-primary-800 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
          <div className="text-center">
            <h1 className="text-4xl md:text-6xl font-bold mb-6">
              Welcome to JNU Marketplace
            </h1>
            <p className="text-xl md:text-2xl mb-8 text-primary-100">
              Buy, sell, and trade within the JNU community
            </p>
            
            {/* Search Bar */}
            <div className="max-w-2xl mx-auto mb-8">
              <form className="flex">
                <input
                  type="text"
                  placeholder="Search for items, services, or categories..."
                  className="flex-1 px-6 py-4 text-gray-900 rounded-l-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
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
                to="/register"
                className="bg-white text-primary-600 px-8 py-3 rounded-lg font-semibold hover:bg-gray-100 transition-colors"
              >
                Start Selling
              </Link>
              <Link
                to="/search"
                className="border-2 border-white text-white px-8 py-3 rounded-lg font-semibold hover:bg-white hover:text-primary-600 transition-colors"
              >
                Browse Items
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Categories Section */}
      <section className="py-16 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-gray-900 mb-4">
              Explore Categories
            </h2>
            <p className="text-lg text-gray-600">
              Find what you're looking for in our diverse categories
            </p>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-5 gap-6">
            {categories.map((category) => (
              <Link
                key={category.name}
                to={`/search?category=${category.name}`}
                className="group"
              >
                <div className="bg-white rounded-lg p-6 text-center hover:shadow-lg transition-shadow">
                  <div className={`${category.color} w-12 h-12 rounded-lg flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform`}>
                    <category.icon className="h-6 w-6 text-white" />
                  </div>
                  <h3 className="font-semibold text-gray-900 group-hover:text-primary-600 transition-colors">
                    {category.name}
                  </h3>
                </div>
              </Link>
            ))}
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-gray-900 mb-4">
              Why Choose JNU Marketplace?
            </h2>
            <p className="text-lg text-gray-600">
              Built specifically for the JNU community
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            <div className="text-center">
              <div className="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <Search className="h-8 w-8 text-primary-600" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">
                Easy Discovery
              </h3>
              <p className="text-gray-600">
                Find exactly what you need with our advanced search and filtering options.
              </p>
            </div>

            <div className="text-center">
              <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <Shield className="h-8 w-8 text-green-600" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">
                Secure & Trusted
              </h3>
              <p className="text-gray-600">
                JNU email verification ensures a safe and trusted community marketplace.
              </p>
            </div>

            <div className="text-center">
              <div className="w-16 h-16 bg-purple-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <MessageCircle className="h-8 w-8 text-purple-600" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">
                Direct Communication
              </h3>
              <p className="text-gray-600">
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
            Join thousands of JNU students, faculty, and staff in our marketplace
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              to="/register"
              className="bg-white text-primary-600 px-8 py-3 rounded-lg font-semibold hover:bg-gray-100 transition-colors flex items-center justify-center space-x-2"
            >
              <span>Get Started</span>
              <ArrowRight className="h-5 w-5" />
            </Link>
            <Link
              to="/about"
              className="border-2 border-white text-white px-8 py-3 rounded-lg font-semibold hover:bg-white hover:text-primary-600 transition-colors"
            >
              Learn More
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