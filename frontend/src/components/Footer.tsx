import React from 'react';
import { Link } from 'react-router-dom';
import { Mail, Phone, MapPin, Facebook, Twitter, Instagram } from 'lucide-react';

const Footer: React.FC = () => {
  return (
    <footer className="bg-gray-900 text-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Brand Section */}
          <div className="col-span-1 md:col-span-2">
            <div className="flex items-center space-x-2 mb-4">
              <div className="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-sm">JNU</span>
              </div>
              <span className="text-xl font-bold">BAZAAR</span>
            </div>
            <p className="text-gray-300 mb-4 max-w-md">
              The official bazaar for Jawaharlal Nehru University community. 
              Buy, sell, and trade items and services within the campus ecosystem.
            </p>
            <div className="flex space-x-4">
              <span className="text-gray-400 text-sm">
                Connect with us on social media
              </span>
            </div>
          </div>

          {/* Quick Links */}
          <div>
            <h3 className="text-lg font-semibold mb-4">Quick Links</h3>
            <ul className="space-y-2">
              <li>
                <Link to="/" className="text-gray-300 hover:text-white transition-colors">
                  Home
                </Link>
              </li>
              <li>
                <Link to="/search" className="text-gray-300 hover:text-white transition-colors">
                  Browse Items
                </Link>
              </li>
              <li>
                <Link to="/create-listing" className="text-gray-300 hover:text-white transition-colors">
                  Sell Items
                </Link>
              </li>
            </ul>
          </div>

          {/* Contact Info */}
          <div>
            <h3 className="text-lg font-semibold mb-4">Contact Us</h3>
            <div className="space-y-3">
              <div className="flex items-center space-x-2 text-gray-300">
                <MapPin className="h-4 w-4" />
                <span>JNU Campus, New Delhi</span>
              </div>
              <div className="flex items-center space-x-2 text-gray-300 text-sm">
                <span className="invisible"><MapPin className="h-4 w-4" /></span>
                <Link
                  to="/developers"
                  style={{ display: 'inline', color: 'inherit', textDecoration: 'none' }}
                  className="hover:text-white cursor-pointer"
                >
                  Meet the Developers
                </Link>
              </div>
              <div className="flex items-center space-x-2 text-gray-300">
                <Phone className="h-4 w-4" />
                <span>+91 11 2670 4000</span>
              </div>
            </div>
          </div>
        </div>

        {/* Bottom Section */}
        <div className="border-t border-gray-800 mt-8 pt-8">
          <div className="flex flex-col md:flex-row justify-between items-center">
            
            <div className="flex space-x-6 mt-4 md:mt-0">
              <span className="text-gray-400 text-sm">
                Made with ❤️ for JNU Community
              </span>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer; 