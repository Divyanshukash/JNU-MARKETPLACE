import React from 'react';
import { Mail, Phone, Linkedin, Facebook, Twitter } from 'lucide-react';

const Developers: React.FC = () => {
  return (
    <div className="min-h-screen bg-gray-100 dark:bg-gray-900 pt-24 pb-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-3xl mx-auto bg-white dark:bg-gray-800 rounded-lg shadow-md p-8">
        <h1 className="text-3xl font-bold mb-8 text-center text-primary-600 dark:text-primary-400">Meet the Developers</h1>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {/* Developer 1 */}
          <div className="flex flex-col items-center bg-gray-50 dark:bg-gray-700 rounded-lg p-6 shadow">
            <img
              src="DIVYANSHU KASHYAP.jpg"
              alt="Divyanshu Kashyap"
              className="w-32 h-32 rounded-full mb-4 shadow"
            />
            <h2 className="text-xl font-semibold mb-1 text-gray-900 dark:text-gray-100">Divyanshu Kashyap</h2>
            <p className="text-primary-600 dark:text-primary-300 font-medium mb-2">Full Stack Developer</p>
            <div className="flex items-center text-gray-600 dark:text-gray-300 mb-1">
              <Mail className="w-4 h-4 mr-2" /> kasdhyapdivyanshu002@gmail.com
            </div>
            <div className="flex items-center text-gray-600 dark:text-gray-300 mb-1">
              <Phone className="w-4 h-4 mr-2" /> +91 8527861524
            </div>
            <div className="flex space-x-3 my-2">
              <a
                href="https://www.linkedin.com/in/divyanshu-kashyap-992293285/"
                className="text-blue-600 dark:text-blue-400 hover:underline"
                target="_blank"
                rel="noopener noreferrer"
              >
                <Linkedin className="w-5 h-5" />
              </a>
              <a href="#" className="text-blue-800 dark:text-blue-500 hover:underline"><Facebook className="w-5 h-5" /></a>
              <a href="#" className="text-blue-400 dark:text-blue-300 hover:underline"><Twitter className="w-5 h-5" /></a>
            </div>
            <p className="text-gray-500 dark:text-gray-300 text-sm text-center mt-2">Passionate about building impactful web applications. Loves coding, coffee, and open-source.</p>
          </div>
          {/* Developer 2 */}
          <div className="flex flex-col items-center bg-gray-50 dark:bg-gray-700 rounded-lg p-6 shadow">
            <img
              src="PROF KARAN SINGH.jpg"
              alt="Dr Karan Singh"
              className="w-32 h-32 rounded-full mb-4 shadow"
            />
            <h2 className="text-xl font-semibold mb-1 text-gray-900 dark:text-gray-100">Dr Karan Singh</h2>
            <p className="text-primary-600 dark:text-primary-300 font-medium mb-2">Faculty Advisor & Mentor</p>
            <div className="flex items-center text-gray-600 dark:text-gray-300 mb-1">
              <Mail className="w-4 h-4 mr-2" /> karancs12@gmail.com
            </div>
            <div className="flex items-center text-gray-600 dark:text-gray-300 mb-1">
              <Phone className="w-4 h-4 mr-2" /> +91 1126738743
            </div>
            <div className="flex space-x-3 my-2">
              <a href="#" className="text-blue-600 dark:text-blue-400 hover:underline"><Linkedin className="w-5 h-5" /></a>
              <a href="#" className="text-blue-800 dark:text-blue-500 hover:underline"><Facebook className="w-5 h-5" /></a>
              <a href="#" className="text-blue-400 dark:text-blue-300 hover:underline"><Twitter className="w-5 h-5" /></a>
            </div>
            <p className="text-gray-500 dark:text-gray-300 text-sm text-center mt-2">Ph.D.(CSE),M.Tech.(CSE), B.Tech.(CSE)
Assistant Professor, School of Computer & Systems Sciences, 
Jawaharlal Nehru University, New Delhi, India</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Developers;
