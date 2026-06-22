import React, { useEffect, useState, useRef } from 'react';
import axios from 'axios';
import { useAuth } from '../contexts/AuthContext';
import { useLocation } from 'react-router-dom';
import { formatDate, formatRelativeTime } from '../utils/dateUtils';
import { 
  Send, 
  Search, 
  MoreVertical, 
  Image, 
  Paperclip, 
  Smile,
  Clock,
  Check,
  CheckCheck,
  User,
  MessageCircle,
  Archive,
  Trash2,
  Star,
  StarOff
} from 'lucide-react';

// Create authenticated axios instance
const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL,
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

// Simple emoji data
const emojis = [
  '😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣', '😊', '😇',
  '🙂', '🙃', '😉', '😌', '😍', '🥰', '😘', '😗', '😙', '😚',
  '😋', '😛', '😝', '😜', '🤪', '🤨', '🧐', '🤓', '😎', '🤩',
  '🥳', '😏', '😒', '😞', '😔', '😟', '😕', '🙁', '☹️', '😣',
  '😖', '😫', '😩', '🥺', '😢', '😭', '😤', '😠', '😡', '🤬',
  '🤯', '😳', '🥵', '🥶', '😱', '😨', '😰', '😥', '😓', '🤗',
  '🤔', '🤭', '🤫', '🤥', '😶', '😐', '😑', '😯', '😦', '😧',
  '😮', '😲', '🥱', '😴', '🤤', '😪', '😵', '🤐', '🥴', '🤢',
  '🤮', '🤧', '😷', '🤒', '🤕', '🤑', '🤠', '💩', '👻', '💀',
  '☠️', '👽', '👾', '🤖', '😺', '😸', '😹', '😻', '😼', '😽',
  '🙀', '😿', '😾', '👋', '🤚', '🖐️', '✋', '🖖', '👌', '🤌',
  '🤏', '✌️', '🤞', '🤟', '🤘', '🤙', '👈', '👉', '👆', '🖕',
  '👇', '☝️', '👍', '👎', '✊', '👊', '🤛', '🤜', '👏', '🙌',
  '👐', '🤲', '🤝', '🙏', '✍️', '💪', '🦾', '🦿', '🦵', '🦶',
  '👂', '🦻', '👃', '🧠', '🫀', '🫁', '🦷', '🦴', '👀', '👁️',
  '👅', '👄', '💋', '🩸', '❤️', '🧡', '💛', '💚', '💙', '💜',
  '🖤', '🤍', '🤎', '💔', '❣️', '💕', '💞', '💓', '💗', '💖',
  '💘', '💝', '💟', '☮️', '✝️', '☪️', '🕉️', '☸️', '✡️', '🔯',
  '🕎', '☯️', '☦️', '🛐', '⛎', '♈', '♉', '♊', '♋', '♌', '♍',
  '♎', '♏', '♐', '♑', '♒', '♓', '🆔', '⚛️', '🉑', '☢️', '☣️'
];

interface Conversation {
  otherUserId: string;
  otherUserName: string;
  lastMessage: string;
  lastMessageTime: string;
  lastMessageSenderId: string;
  unreadCount?: number;
  isOnline?: boolean;
  lastSeen?: string;
  otherUserProfilePicture?: string;
}

interface Message {
  id: string;
  senderId: string;
  receiverId: string;
  senderName: string;
  receiverName: string;
  listingId?: string;
  content: string;
  createdAt: string;
  read?: boolean;
  delivered?: boolean;
  messageType?: string;
  attachments?: string[];
  attachmentUrl?: string;
}

