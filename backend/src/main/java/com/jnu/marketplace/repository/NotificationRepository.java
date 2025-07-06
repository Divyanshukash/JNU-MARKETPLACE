package com.jnu.marketplace.repository;

import com.jnu.marketplace.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    // Find by user ID
    List<Notification> findByUserId(String userId);

    // Find by user ID with pagination
    Page<Notification> findByUserId(String userId, Pageable pageable);

    // Find by notification type
    List<Notification> findByNotificationType(Notification.NotificationType notificationType);

    // Find by priority
    List<Notification> findByPriority(Notification.Priority priority);

    // Find by read status
    List<Notification> findByRead(boolean read);

    // Find by sent status
    List<Notification> findBySent(boolean sent);

    // Find by related entity type
    List<Notification> findByRelatedEntityType(Notification.EntityType relatedEntityType);

    // Find by related entity ID
    List<Notification> findByRelatedEntityId(String relatedEntityId);

    // Find by created date range
    List<Notification> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find by read date range
    List<Notification> findByReadAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find by sent date range
    List<Notification> findBySentAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find by expiry date
    List<Notification> findByExpiresAtBefore(LocalDateTime date);

    // Find by user ID and notification type
    List<Notification> findByUserIdAndNotificationType(String userId, Notification.NotificationType notificationType);

    // Find by user ID and notification type with pagination
    Page<Notification> findByUserIdAndNotificationType(String userId, Notification.NotificationType notificationType, Pageable pageable);

    // Find by user ID and priority
    List<Notification> findByUserIdAndPriority(String userId, Notification.Priority priority);

    // Find by user ID and priority with pagination
    Page<Notification> findByUserIdAndPriority(String userId, Notification.Priority priority, Pageable pageable);

    // Find by user ID and read status
    List<Notification> findByUserIdAndRead(String userId, boolean read);

    // Find by user ID and read status with pagination
    Page<Notification> findByUserIdAndRead(String userId, boolean read, Pageable pageable);

    // Find by user ID and sent status
    List<Notification> findByUserIdAndSent(String userId, boolean sent);

    // Find by user ID and sent status with pagination
    Page<Notification> findByUserIdAndSent(String userId, boolean sent, Pageable pageable);

    // Find by user ID and related entity type
    List<Notification> findByUserIdAndRelatedEntityType(String userId, Notification.EntityType relatedEntityType);

    // Find by user ID and related entity type with pagination
    Page<Notification> findByUserIdAndRelatedEntityType(String userId, Notification.EntityType relatedEntityType, Pageable pageable);

    // Find by user ID and related entity ID
    List<Notification> findByUserIdAndRelatedEntityId(String userId, String relatedEntityId);

    // Find by user ID and related entity ID with pagination
    Page<Notification> findByUserIdAndRelatedEntityId(String userId, String relatedEntityId, Pageable pageable);

    // Find by notification type and priority
    List<Notification> findByNotificationTypeAndPriority(Notification.NotificationType notificationType, Notification.Priority priority);

    // Find by notification type and read status
    List<Notification> findByNotificationTypeAndRead(Notification.NotificationType notificationType, boolean read);

    // Find by notification type and sent status
    List<Notification> findByNotificationTypeAndSent(Notification.NotificationType notificationType, boolean sent);

    // Find by priority and read status
    List<Notification> findByPriorityAndRead(Notification.Priority priority, boolean read);

    // Find by priority and sent status
    List<Notification> findByPriorityAndSent(Notification.Priority priority, boolean sent);

    // Find by read status and sent status
    List<Notification> findByReadAndSent(boolean read, boolean sent);

    // Find by related entity type and related entity ID
    List<Notification> findByRelatedEntityTypeAndRelatedEntityId(Notification.EntityType relatedEntityType, String relatedEntityId);

    // Find by user ID and created date range
    List<Notification> findByUserIdAndCreatedAtBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);

    // Find by user ID and read date range
    List<Notification> findByUserIdAndReadAtBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);

    // Find by user ID and sent date range
    List<Notification> findByUserIdAndSentAtBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);

    // Find by user ID and expiry date
    List<Notification> findByUserIdAndExpiresAtBefore(String userId, LocalDateTime date);

    // Find by notification type and created date range
    List<Notification> findByNotificationTypeAndCreatedAtBetween(Notification.NotificationType notificationType, LocalDateTime startDate, LocalDateTime endDate);

    // Find by priority and created date range
    List<Notification> findByPriorityAndCreatedAtBetween(Notification.Priority priority, LocalDateTime startDate, LocalDateTime endDate);

    // Find by read status and created date range
    List<Notification> findByReadAndCreatedAtBetween(boolean read, LocalDateTime startDate, LocalDateTime endDate);

    // Find by sent status and created date range
    List<Notification> findBySentAndCreatedAtBetween(boolean sent, LocalDateTime startDate, LocalDateTime endDate);

    // Find by user ID and notification type and read status
    List<Notification> findByUserIdAndNotificationTypeAndRead(String userId, Notification.NotificationType notificationType, boolean read);

    // Find by user ID and notification type and sent status
    List<Notification> findByUserIdAndNotificationTypeAndSent(String userId, Notification.NotificationType notificationType, boolean sent);

    // Find by user ID and priority and read status
    List<Notification> findByUserIdAndPriorityAndRead(String userId, Notification.Priority priority, boolean read);

    // Find by user ID and priority and sent status
    List<Notification> findByUserIdAndPriorityAndSent(String userId, Notification.Priority priority, boolean sent);

    // Find by user ID and read status and sent status
    List<Notification> findByUserIdAndReadAndSent(String userId, boolean read, boolean sent);

    // Find by user ID and notification type and priority
    List<Notification> findByUserIdAndNotificationTypeAndPriority(String userId, Notification.NotificationType notificationType, Notification.Priority priority);

    // Find by user ID and notification type and related entity type
    List<Notification> findByUserIdAndNotificationTypeAndRelatedEntityType(String userId, Notification.NotificationType notificationType, Notification.EntityType relatedEntityType);

    // Find by user ID and notification type and related entity ID
    List<Notification> findByUserIdAndNotificationTypeAndRelatedEntityId(String userId, Notification.NotificationType notificationType, String relatedEntityId);

    // Find by user ID and priority and related entity type
    List<Notification> findByUserIdAndPriorityAndRelatedEntityType(String userId, Notification.Priority priority, Notification.EntityType relatedEntityType);

    // Find by user ID and priority and related entity ID
    List<Notification> findByUserIdAndPriorityAndRelatedEntityId(String userId, Notification.Priority priority, String relatedEntityId);

    // Find by user ID and read status and related entity type
    List<Notification> findByUserIdAndReadAndRelatedEntityType(String userId, boolean read, Notification.EntityType relatedEntityType);

    // Find by user ID and read status and related entity ID
    List<Notification> findByUserIdAndReadAndRelatedEntityId(String userId, boolean read, String relatedEntityId);

    // Find by user ID and sent status and related entity type
    List<Notification> findByUserIdAndSentAndRelatedEntityType(String userId, boolean sent, Notification.EntityType relatedEntityType);

    // Find by user ID and sent status and related entity ID
    List<Notification> findByUserIdAndSentAndRelatedEntityId(String userId, boolean sent, String relatedEntityId);

    // Find by user ID and notification type and created date range
    List<Notification> findByUserIdAndNotificationTypeAndCreatedAtBetween(String userId, Notification.NotificationType notificationType, LocalDateTime startDate, LocalDateTime endDate);

    // Find by user ID and priority and created date range
    List<Notification> findByUserIdAndPriorityAndCreatedAtBetween(String userId, Notification.Priority priority, LocalDateTime startDate, LocalDateTime endDate);

    // Find by user ID and read status and created date range
    List<Notification> findByUserIdAndReadAndCreatedAtBetween(String userId, boolean read, LocalDateTime startDate, LocalDateTime endDate);

    // Find by user ID and sent status and created date range
    List<Notification> findByUserIdAndSentAndCreatedAtBetween(String userId, boolean sent, LocalDateTime startDate, LocalDateTime endDate);

    // Find by notification type with pagination
    Page<Notification> findByNotificationType(Notification.NotificationType notificationType, Pageable pageable);

    // Find by priority with pagination
    Page<Notification> findByPriority(Notification.Priority priority, Pageable pageable);

    // Find by read status with pagination
    Page<Notification> findByRead(boolean read, Pageable pageable);

    // Find by sent status with pagination
    Page<Notification> findBySent(boolean sent, Pageable pageable);

    // Find by related entity type with pagination
    Page<Notification> findByRelatedEntityType(Notification.EntityType relatedEntityType, Pageable pageable);

    // Find by related entity ID with pagination
    Page<Notification> findByRelatedEntityId(String relatedEntityId, Pageable pageable);

    // Find by created date range with pagination
    Page<Notification> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by read date range with pagination
    Page<Notification> findByReadAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by sent date range with pagination
    Page<Notification> findBySentAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by expiry date with pagination
    Page<Notification> findByExpiresAtBefore(LocalDateTime date, Pageable pageable);

    // Find by notification type and priority with pagination
    Page<Notification> findByNotificationTypeAndPriority(Notification.NotificationType notificationType, Notification.Priority priority, Pageable pageable);

    // Find by notification type and read status with pagination
    Page<Notification> findByNotificationTypeAndRead(Notification.NotificationType notificationType, boolean read, Pageable pageable);

    // Find by notification type and sent status with pagination
    Page<Notification> findByNotificationTypeAndSent(Notification.NotificationType notificationType, boolean sent, Pageable pageable);

    // Find by priority and read status with pagination
    Page<Notification> findByPriorityAndRead(Notification.Priority priority, boolean read, Pageable pageable);

    // Find by priority and sent status with pagination
    Page<Notification> findByPriorityAndSent(Notification.Priority priority, boolean sent, Pageable pageable);

    // Find by read status and sent status with pagination
    Page<Notification> findByReadAndSent(boolean read, boolean sent, Pageable pageable);

    // Find by related entity type and related entity ID with pagination
    Page<Notification> findByRelatedEntityTypeAndRelatedEntityId(Notification.EntityType relatedEntityType, String relatedEntityId, Pageable pageable);

    // Find by notification type and created date range with pagination
    Page<Notification> findByNotificationTypeAndCreatedAtBetween(Notification.NotificationType notificationType, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by priority and created date range with pagination
    Page<Notification> findByPriorityAndCreatedAtBetween(Notification.Priority priority, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by read status and created date range with pagination
    Page<Notification> findByReadAndCreatedAtBetween(boolean read, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by sent status and created date range with pagination
    Page<Notification> findBySentAndCreatedAtBetween(boolean sent, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Count by user ID
    long countByUserId(String userId);

    // Count by notification type
    long countByNotificationType(Notification.NotificationType notificationType);

    // Count by priority
    long countByPriority(Notification.Priority priority);

    // Count by read status
    long countByRead(boolean read);

    // Count by sent status
    long countBySent(boolean sent);

    // Count by related entity type
    long countByRelatedEntityType(Notification.EntityType relatedEntityType);

    // Count by related entity ID
    long countByRelatedEntityId(String relatedEntityId);

    // Count by user ID and notification type
    long countByUserIdAndNotificationType(String userId, Notification.NotificationType notificationType);

    // Count by user ID and priority
    long countByUserIdAndPriority(String userId, Notification.Priority priority);

    // Count by user ID and read status
    long countByUserIdAndRead(String userId, boolean read);

    // Count by user ID and sent status
    long countByUserIdAndSent(String userId, boolean sent);

    // Count by user ID and related entity type
    long countByUserIdAndRelatedEntityType(String userId, Notification.EntityType relatedEntityType);

    // Count by user ID and related entity ID
    long countByUserIdAndRelatedEntityId(String userId, String relatedEntityId);

    // Count by notification type and priority
    long countByNotificationTypeAndPriority(Notification.NotificationType notificationType, Notification.Priority priority);

    // Count by notification type and read status
    long countByNotificationTypeAndRead(Notification.NotificationType notificationType, boolean read);

    // Count by notification type and sent status
    long countByNotificationTypeAndSent(Notification.NotificationType notificationType, boolean sent);

    // Count by priority and read status
    long countByPriorityAndRead(Notification.Priority priority, boolean read);

    // Count by priority and sent status
    long countByPriorityAndSent(Notification.Priority priority, boolean sent);

    // Count by read status and sent status
    long countByReadAndSent(boolean read, boolean sent);

    // Count by related entity type and related entity ID
    long countByRelatedEntityTypeAndRelatedEntityId(Notification.EntityType relatedEntityType, String relatedEntityId);

    // Count by user ID and notification type and read status
    long countByUserIdAndNotificationTypeAndRead(String userId, Notification.NotificationType notificationType, boolean read);

    // Count by user ID and notification type and sent status
    long countByUserIdAndNotificationTypeAndSent(String userId, Notification.NotificationType notificationType, boolean sent);

    // Count by user ID and priority and read status
    long countByUserIdAndPriorityAndRead(String userId, Notification.Priority priority, boolean read);

    // Count by user ID and priority and sent status
    long countByUserIdAndPriorityAndSent(String userId, Notification.Priority priority, boolean sent);

    // Count by user ID and read status and sent status
    long countByUserIdAndReadAndSent(String userId, boolean read, boolean sent);

    // Count by user ID and notification type and priority
    long countByUserIdAndNotificationTypeAndPriority(String userId, Notification.NotificationType notificationType, Notification.Priority priority);

    // Count by user ID and notification type and related entity type
    long countByUserIdAndNotificationTypeAndRelatedEntityType(String userId, Notification.NotificationType notificationType, Notification.EntityType relatedEntityType);

    // Count by user ID and notification type and related entity ID
    long countByUserIdAndNotificationTypeAndRelatedEntityId(String userId, Notification.NotificationType notificationType, String relatedEntityId);

    // Count by user ID and priority and related entity type
    long countByUserIdAndPriorityAndRelatedEntityType(String userId, Notification.Priority priority, Notification.EntityType relatedEntityType);

    // Count by user ID and priority and related entity ID
    long countByUserIdAndPriorityAndRelatedEntityId(String userId, Notification.Priority priority, String relatedEntityId);

    // Count by user ID and read status and related entity type
    long countByUserIdAndReadAndRelatedEntityType(String userId, boolean read, Notification.EntityType relatedEntityType);

    // Count by user ID and read status and related entity ID
    long countByUserIdAndReadAndRelatedEntityId(String userId, boolean read, String relatedEntityId);

    // Count by user ID and sent status and related entity type
    long countByUserIdAndSentAndRelatedEntityType(String userId, boolean sent, Notification.EntityType relatedEntityType);

    // Count by user ID and sent status and related entity ID
    long countByUserIdAndSentAndRelatedEntityId(String userId, boolean sent, String relatedEntityId);

    // Delete by user ID
    void deleteByUserId(String userId);

    // Delete by notification type
    void deleteByNotificationType(Notification.NotificationType notificationType);

    // Delete by priority
    void deleteByPriority(Notification.Priority priority);

    // Delete by read status
    void deleteByRead(boolean read);

    // Delete by sent status
    void deleteBySent(boolean sent);

    // Delete by related entity type
    void deleteByRelatedEntityType(Notification.EntityType relatedEntityType);

    // Delete by related entity ID
    void deleteByRelatedEntityId(String relatedEntityId);

    // Delete by user ID and notification type
    void deleteByUserIdAndNotificationType(String userId, Notification.NotificationType notificationType);

    // Delete by user ID and priority
    void deleteByUserIdAndPriority(String userId, Notification.Priority priority);

    // Delete by user ID and read status
    void deleteByUserIdAndRead(String userId, boolean read);

    // Delete by user ID and sent status
    void deleteByUserIdAndSent(String userId, boolean sent);

    // Delete by user ID and related entity type
    void deleteByUserIdAndRelatedEntityType(String userId, Notification.EntityType relatedEntityType);

    // Delete by user ID and related entity ID
    void deleteByUserIdAndRelatedEntityId(String userId, String relatedEntityId);

    // Delete by notification type and priority
    void deleteByNotificationTypeAndPriority(Notification.NotificationType notificationType, Notification.Priority priority);

    // Delete by notification type and read status
    void deleteByNotificationTypeAndRead(Notification.NotificationType notificationType, boolean read);

    // Delete by notification type and sent status
    void deleteByNotificationTypeAndSent(Notification.NotificationType notificationType, boolean sent);

    // Delete by priority and read status
    void deleteByPriorityAndRead(Notification.Priority priority, boolean read);

    // Delete by priority and sent status
    void deleteByPriorityAndSent(Notification.Priority priority, boolean sent);

    // Delete by read status and sent status
    void deleteByReadAndSent(boolean read, boolean sent);

    // Delete by related entity type and related entity ID
    void deleteByRelatedEntityTypeAndRelatedEntityId(Notification.EntityType relatedEntityType, String relatedEntityId);

    // Delete by user ID and notification type and read status
    void deleteByUserIdAndNotificationTypeAndRead(String userId, Notification.NotificationType notificationType, boolean read);

    // Delete by user ID and notification type and sent status
    void deleteByUserIdAndNotificationTypeAndSent(String userId, Notification.NotificationType notificationType, boolean sent);

    // Delete by user ID and priority and read status
    void deleteByUserIdAndPriorityAndRead(String userId, Notification.Priority priority, boolean read);

    // Delete by user ID and priority and sent status
    void deleteByUserIdAndPriorityAndSent(String userId, Notification.Priority priority, boolean sent);

    // Delete by user ID and read status and sent status
    void deleteByUserIdAndReadAndSent(String userId, boolean read, boolean sent);

    // Delete by user ID and notification type and priority
    void deleteByUserIdAndNotificationTypeAndPriority(String userId, Notification.NotificationType notificationType, Notification.Priority priority);

    // Delete by user ID and notification type and related entity type
    void deleteByUserIdAndNotificationTypeAndRelatedEntityType(String userId, Notification.NotificationType notificationType, Notification.EntityType relatedEntityType);

    // Delete by user ID and notification type and related entity ID
    void deleteByUserIdAndNotificationTypeAndRelatedEntityId(String userId, Notification.NotificationType notificationType, String relatedEntityId);

    // Delete by user ID and priority and related entity type
    void deleteByUserIdAndPriorityAndRelatedEntityType(String userId, Notification.Priority priority, Notification.EntityType relatedEntityType);

    // Delete by user ID and priority and related entity ID
    void deleteByUserIdAndPriorityAndRelatedEntityId(String userId, Notification.Priority priority, String relatedEntityId);

    // Delete by user ID and read status and related entity type
    void deleteByUserIdAndReadAndRelatedEntityType(String userId, boolean read, Notification.EntityType relatedEntityType);

    // Delete by user ID and read status and related entity ID
    void deleteByUserIdAndReadAndRelatedEntityId(String userId, boolean read, String relatedEntityId);

    // Delete by user ID and sent status and related entity type
    void deleteByUserIdAndSentAndRelatedEntityType(String userId, boolean sent, Notification.EntityType relatedEntityType);

    // Delete by user ID and sent status and related entity ID
    void deleteByUserIdAndSentAndRelatedEntityId(String userId, boolean sent, String relatedEntityId);

    // Find all unread notifications for a user
    @Query("{'$and': [{'userId': ?0}, {'read': false}]}")
    Page<Notification> findUnreadNotificationsForUser(String userId, Pageable pageable);

    // Find all high priority notifications for a user
    @Query("{'$and': [{'userId': ?0}, {'priority': {$in: ['HIGH', 'URGENT']}}]}")
    Page<Notification> findHighPriorityNotificationsForUser(String userId, Pageable pageable);

    // Find all urgent notifications for a user
    @Query("{'$and': [{'userId': ?0}, {'priority': 'URGENT'}]}")
    Page<Notification> findUrgentNotificationsForUser(String userId, Pageable pageable);

    // Find all notifications for a user by type
    @Query("{'userId': ?0}")
    Page<Notification> findNotificationsForUser(String userId, Pageable pageable);

    // Find all notifications for a user by type with pagination
    @Query("{'$and': [{'userId': ?0}, {'notificationType': ?1}]}")
    Page<Notification> findNotificationsByTypeForUser(String userId, Notification.NotificationType notificationType, Pageable pageable);

    // Find all notifications for a user by priority with pagination
    @Query("{'$and': [{'userId': ?0}, {'priority': ?1}]}")
    Page<Notification> findNotificationsByPriorityForUser(String userId, Notification.Priority priority, Pageable pageable);

    // Find all notifications for a user by read status with pagination
    @Query("{'$and': [{'userId': ?0}, {'read': ?1}]}")
    Page<Notification> findNotificationsByReadStatusForUser(String userId, boolean read, Pageable pageable);

    // Find all notifications for a user by sent status with pagination
    @Query("{'$and': [{'userId': ?0}, {'sent': ?1}]}")
    Page<Notification> findNotificationsBySentStatusForUser(String userId, boolean sent, Pageable pageable);

    // Find all notifications for a user by related entity type with pagination
    @Query("{'$and': [{'userId': ?0}, {'relatedEntityType': ?1}]}")
    Page<Notification> findNotificationsByRelatedEntityTypeForUser(String userId, Notification.EntityType relatedEntityType, Pageable pageable);

    // Find all notifications for a user by related entity ID with pagination
    @Query("{'$and': [{'userId': ?0}, {'relatedEntityId': ?1}]}")
    Page<Notification> findNotificationsByRelatedEntityIdForUser(String userId, String relatedEntityId, Pageable pageable);

    // Find all notifications for a user by created date range with pagination
    @Query("{'$and': [{'userId': ?0}, {'createdAt': {$gte: ?1, $lte: ?2}}]}")
    Page<Notification> findNotificationsByDateRangeForUser(String userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find all notifications for a user by read date range with pagination
    @Query("{'$and': [{'userId': ?0}, {'readAt': {$gte: ?1, $lte: ?2}}]}")
    Page<Notification> findNotificationsByReadDateRangeForUser(String userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find all notifications for a user by sent date range with pagination
    @Query("{'$and': [{'userId': ?0}, {'sentAt': {$gte: ?1, $lte: ?2}}]}")
    Page<Notification> findNotificationsBySentDateRangeForUser(String userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find all notifications for a user by expiry date with pagination
    @Query("{'$and': [{'userId': ?0}, {'expiresAt': {$lt: ?1}}]}")
    Page<Notification> findExpiredNotificationsForUser(String userId, LocalDateTime date, Pageable pageable);

    // Count all unread notifications for a user
    @Query("{'$and': [{'userId': ?0}, {'read': false}]}")
    long countUnreadNotificationsForUser(String userId);

    // Count all high priority notifications for a user
    @Query("{'$and': [{'userId': ?0}, {'priority': {$in: ['HIGH', 'URGENT']}}]}")
    long countHighPriorityNotificationsForUser(String userId);

    // Count all urgent notifications for a user
    @Query("{'$and': [{'userId': ?0}, {'priority': 'URGENT'}]}")
    long countUrgentNotificationsForUser(String userId);

    // Count all notifications for a user
    @Query("{'userId': ?0}")
    long countNotificationsForUser(String userId);

    // Count all notifications for a user by type
    @Query("{'$and': [{'userId': ?0}, {'notificationType': ?1}]}")
    long countNotificationsByTypeForUser(String userId, Notification.NotificationType notificationType);

    // Count all notifications for a user by priority
    @Query("{'$and': [{'userId': ?0}, {'priority': ?1}]}")
    long countNotificationsByPriorityForUser(String userId, Notification.Priority priority);

    // Count all notifications for a user by read status
    @Query("{'$and': [{'userId': ?0}, {'read': ?1}]}")
    long countNotificationsByReadStatusForUser(String userId, boolean read);

    // Count all notifications for a user by sent status
    @Query("{'$and': [{'userId': ?0}, {'sent': ?1}]}")
    long countNotificationsBySentStatusForUser(String userId, boolean sent);

    // Count all notifications for a user by related entity type
    @Query("{'$and': [{'userId': ?0}, {'relatedEntityType': ?1}]}")
    long countNotificationsByRelatedEntityTypeForUser(String userId, Notification.EntityType relatedEntityType);

    // Count all notifications for a user by related entity ID
    @Query("{'$and': [{'userId': ?0}, {'relatedEntityId': ?1}]}")
    long countNotificationsByRelatedEntityIdForUser(String userId, String relatedEntityId);

    // Count all notifications for a user by created date range
    @Query("{'$and': [{'userId': ?0}, {'createdAt': {$gte: ?1, $lte: ?2}}]}")
    long countNotificationsByDateRangeForUser(String userId, LocalDateTime startDate, LocalDateTime endDate);

    // Count all notifications for a user by read date range
    @Query("{'$and': [{'userId': ?0}, {'readAt': {$gte: ?1, $lte: ?2}}]}")
    long countNotificationsByReadDateRangeForUser(String userId, LocalDateTime startDate, LocalDateTime endDate);

    // Count all notifications for a user by sent date range
    @Query("{'$and': [{'userId': ?0}, {'sentAt': {$gte: ?1, $lte: ?2}}]}")
    long countNotificationsBySentDateRangeForUser(String userId, LocalDateTime startDate, LocalDateTime endDate);

    // Count all notifications for a user by expiry date
    @Query("{'$and': [{'userId': ?0}, {'expiresAt': {$lt: ?1}}]}")
    long countExpiredNotificationsForUser(String userId, LocalDateTime date);

    // Delete all notifications for a user
    @Query("{'userId': ?0}")
    void deleteNotificationsForUser(String userId);

    // Delete all unread notifications for a user
    @Query("{'$and': [{'userId': ?0}, {'read': false}]}")
    void deleteUnreadNotificationsForUser(String userId);

    // Delete all high priority notifications for a user
    @Query("{'$and': [{'userId': ?0}, {'priority': {$in: ['HIGH', 'URGENT']}}]}")
    void deleteHighPriorityNotificationsForUser(String userId);

    // Delete all urgent notifications for a user
    @Query("{'$and': [{'userId': ?0}, {'priority': 'URGENT'}]}")
    void deleteUrgentNotificationsForUser(String userId);

    // Delete all notifications for a user by type
    @Query("{'$and': [{'userId': ?0}, {'notificationType': ?1}]}")
    void deleteNotificationsByTypeForUser(String userId, Notification.NotificationType notificationType);

    // Delete all notifications for a user by priority
    @Query("{'$and': [{'userId': ?0}, {'priority': ?1}]}")
    void deleteNotificationsByPriorityForUser(String userId, Notification.Priority priority);

    // Delete all notifications for a user by read status
    @Query("{'$and': [{'userId': ?0}, {'read': ?1}]}")
    void deleteNotificationsByReadStatusForUser(String userId, boolean read);

    // Delete all notifications for a user by sent status
    @Query("{'$and': [{'userId': ?0}, {'sent': ?1}]}")
    void deleteNotificationsBySentStatusForUser(String userId, boolean sent);

    // Delete all notifications for a user by related entity type
    @Query("{'$and': [{'userId': ?0}, {'relatedEntityType': ?1}]}")
    void deleteNotificationsByRelatedEntityTypeForUser(String userId, Notification.EntityType relatedEntityType);

    // Delete all notifications for a user by related entity ID
    @Query("{'$and': [{'userId': ?0}, {'relatedEntityId': ?1}]}")
    void deleteNotificationsByRelatedEntityIdForUser(String userId, String relatedEntityId);

    // Delete all notifications for a user by created date range
    @Query("{'$and': [{'userId': ?0}, {'createdAt': {$gte: ?1, $lte: ?2}}]}")
    void deleteNotificationsByDateRangeForUser(String userId, LocalDateTime startDate, LocalDateTime endDate);

    // Delete all notifications for a user by read date range
    @Query("{'$and': [{'userId': ?0}, {'readAt': {$gte: ?1, $lte: ?2}}]}")
    void deleteNotificationsByReadDateRangeForUser(String userId, LocalDateTime startDate, LocalDateTime endDate);

    // Delete all notifications for a user by sent date range
    @Query("{'$and': [{'userId': ?0}, {'sentAt': {$gte: ?1, $lte: ?2}}]}")
    void deleteNotificationsBySentDateRangeForUser(String userId, LocalDateTime startDate, LocalDateTime endDate);

    // Delete all notifications for a user by expiry date
    @Query("{'$and': [{'userId': ?0}, {'expiresAt': {$lt: ?1}}]}")
    void deleteExpiredNotificationsForUser(String userId, LocalDateTime date);
} 