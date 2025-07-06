package com.jnu.marketplace.repository;

import com.jnu.marketplace.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Message entity
 * 
 * This repository provides data access methods for message management,
 * including conversation history, unread messages, and message search.
 * 
 * @author JNU Marketplace Team
 * @version 1.0.0
 */
@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    // Basic find methods
    List<Message> findBySenderId(String senderId);
    
    List<Message> findByReceiverId(String receiverId);
    
    Page<Message> findBySenderId(String senderId, Pageable pageable);
    
    Page<Message> findByReceiverId(String receiverId, Pageable pageable);

    // Conversation methods
    @Query("{'$or': [{'senderId': ?0, 'receiverId': ?1}, {'senderId': ?1, 'receiverId': ?0}]}")
    List<Message> findConversationBetweenUsers(String userId1, String userId2);
    
    @Query("{'$or': [{'senderId': ?0, 'receiverId': ?1}, {'senderId': ?1, 'receiverId': ?0}]}")
    Page<Message> findConversationBetweenUsers(String userId1, String userId2, Pageable pageable);
    
    @Query("{'$or': [{'senderId': ?0, 'receiverId': ?1}, {'senderId': ?1, 'receiverId': ?0}], 'createdAt': {'$gt': ?2}}")
    List<Message> findRecentConversationMessages(String userId1, String userId2, LocalDateTime since);

    // Listing-related messages
    List<Message> findByListingId(String listingId);
    
    Page<Message> findByListingId(String listingId, Pageable pageable);
    
    @Query("{'listingId': ?0, 'senderId': ?1}")
    List<Message> findMessagesByListingAndSender(String listingId, String senderId);
    
    @Query("{'listingId': ?0, 'receiverId': ?1}")
    List<Message> findMessagesByListingAndReceiver(String listingId, String receiverId);

    // Message type methods
    List<Message> findByMessageType(Message.MessageType messageType);
    
    List<Message> findBySenderIdAndMessageType(String senderId, Message.MessageType messageType);
    
    List<Message> findByReceiverIdAndMessageType(String receiverId, Message.MessageType messageType);

    // Status-based methods
    List<Message> findByStatus(Message.MessageStatus status);
    
    List<Message> findBySenderIdAndStatus(String senderId, Message.MessageStatus status);
    
    List<Message> findByReceiverIdAndStatus(String receiverId, Message.MessageStatus status);

    // Read/unread methods
    List<Message> findByReceiverIdAndReadFalse(String receiverId);
    
    long countByReceiverIdAndReadFalse(String receiverId);
    
    @Query("{'receiverId': ?0, 'read': false, 'createdAt': {'$gt': ?1}}")
    List<Message> findUnreadMessagesAfter(String receiverId, LocalDateTime since);
    
    @Query("{'receiverId': ?0, 'read': false}")
    Page<Message> findUnreadMessages(String receiverId, Pageable pageable);

    // Delivered methods
    List<Message> findByReceiverIdAndDeliveredFalse(String receiverId);
    
    long countByReceiverIdAndDeliveredFalse(String receiverId);
    
    @Query("{'receiverId': ?0, 'delivered': false, 'createdAt': {'$lt': ?1}}")
    List<Message> findUndeliveredMessagesBefore(String receiverId, LocalDateTime before);

    // System messages
    List<Message> findBySystemMessageTrue();
    
    List<Message> findByReceiverIdAndSystemMessageTrue(String receiverId);
    
    @Query("{'systemMessage': true, 'receiverId': ?0, 'createdAt': {'$gt': ?1}}")
    List<Message> findRecentSystemMessages(String receiverId, LocalDateTime since);

    // Reply and forwarded messages
    List<Message> findByReplyTo(String messageId);
    
    List<Message> findByForwardedFrom(String messageId);
    
    @Query("{'replyTo': ?0}")
    Page<Message> findRepliesToMessage(String messageId, Pageable pageable);

    // Date-based methods
    List<Message> findByCreatedAtAfter(LocalDateTime dateTime);
    
    List<Message> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Message> findByReadAtAfter(LocalDateTime dateTime);
    
    List<Message> findByDeliveredAtAfter(LocalDateTime dateTime);
    
    List<Message> findByEditedAtAfter(LocalDateTime dateTime);

    // Recent messages
    @Query("{'$or': [{'senderId': ?0}, {'receiverId': ?0}], 'createdAt': {'$gt': ?1}}")
    List<Message> findRecentMessagesForUser(String userId, LocalDateTime since);
    
    @Query("{'$or': [{'senderId': ?0}, {'receiverId': ?0}], 'createdAt': {'$gt': ?1}}")
    Page<Message> findRecentMessagesForUser(String userId, LocalDateTime since, Pageable pageable);

    // Message search
    @Query("{'$or': [{'senderId': ?0}, {'receiverId': ?0}], 'content': {'$regex': ?1, '$options': 'i'}}")
    List<Message> searchMessagesForUser(String userId, String searchTerm);
    
    @Query("{'$or': [{'senderId': ?0}, {'receiverId': ?0}], 'content': {'$regex': ?1, '$options': 'i'}}")
    Page<Message> searchMessagesForUser(String userId, String searchTerm, Pageable pageable);

    // Attachments
    @Query("{'attachments': {'$size': {'$gt': 0}}}")
    List<Message> findMessagesWithAttachments();
    
    @Query("{'senderId': ?0, 'attachments': {'$size': {'$gt': 0}}}")
    List<Message> findMessagesWithAttachmentsBySender(String senderId);
    
    @Query("{'receiverId': ?0, 'attachments': {'$size': {'$gt': 0}}}")
    List<Message> findMessagesWithAttachmentsByReceiver(String receiverId);

    // Edited messages
    List<Message> findByEditedTrue();
    
    List<Message> findBySenderIdAndEditedTrue(String senderId);

    // Dismissed messages
    List<Message> findByReceiverIdAndDismissedTrue(String receiverId);
    
    @Query("{'receiverId': ?0, 'dismissed': true, 'dismissedAt': {'$gt': ?1}}")
    List<Message> findDismissedMessagesAfter(String receiverId, LocalDateTime since);

    // Clicked messages
    List<Message> findByReceiverIdAndClickedTrue(String receiverId);
    
    @Query("{'receiverId': ?0, 'clicked': true, 'clickedAt': {'$gt': ?1}}")
    List<Message> findClickedMessagesAfter(String receiverId, LocalDateTime since);

    // Complex queries
    @Query("{'$and': [{'$or': [{'senderId': ?0}, {'receiverId': ?0}]}, {'messageType': ?1}, {'createdAt': {'$gt': ?2}}]}")
    List<Message> findRecentMessagesByType(String userId, Message.MessageType messageType, LocalDateTime since);
    
    @Query("{'$and': [{'receiverId': ?0}, {'read': false}, {'systemMessage': false}]}")
    Page<Message> findUnreadNonSystemMessages(String receiverId, Pageable pageable);
    
    @Query("{'$and': [{'$or': [{'senderId': ?0}, {'receiverId': ?0}]}, {'listingId': ?1}, {'createdAt': {'$gt': ?2}}]}")
    List<Message> findRecentMessagesForListing(String userId, String listingId, LocalDateTime since);

    // Statistics methods
    long countBySenderId(String senderId);
    
    long countByReceiverId(String receiverId);
    
    long countBySenderIdAndMessageType(String senderId, Message.MessageType messageType);
    
    long countByReceiverIdAndMessageType(String receiverId, Message.MessageType messageType);
    
    long countByListingId(String listingId);
    
    long countByCreatedAtAfter(LocalDateTime dateTime);
    
    long countByReadAtAfter(LocalDateTime dateTime);
    
    long countByDeliveredAtAfter(LocalDateTime dateTime);

    // Message activity
    @Query("{'senderId': ?0, 'createdAt': {'$gt': ?1}}")
    long countMessagesSentByUserAfter(String senderId, LocalDateTime since);
    
    @Query("{'receiverId': ?0, 'createdAt': {'$gt': ?1}}")
    long countMessagesReceivedByUserAfter(String receiverId, LocalDateTime since);
    
    @Query("{'$or': [{'senderId': ?0}, {'receiverId': ?0}], 'createdAt': {'$gt': ?1}}")
    long countTotalMessagesForUserAfter(String userId, LocalDateTime since);

    // Conversation statistics
    @Query("{'$or': [{'senderId': ?0, 'receiverId': ?1}, {'senderId': ?1, 'receiverId': ?0}]}")
    long countMessagesBetweenUsers(String userId1, String userId2);
    
    @Query("{'$or': [{'senderId': ?0, 'receiverId': ?1}, {'senderId': ?1, 'receiverId': ?0}], 'createdAt': {'$gt': ?2}}")
    long countRecentMessagesBetweenUsers(String userId1, String userId2, LocalDateTime since);

    // Unread conversation counts
    @Query("{'receiverId': ?0, 'read': false, 'senderId': ?1}")
    long countUnreadMessagesFromSender(String receiverId, String senderId);
    
    @Query("{'receiverId': ?0, 'read': false, 'listingId': ?1}")
    long countUnreadMessagesForListing(String receiverId, String listingId);

    // Message analytics
    @Query(value = "{}", fields = "{'messageType': 1, 'status': 1, 'createdAt': 1}")
    List<Message> findMessageStats();
    
    @Query(value = "{'senderId': ?0}", fields = "{'messageType': 1, 'status': 1, 'createdAt': 1}")
    List<Message> findUserMessageStats(String senderId);
    
    @Query(value = "{'receiverId': ?0}", fields = "{'messageType': 1, 'status': 1, 'createdAt': 1}")
    List<Message> findReceivedMessageStats(String receiverId);

    // Cleanup queries
    @Query("{'createdAt': {'$lt': ?0}}")
    List<Message> findOldMessages(LocalDateTime cutoff);
    
    @Query("{'systemMessage': true, 'createdAt': {'$lt': ?0}}")
    List<Message> findOldSystemMessages(LocalDateTime cutoff);
    
    @Query("{'dismissed': true, 'dismissedAt': {'$lt': ?0}}")
    List<Message> findOldDismissedMessages(LocalDateTime cutoff);
} 