const Messages: React.FC = () => {
  const { user } = useAuth();
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [selectedConv, setSelectedConv] = useState<Conversation | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [newMessage, setNewMessage] = useState('');
  const [sending, setSending] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [showSearch, setShowSearch] = useState(false);
  const [showEmojiPicker, setShowEmojiPicker] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const emojiPickerRef = useRef<HTMLDivElement>(null);
  const location = useLocation();

  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    if (!token) {
      setError('You must be logged in to view messages.');
      setLoading(false);
      return;
    }
    loadConversations();
  }, []);

  const loadConversations = () => {
    setLoading(true);
    setError(null);
    const token = localStorage.getItem('accessToken');
    api.get('/messages/conversations')
      .then(res => {
        // Use actual data from server
        setConversations(res.data);
        
        // Check if we have a selected user from navigation
        const selectedUser = location.state?.selectedUserId;
        if (selectedUser) {
          const targetConversation = res.data.find((conv: Conversation) => 
            conv.otherUserId === selectedUser
          );
          if (targetConversation) {
            loadMessages(targetConversation);
          }
        }
      })
      .catch(() => setError('Failed to load conversations.'))
      .finally(() => setLoading(false));
  };

  const loadMessages = (conv: Conversation) => {
    setSelectedConv(conv);
    setMessages([]);
    setLoading(true);
    setError(null);
    const token = localStorage.getItem('accessToken');
    api.get('/messages')
      .then(res => {
        // Filter messages for this conversation (user only, ignore listingId)
        const filtered = res.data.filter((msg: Message) =>
          (msg.senderId === user?.id && msg.receiverId === conv.otherUserId) ||
          (msg.senderId === conv.otherUserId && msg.receiverId === user?.id)
        );
        setMessages(filtered);
        
        // Mark messages as read when conversation is opened
        markMessagesAsRead(conv.otherUserId);
        
        // Scroll to bottom after messages are loaded
        setTimeout(() => {
          const messagesContainer = document.querySelector('.messages-container');
          if (messagesContainer) {
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
          }
        }, 100);
      })
      .catch(() => setError('Failed to load messages.'))
      .finally(() => setLoading(false));
  };

  const markMessagesAsRead = async (otherUserId: string) => {
    try {
      const token = localStorage.getItem('accessToken');
      await api.put(`/messages/mark-read/${otherUserId}`);
      
      // Update conversations to remove unread count for this user
      setConversations(prev => prev.map(conv => 
        conv.otherUserId === otherUserId 
          ? { ...conv, unreadCount: 0 }
          : conv
      ));
    } catch (error) {
      console.error('Failed to mark messages as read:', error);
    }
  };

  const handleSend = async () => {
    if (!newMessage.trim() || !selectedConv) return;
    setSending(true);
    setError(null);
    const token = localStorage.getItem('accessToken');
    try {
      await api.post('/messages', {
        recipientId: selectedConv.otherUserId,
        content: newMessage,
      });
      setNewMessage('');
      
      // Add the new message to the current messages instead of reloading
      const newMsg: Message = {
        id: Date.now().toString(), // Temporary ID
        senderId: user?.id || '',
        receiverId: selectedConv.otherUserId,
        senderName: user?.firstName + ' ' + user?.lastName || '',
        receiverName: selectedConv.otherUserName,
        content: newMessage,
        createdAt: new Date().toISOString(),
        read: false,
        delivered: false
      };
      
      setMessages(prev => [...prev, newMsg]);
      
      // Scroll to bottom after sending
      setTimeout(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
      }, 100);
    } catch {
      setError('Failed to send message.');
    } finally {
      setSending(false);
    }
  };

  const handleFileUpload = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file || !selectedConv) return;

    // Check file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      setError('File size must be less than 5MB');
      return;
    }

    // Check file type
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
    if (!allowedTypes.includes(file.type)) {
      setError('File type not supported. Please upload images, PDFs, or Word documents.');
      return;
    }

    setSending(true);
    setError(null);

    try {
      const formData = new FormData();
      formData.append('file', file);

      const token = localStorage.getItem('accessToken');
      
      // Upload file to server
      const uploadResponse = await api.post('/listings/upload', formData, {
        headers: { 
          'Content-Type': 'multipart/form-data'
        },
      });

      const fileUrl = uploadResponse.data;

      // Send message with file attachment
      await api.post('/messages', {
        recipientId: selectedConv.otherUserId,
        content: `📎 ${file.name}`,
        attachmentUrl: fileUrl
      });

      // Reload messages
      loadMessages(selectedConv);
      
      // Clear file input
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    } catch (err: any) {
      setError('Failed to upload file. ' + (err?.response?.data?.message || err.message));
    } finally {
      setSending(false);
    }
  };

  const filteredConversations = conversations.filter(conv => {
    const matchesSearch = conv.otherUserName.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         conv.lastMessage.toLowerCase().includes(searchQuery.toLowerCase());
    
    return matchesSearch;
  });

  const formatTime = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInHours = (now.getTime() - date.getTime()) / (1000 * 60 * 60);
    
    if (diffInHours < 24) {
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } else if (diffInHours < 48) {
      return 'Yesterday';
    } else {
      return formatDate(date);
    }
  };

  const getMessageStatus = (message: Message) => {
    if (message.senderId === user?.id) {
      if (message.read) {
        return <CheckCheck className="h-3 w-3 text-blue-500" />;
      } else if (message.delivered) {
        return <CheckCheck className="h-3 w-3 text-gray-400" />;
      } else {
        return <Check className="h-3 w-3 text-gray-400" />;
      }
    }
    return null;
  };

  const handleEmojiClick = (emoji: string) => {
    setNewMessage(prev => prev + emoji);
    setShowEmojiPicker(false);
  };

  // Close emoji picker when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (emojiPickerRef.current && !emojiPickerRef.current.contains(event.target as Node)) {
        setShowEmojiPicker(false);
      }
    };

    if (showEmojiPicker) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showEmojiPicker]);

  if (!user) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-gray-800 pt-16">
        <div className="max-w-4xl mx-auto py-8 px-4">
          <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-8 text-center">
            <MessageCircle className="h-16 w-16 text-gray-400 mx-auto mb-4" />
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">Access Denied</h2>
            <p className="text-gray-600 dark:text-gray-300">You must be logged in to view messages.</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-gray-800 pt-16">
      <div className="max-w-7xl mx-auto py-8 px-4">
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg overflow-hidden">
          <div className="flex h-[600px]">
            {/* Conversation List */}
            <div className="w-1/3 border-r border-gray-200 dark:border-gray-700 flex flex-col">
              {/* Header */}
              <div className="p-4 border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-700">
                <div className="flex items-center justify-between mb-4">
                  <h2 className="text-xl font-bold text-gray-900 dark:text-white">Messages</h2>
                  <div className="flex items-center space-x-2">
                    <button 
                      onClick={() => setShowSearch(!showSearch)}
                      className="p-2 text-gray-500 hover:text-gray-700 dark:hover:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600"
                    >
                      <Search className="h-4 w-4" />
                    </button>
                  </div>
                </div>
                
                {showSearch && (
                  <div className="relative mb-4">
                    <Search className="h-4 w-4 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                    <input
                      type="text"
                      placeholder="Search conversations..."
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                      className="w-full pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 dark:bg-gray-700 dark:text-white"
                    />
                  </div>
                )}
              </div>

              {/* Conversation List */}
              <div className="flex-1 overflow-y-auto">
                {loading ? (
                  <div className="p-4 text-center text-gray-500 dark:text-gray-400">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-500 mx-auto"></div>
                    <p className="mt-2">Loading conversations...</p>
                  </div>
                ) : error ? (
                  <div className="p-4 text-center text-red-600 dark:text-red-400">{error}</div>
                ) : filteredConversations.length === 0 ? (
                  <div className="p-4 text-center text-gray-500 dark:text-gray-400">
                    <MessageCircle className="h-12 w-12 mx-auto mb-2 text-gray-300" />
                    <p>No conversations found.</p>
                  </div>
                ) : (
                  <div className="space-y-1">
                    {filteredConversations.map((conv) => (
                      <div
                        key={conv.otherUserId}
                        className={`p-4 cursor-pointer transition-colors ${
                          selectedConv && conv.otherUserId === selectedConv.otherUserId 
                            ? 'bg-primary-50 dark:bg-primary-900/20 border-r-2 border-primary-500' 
                            : 'hover:bg-gray-50 dark:hover:bg-gray-700'
                        }`}
                        onClick={() => loadMessages(conv)}
                      >
                        <div className="flex items-center space-x-3">
                          <div className="relative">
                            {conv.otherUserProfilePicture ? (
                              <img 
                                src={`${process.env.REACT_APP_BASE_URL}${conv.otherUserProfilePicture}`}
                                alt={conv.otherUserName}
                                className="w-12 h-12 rounded-full object-cover"
                              />
                            ) : (
                              <div className="w-12 h-12 bg-gradient-to-r from-primary-500 to-primary-600 rounded-full flex items-center justify-center">
                                <span className="text-white font-semibold text-lg">
                                  {conv.otherUserName.charAt(0).toUpperCase()}
                                </span>
                              </div>
                            )}
                          </div>
                          <div className="flex-1 min-w-0">
                            <div className="flex items-center justify-between">
                              <h3 className="font-semibold text-gray-900 dark:text-white truncate">
                                {conv.otherUserName}
                              </h3>
                              <span className="text-xs text-gray-500 dark:text-gray-400">
                                {formatTime(conv.lastMessageTime)}
                              </span>
                            </div>
                            <div className="flex items-center justify-between">
                              <p className="text-sm text-gray-600 dark:text-gray-300 truncate">
                                {conv.lastMessageSenderId === user?.id ? 'You: ' : ''}
                                {conv.lastMessage}
                              </p>
                              {conv.unreadCount && conv.unreadCount > 0 && (
                                <span className="bg-primary-500 text-white text-xs rounded-full px-2 py-1 min-w-[20px] text-center">
                                  {conv.unreadCount}
                                </span>
                              )}
                            </div>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>

            {/* Chat Window */}
            <div className="flex-1 flex flex-col">
              {selectedConv ? (
                <>
                  {/* Chat Header */}
                  <div className="p-4 border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-700">
                    <div className="flex items-center justify-between">
                                              <div className="flex items-center space-x-3">
                          <div className="relative">
                            {selectedConv.otherUserProfilePicture ? (
                              <img 
                                src={`${process.env.REACT_APP_BASE_URL}${selectedConv.otherUserProfilePicture}`}
                                alt={selectedConv.otherUserName}
                                className="w-10 h-10 rounded-full object-cover"
                              />
                            ) : (
                              <div className="w-10 h-10 bg-gradient-to-r from-primary-500 to-primary-600 rounded-full flex items-center justify-center">
                                <span className="text-white font-semibold">
                                  {selectedConv.otherUserName.charAt(0).toUpperCase()}
                                </span>
                              </div>
                            )}
                          </div>
                        <div>
                          <h3 className="font-semibold text-gray-900 dark:text-white">
                            {selectedConv.otherUserName}
                          </h3>
                        </div>
                      </div>

                    </div>
                  </div>

                  {/* Messages */}
                  <div className="flex-1 overflow-y-auto p-4 bg-gray-50 dark:bg-gray-900 messages-container">
                    {loading ? (
                      <div className="flex items-center justify-center h-full">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-500"></div>
                      </div>
                    ) : messages.length === 0 ? (
                      <div className="flex flex-col items-center justify-center h-full text-gray-500 dark:text-gray-400">
                        <MessageCircle className="h-16 w-16 mb-4 text-gray-300" />
                        <p className="text-lg font-medium">No messages yet</p>
                        <p className="text-sm">Start a conversation with {selectedConv.otherUserName}</p>
                      </div>
                    ) : (
                      <div className="space-y-4">
                        {[...messages]
                          .sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())
                          .map((msg, idx, arr) => {
                            const isFirstOfGroup = idx === 0 || arr[idx - 1].senderId !== msg.senderId;
                            const isLastOfGroup = idx === arr.length - 1 || arr[idx + 1].senderId !== msg.senderId;
                            const isOwnMessage = msg.senderId === user?.id;
                            
                            return (
                              <div
                                key={msg.id}
                                className={`flex ${isOwnMessage ? 'justify-end' : 'justify-start'}`}
                              >
                                <div className={`max-w-xs lg:max-w-md ${isOwnMessage ? 'order-2' : 'order-1'}`}>
                                  {!isOwnMessage && isFirstOfGroup && (
                                    <div className="flex items-center space-x-2 mb-2">
                                      <div className="w-6 h-6 bg-gradient-to-r from-primary-500 to-primary-600 rounded-full flex items-center justify-center">
                                        <span className="text-white text-xs font-semibold">
                                          {msg.senderName.charAt(0).toUpperCase()}
                                        </span>
                                      </div>
                                      <span className="text-xs text-gray-500 dark:text-gray-400">
                                        {msg.senderName}
                                      </span>
                                    </div>
                                  )}
                                                                     <div
                                     className={`rounded-2xl px-4 py-3 ${
                                       isOwnMessage
                                         ? 'bg-primary-500 text-white'
                                         : 'bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 shadow-sm'
                                     }`}
                                   >
                                     <div className="text-sm break-words">{msg.content}</div>
                                     {msg.attachmentUrl && (
                                       <div className="mt-2">
                                         {msg.attachmentUrl.match(/\.(jpg|jpeg|png|gif|webp)$/i) ? (
                                           <img 
                                             src={`${process.env.REACT_APP_BASE_URL}${msg.attachmentUrl}`}
                                             alt="Attachment" 
                                             className="max-w-xs rounded-lg cursor-pointer hover:opacity-80 transition-opacity"
                                             onClick={() => window.open(`${process.env.REACT_APP_API_URL?.replace('/api', '')}{msg.attachmentUrl}`, '_blank')}
                                           />
                                         ) : (
                                           <a 
                                             href={`${process.env.REACT_APP_API_URL?.replace('/api', '')}${msg.attachmentUrl}`}
                                             target="_blank"
                                             rel="noopener noreferrer"
                                             className="inline-flex items-center space-x-2 px-3 py-2 bg-gray-100 dark:bg-gray-700 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"
                                           >
                                             <Paperclip className="h-4 w-4" />
                                             <span className="text-sm">View Attachment</span>
                                           </a>
                                         )}
                                       </div>
                                     )}
                                    {isLastOfGroup && (
                                      <div className={`flex items-center justify-end space-x-1 mt-1 ${
                                        isOwnMessage ? 'text-primary-100' : 'text-gray-400 dark:text-gray-500'
                                      }`}>
                                        <span className="text-xs">
                                          {new Date(msg.createdAt).toLocaleTimeString([], {
                                            hour: '2-digit',
                                            minute: '2-digit',
                                          })}
                                        </span>
                                        {isOwnMessage && getMessageStatus(msg)}
                                      </div>
                                    )}
                                  </div>
                                </div>
                              </div>
                            );
                          })}
                        <div ref={messagesEndRef} />
                      </div>
                    )}
                  </div>

                  {/* Message Input */}
                  <div className="p-4 border-t border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800">
                    <div className="flex items-center space-x-2">
                      <div className="relative">
                        <button 
                          onClick={() => setShowEmojiPicker(!showEmojiPicker)}
                          className={`p-2 rounded-lg transition-colors ${
                            showEmojiPicker 
                              ? 'bg-primary-100 text-primary-600 dark:bg-primary-900 dark:text-primary-400' 
                              : 'text-gray-500 hover:text-gray-700 dark:hover:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'
                          }`}
                        >
                          <Smile className="h-5 w-5" />
                        </button>
                        
                        {/* Emoji Picker */}
                        {showEmojiPicker && (
                          <div 
                            ref={emojiPickerRef}
                            className="absolute bottom-full left-0 mb-2 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 p-3 z-10"
                            style={{ width: '320px', maxHeight: '200px', overflowY: 'auto' }}
                          >
                            <div className="grid grid-cols-8 gap-1">
                              {emojis.map((emoji, index) => (
                                <button
                                  key={index}
                                  onClick={() => handleEmojiClick(emoji)}
                                  className="w-8 h-8 flex items-center justify-center text-lg hover:bg-gray-100 dark:hover:bg-gray-700 rounded transition-colors"
                                >
                                  {emoji}
                                </button>
                              ))}
                            </div>
                          </div>
                        )}
                      </div>
                      <button 
                        onClick={() => fileInputRef.current?.click()}
                        className="p-2 text-gray-500 hover:text-gray-700 dark:hover:text-gray-300 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700"
                      >
                        <Paperclip className="h-5 w-5" />
                      </button>
                      <input
                        ref={fileInputRef}
                        type="file"
                        className="hidden"
                        onChange={handleFileUpload}
                        accept="image/*,.pdf,.doc,.docx"
                      />
                      <div className="flex-1 relative">
                        <input
                          type="text"
                          className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-full focus:outline-none focus:ring-2 focus:ring-primary-500 dark:bg-gray-700 dark:text-white"
                          placeholder="Type a message..."
                          value={newMessage}
                          onChange={(e) => setNewMessage(e.target.value)}
                          onKeyDown={(e) => { if (e.key === 'Enter' && !e.shiftKey) handleSend(); }}
                          disabled={sending}
                        />
                      </div>
                      <button 
                        onClick={handleSend}
                        disabled={sending || !newMessage.trim()}
                        className="p-3 bg-primary-500 hover:bg-primary-600 disabled:bg-gray-300 dark:disabled:bg-gray-600 text-white rounded-full transition-colors disabled:cursor-not-allowed"
                      >
                        <Send className="h-5 w-5" />
                      </button>
                    </div>
                  </div>
                </>
              ) : (
                <div className="flex-1 flex items-center justify-center">
                  <div className="text-center text-gray-500 dark:text-gray-400">
                    <MessageCircle className="h-16 w-16 mx-auto mb-4 text-gray-300" />
                    <h3 className="text-lg font-medium mb-2">Select a conversation</h3>
                    <p className="text-sm">Choose a chat to start messaging</p>
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

export default Messages; 
