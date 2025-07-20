import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate, Link } from 'react-router-dom';
import { authService } from '../services/authService';
import { 
  User, 
  Mail, 
  Lock, 
  Phone, 
  CheckCircle, 
  ArrowRight, 
  ArrowLeft,
  Shield,
  Users,
  Star,
  TrendingUp,
  GraduationCap,
  Building,
  Globe,
  Eye,
  EyeOff
} from 'lucide-react';

interface RegisterFormData {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phoneNumber: string;
}

const Register: React.FC = () => {
  const { register, handleSubmit, formState: { errors }, watch } = useForm<RegisterFormData>();
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [currentStep, setCurrentStep] = useState(1);
  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();

  const watchedPassword = watch('password');

  const onSubmit = async (data: RegisterFormData) => {
    setIsLoading(true);
    setError(null);
    setSuccess(null);
    try {
      await authService.register(data);
      setSuccess('Registration successful! Please check your email for verification.');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Registration failed.');
    } finally {
      setIsLoading(false);
    }
  };

  const nextStep = () => setCurrentStep(prev => Math.min(prev + 1, 3));
  const prevStep = () => setCurrentStep(prev => Math.max(prev - 1, 1));

  const getPasswordStrength = (password: string) => {
    if (!password) return { score: 0, color: 'bg-gray-200', text: '' };
    let score = 0;
    if (password.length >= 8) score++;
    if (/[a-z]/.test(password)) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/[0-9]/.test(password)) score++;
    if (/[^A-Za-z0-9]/.test(password)) score++;
    
    const colors = ['bg-red-500', 'bg-orange-500', 'bg-yellow-500', 'bg-blue-500', 'bg-green-500'];
    const texts = ['Very Weak', 'Weak', 'Fair', 'Good', 'Strong'];
    return { score, color: colors[score - 1] || 'bg-gray-200', text: texts[score - 1] || '' };
  };

  const passwordStrength = getPasswordStrength(watchedPassword);

  return (
    <div className="min-h-screen pt-16 bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid lg:grid-cols-2 gap-12 items-center">
          {/* Left Side - Registration Form */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-2xl p-8 lg:p-12">
            {/* Header */}
            <div className="text-center mb-8">
              <div className="w-16 h-16 bg-gradient-to-r from-primary-600 to-primary-700 rounded-full flex items-center justify-center mx-auto mb-4">
                <User className="h-8 w-8 text-white" />
              </div>
              <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">
                Join JNU BAZAAR
              </h1>
              <p className="text-gray-600 dark:text-gray-300">
                Start your journey with the JNU community marketplace
              </p>
            </div>

            {/* Progress Steps */}
            <div className="flex items-center justify-between mb-8">
              {[1, 2, 3].map((step) => (
                <div key={step} className="flex items-center">
                  <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-semibold ${
                    step <= currentStep 
                      ? 'bg-primary-600 text-white' 
                      : 'bg-gray-200 dark:bg-gray-700 text-gray-500 dark:text-gray-400'
                  }`}>
                    {step < currentStep ? <CheckCircle className="h-4 w-4" /> : step}
                  </div>
                  {step < 3 && (
                    <div className={`w-12 h-1 mx-2 ${
                      step < currentStep ? 'bg-primary-600' : 'bg-gray-200 dark:bg-gray-700'
                    }`} />
                  )}
                </div>
              ))}
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
              {/* Step 1: Personal Information */}
              {currentStep === 1 && (
                <div className="space-y-6 transition-all duration-300">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">
                        First Name
                      </label>
                      <div className="relative">
                        <input
                          type="text"
                          {...register('firstName', { required: 'First name is required' })}
                          className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-gray-700 dark:text-white transition-colors"
                          placeholder="Enter your first name"
                        />
                        <User className="absolute right-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
                      </div>
                      {errors.firstName && <p className="mt-1 text-sm text-red-600">{errors.firstName.message}</p>}
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">
                        Last Name
                      </label>
                      <div className="relative">
                        <input
                          type="text"
                          {...register('lastName', { required: 'Last name is required' })}
                          className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-gray-700 dark:text-white transition-colors"
                          placeholder="Enter your last name"
                        />
                        <User className="absolute right-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
                      </div>
                      {errors.lastName && <p className="mt-1 text-sm text-red-600">{errors.lastName.message}</p>}
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">
                      Email Address
                    </label>
                    <div className="relative">
                      <input
                        type="email"
                        {...register('email', {
                          required: 'Email is required',
                          pattern: {
                            value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                            message: 'Invalid email address',
                          },
                        })}
                        className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-gray-700 dark:text-white transition-colors"
                        placeholder="Enter your email"
                      />
                      <Mail className="absolute right-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
                    </div>
                    {errors.email && <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">
                      Phone Number
                    </label>
                    <div className="relative">
                      <input
                        type="tel"
                        {...register('phoneNumber', {
                          required: 'Phone number is required',
                          pattern: {
                            value: /^[0-9]{10}$/,
                            message: 'Phone number must be exactly 10 digits',
                          },
                        })}
                        className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-gray-700 dark:text-white transition-colors"
                        placeholder="Enter your phone number"
                      />
                      <Phone className="absolute right-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
                    </div>
                    {errors.phoneNumber && <p className="mt-1 text-sm text-red-600">{errors.phoneNumber.message}</p>}
                  </div>

                  <button
                    type="button"
                    onClick={nextStep}
                    className="w-full bg-primary-600 text-white py-3 px-6 rounded-lg font-semibold hover:bg-primary-700 transition-colors flex items-center justify-center space-x-2"
                  >
                    <span>Continue</span>
                    <ArrowRight className="h-5 w-5" />
                  </button>
                </div>
              )}

              {/* Step 2: Security */}
              {currentStep === 2 && (
                <div className="space-y-6 transition-all duration-300">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">
                      Password
                    </label>
                    <div className="relative">
                      <input
                        type={showPassword ? "text" : "password"}
                        {...register('password', {
                          required: 'Password is required',
                          minLength: {
                            value: 8,
                            message: 'Password must be at least 8 characters long',
                          },
                        })}
                        className="w-full px-4 py-3 pr-12 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-gray-700 dark:text-white transition-colors"
                        placeholder="Create a strong password"
                      />
                      <button
                        type="button"
                        onClick={() => setShowPassword(!showPassword)}
                        className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                      >
                        {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                      </button>
                    </div>
                    {errors.password && <p className="mt-1 text-sm text-red-600">{errors.password.message}</p>}
                  </div>

                  {/* Password Strength Indicator */}
                  {watchedPassword && (
                    <div className="space-y-2">
                      <div className="flex items-center justify-between text-sm">
                        <span className="text-gray-600 dark:text-gray-300">Password Strength:</span>
                        <span className={`font-medium ${passwordStrength.score >= 4 ? 'text-green-600' : passwordStrength.score >= 3 ? 'text-blue-600' : passwordStrength.score >= 2 ? 'text-yellow-600' : 'text-red-600'}`}>
                          {passwordStrength.text}
                        </span>
                      </div>
                      <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                        <div 
                          className={`h-2 rounded-full transition-all duration-300 ${passwordStrength.color}`}
                          style={{ width: `${(passwordStrength.score / 5) * 100}%` }}
                        />
                      </div>
                      <div className="grid grid-cols-2 gap-2 text-xs text-gray-500 dark:text-gray-400">
                        <div className={`flex items-center space-x-1 ${watchedPassword.length >= 8 ? 'text-green-600' : ''}`}>
                          <div className={`w-2 h-2 rounded-full ${watchedPassword.length >= 8 ? 'bg-green-500' : 'bg-gray-300'}`} />
                          <span>At least 8 characters</span>
                        </div>
                        <div className={`flex items-center space-x-1 ${/[a-z]/.test(watchedPassword) ? 'text-green-600' : ''}`}>
                          <div className={`w-2 h-2 rounded-full ${/[a-z]/.test(watchedPassword) ? 'bg-green-500' : 'bg-gray-300'}`} />
                          <span>Lowercase letter</span>
                        </div>
                        <div className={`flex items-center space-x-1 ${/[A-Z]/.test(watchedPassword) ? 'text-green-600' : ''}`}>
                          <div className={`w-2 h-2 rounded-full ${/[A-Z]/.test(watchedPassword) ? 'bg-green-500' : 'bg-gray-300'}`} />
                          <span>Uppercase letter</span>
                        </div>
                        <div className={`flex items-center space-x-1 ${/[0-9]/.test(watchedPassword) ? 'text-green-600' : ''}`}>
                          <div className={`w-2 h-2 rounded-full ${/[0-9]/.test(watchedPassword) ? 'bg-green-500' : 'bg-gray-300'}`} />
                          <span>Number</span>
                        </div>
                      </div>
                    </div>
                  )}

                  <div className="flex space-x-4">
                    <button
                      type="button"
                      onClick={prevStep}
                      className="flex-1 bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 py-3 px-6 rounded-lg font-semibold hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors flex items-center justify-center space-x-2"
                    >
                      <ArrowLeft className="h-5 w-5" />
                      <span>Back</span>
                    </button>
                    <button
                      type="button"
                      onClick={nextStep}
                      className="flex-1 bg-primary-600 text-white py-3 px-6 rounded-lg font-semibold hover:bg-primary-700 transition-colors flex items-center justify-center space-x-2"
                    >
                      <span>Continue</span>
                      <ArrowRight className="h-5 w-5" />
                    </button>
                  </div>
                </div>
              )}

              {/* Step 3: Review & Submit */}
              {currentStep === 3 && (
                <div className="space-y-6 transition-all duration-300">
                  <div className="bg-gray-50 dark:bg-gray-700 rounded-lg p-6">
                    <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Review Your Information</h3>
                    <div className="space-y-3">
                      <div className="flex justify-between">
                        <span className="text-gray-600 dark:text-gray-300">Name:</span>
                        <span className="font-medium text-gray-900 dark:text-white">{watch('firstName')} {watch('lastName')}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600 dark:text-gray-300">Email:</span>
                        <span className="font-medium text-gray-900 dark:text-white">{watch('email')}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600 dark:text-gray-300">Phone:</span>
                        <span className="font-medium text-gray-900 dark:text-white">{watch('phoneNumber')}</span>
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center space-x-3 p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                    <Shield className="h-5 w-5 text-blue-600 dark:text-blue-400" />
                    <p className="text-sm text-blue-800 dark:text-blue-200">
                      Your information is secure and will only be used for account verification.
                    </p>
                  </div>

                  <div className="flex space-x-4">
                    <button
                      type="button"
                      onClick={prevStep}
                      className="flex-1 bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 py-3 px-6 rounded-lg font-semibold hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors flex items-center justify-center space-x-2"
                    >
                      <ArrowLeft className="h-5 w-5" />
                      <span>Back</span>
                    </button>
                    <button
                      type="submit"
                      disabled={isLoading}
                      className="flex-1 bg-primary-600 text-white py-3 px-6 rounded-lg font-semibold hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center space-x-2"
                    >
                      {isLoading ? (
                        <>
                          <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
                          <span>Creating Account...</span>
                        </>
                      ) : (
                        <>
                          <span>Create Account</span>
                          <ArrowRight className="h-5 w-5" />
                        </>
                      )}
                    </button>
                  </div>
                </div>
              )}

              {error && (
                <div className="p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
                  <div className="flex items-center">
                    <div className="w-5 h-5 bg-red-500 rounded-full flex items-center justify-center mr-3">
                      <div className="w-2 h-2 bg-white rounded-full"></div>
                    </div>
                    <span className="text-red-800 dark:text-red-200">{error}</span>
                  </div>
                </div>
              )}
              
              {success && (
                <div className="p-4 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg">
                  <div className="flex items-center">
                    <div className="w-5 h-5 bg-green-500 rounded-full flex items-center justify-center mr-3">
                      <div className="w-2 h-2 bg-white rounded-full"></div>
                    </div>
                    <span className="text-green-800 dark:text-green-200">{success}</span>
                  </div>
                </div>
              )}
            </form>

            <div className="mt-8 text-center">
              <p className="text-gray-600 dark:text-gray-300">
                Already have an account?{' '}
                <Link to="/login" className="font-medium text-primary-600 hover:text-primary-500 transition-colors">
                  Sign in here
                </Link>
              </p>
            </div>
          </div>

          {/* Right Side - Benefits & Features */}
          <div className="space-y-8">
            <div className="text-center lg:text-left">
              <h2 className="text-4xl font-bold text-gray-900 dark:text-white mb-4">
                Welcome to JNU BAZAAR
              </h2>
              <p className="text-xl text-gray-600 dark:text-gray-300 mb-8">
                Join thousands of students, faculty, and staff in our vibrant community marketplace
              </p>
            </div>

            <div className="grid gap-6">
              <div className="flex items-start space-x-4 p-6 bg-white dark:bg-gray-800 rounded-xl shadow-lg">
                <div className="w-12 h-12 bg-blue-100 dark:bg-blue-900 rounded-lg flex items-center justify-center flex-shrink-0">
                  <Users className="h-6 w-6 text-blue-600 dark:text-blue-400" />
                </div>
                <div>
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">
                    Community First
                  </h3>
                  <p className="text-gray-600 dark:text-gray-300">
                    Connect with fellow JNU students, faculty, and staff in a trusted environment.
                  </p>
                </div>
              </div>

              <div className="flex items-start space-x-4 p-6 bg-white dark:bg-gray-800 rounded-xl shadow-lg">
                <div className="w-12 h-12 bg-green-100 dark:bg-green-900 rounded-lg flex items-center justify-center flex-shrink-0">
                  <Shield className="h-6 w-6 text-green-600 dark:text-green-400" />
                </div>
                <div>
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">
                    Safe & Secure
                  </h3>
                  <p className="text-gray-600 dark:text-gray-300">
                    Verified users and secure platform ensure trustworthy transactions.
                  </p>
                </div>
              </div>

              <div className="flex items-start space-x-4 p-6 bg-white dark:bg-gray-800 rounded-xl shadow-lg">
                <div className="w-12 h-12 bg-purple-100 dark:bg-purple-900 rounded-lg flex items-center justify-center flex-shrink-0">
                  <TrendingUp className="h-6 w-6 text-purple-600 dark:text-purple-400" />
                </div>
                <div>
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">
                    Great Deals
                  </h3>
                  <p className="text-gray-600 dark:text-gray-300">
                    Find amazing deals on textbooks, electronics, and more from your community.
                  </p>
                </div>
              </div>

              <div className="flex items-start space-x-4 p-6 bg-white dark:bg-gray-800 rounded-xl shadow-lg">
                <div className="w-12 h-12 bg-yellow-100 dark:bg-yellow-900 rounded-lg flex items-center justify-center flex-shrink-0">
                  <Star className="h-6 w-6 text-yellow-600 dark:text-yellow-400" />
                </div>
                <div>
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">
                    Quality Assurance
                  </h3>
                  <p className="text-gray-600 dark:text-gray-300">
                    Verified listings ensure quality transactions.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>


    </div>
  );
};

export default Register; 