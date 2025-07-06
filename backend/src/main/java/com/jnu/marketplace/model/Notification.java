package com.jnu.marketplace.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Notification entity for real-time alerts and notifications
 * 
 * This entity handles various types of notifications including
 * messages, offers, system updates, and transaction updates.
 * 
 * @author JNU Marketplace Team
 * @version 1.0.0
 */
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    @NotBlank(message = "Recipient ID is required")
    @Indexed
    @Field("recipient_id")
    private String recipientId;

    @Field("sender_id")
    private String senderId;

    @Field("sender_name")
    private String senderName;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    @Field("title")
    private String title;

    @NotBlank(message = "Message is required")
    @Size(max = 500, message = "Message cannot exceed 500 characters")
    @Field("message")
    private String message;

    @NotNull(message = "Notification type is required")
    @Field("type")
    private NotificationType type;

    @Field("category")
    private NotificationCategory category = NotificationCategory.GENERAL;

    @Field("priority")
    private NotificationPriority priority = NotificationPriority.NORMAL;

    @Field("read")
    private boolean read = false;

    @Field("read_at")
    private LocalDateTime readAt;

    @Field("clicked")
    private boolean clicked = false;

    @Field("clicked_at")
    private LocalDateTime clickedAt;

    @Field("dismissed")
    private boolean dismissed = false;

    @Field("dismissed_at")
    private LocalDateTime dismissedAt;

    @Field("action_url")
    private String actionUrl;

    @Field("action_text")
    private String actionText;

    @Field("image_url")
    private String imageUrl;

    @Field("icon")
    private String icon;

    @Field("badge_count")
    private int badgeCount = 1;

    @Field("related_entity_type")
    private String relatedEntityType;

    @Field("related_entity_id")
    private String relatedEntityId;

    @Field("metadata")
    private Map<String, Object> metadata = new HashMap<>();

    @Field("scheduled_at")
    private LocalDateTime scheduledAt;

    @Field("expires_at")
    private LocalDateTime expiresAt;

    @Field("sent_via_email")
    private boolean sentViaEmail = false;

    @Field("sent_via_push")
    private boolean sentViaPush = false;

    @Field("sent_via_sms")
    private boolean sentViaSms = false;

    @Field("email_sent_at")
    private LocalDateTime emailSentAt;

    @Field("push_sent_at")
    private LocalDateTime pushSentAt;

    @Field("sms_sent_at")
    private LocalDateTime smsSentAt;

    @Field("delivery_status")
    private DeliveryStatus deliveryStatus = DeliveryStatus.PENDING;

    @Field("failure_reason")
    private String failureReason;

    @Field("retry_count")
    private int retryCount = 0;

    @Field("max_retries")
    private int maxRetries = 3;

    @Field("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Field("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructors
    public Notification() {}

    public Notification(String recipientId, String title, String message, NotificationType type) {
        this.recipientId = recipientId;
        this.title = title;
        this.message = message;
        this.type = type;
    }

    public Notification(String recipientId, String senderId, String title, String message, NotificationType type) {
        this.recipientId = recipientId;
        this.senderId = senderId;
        this.title = title;
        this.message = message;
        this.type = type;
    }

    // Helper methods
    public void markAsRead() {
        this.read = true;
        this.readAt = LocalDateTime.now();
    }

    public void markAsClicked() {
        this.clicked = true;
        this.clickedAt = LocalDateTime.now();
    }

    public void dismiss() {
        this.dismissed = true;
        this.dismissedAt = LocalDateTime.now();
    }

    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        return this.metadata != null ? this.metadata.get(key) : null;
    }

    public void setAction(String actionUrl, String actionText) {
        this.actionUrl = actionUrl;
        this.actionText = actionText;
    }

    public void markEmailSent() {
        this.sentViaEmail = true;
        this.emailSentAt = LocalDateTime.now();
    }

    public void markPushSent() {
        this.sentViaPush = true;
        this.pushSentAt = LocalDateTime.now();
    }

    public void markSmsSent() {
        this.sentViaSms = true;
        this.smsSentAt = LocalDateTime.now();
    }

    public void markAsDelivered() {
        this.deliveryStatus = DeliveryStatus.DELIVERED;
    }

    public void markAsFailed(String reason) {
        this.deliveryStatus = DeliveryStatus.FAILED;
        this.failureReason = reason;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public boolean canRetry() {
        return this.retryCount < this.maxRetries;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isScheduled() {
        return scheduledAt != null && LocalDateTime.now().isBefore(scheduledAt);
    }

    public boolean isActionable() {
        return actionUrl != null && !actionUrl.isEmpty();
    }

    public boolean isUrgent() {
        return priority == NotificationPriority.HIGH || priority == NotificationPriority.URGENT;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public NotificationCategory getCategory() {
        return category;
    }

    public void setCategory(NotificationCategory category) {
        this.category = category;
    }

    public NotificationPriority getPriority() {
        return priority;
    }

    public void setPriority(NotificationPriority priority) {
        this.priority = priority;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public boolean isClicked() {
        return clicked;
    }

    public void setClicked(boolean clicked) {
        this.clicked = clicked;
    }

    public LocalDateTime getClickedAt() {
        return clickedAt;
    }

    public void setClickedAt(LocalDateTime clickedAt) {
        this.clickedAt = clickedAt;
    }

    public boolean isDismissed() {
        return dismissed;
    }

    public void setDismissed(boolean dismissed) {
        this.dismissed = dismissed;
    }

    public LocalDateTime getDismissedAt() {
        return dismissedAt;
    }

    public void setDismissedAt(LocalDateTime dismissedAt) {
        this.dismissedAt = dismissedAt;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public String getActionText() {
        return actionText;
    }

    public void setActionText(String actionText) {
        this.actionText = actionText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getBadgeCount() {
        return badgeCount;
    }

    public void setBadgeCount(int badgeCount) {
        this.badgeCount = badgeCount;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public String getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(String relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isSentViaEmail() {
        return sentViaEmail;
    }

    public void setSentViaEmail(boolean sentViaEmail) {
        this.sentViaEmail = sentViaEmail;
    }

    public boolean isSentViaPush() {
        return sentViaPush;
    }

    public void setSentViaPush(boolean sentViaPush) {
        this.sentViaPush = sentViaPush;
    }

    public boolean isSentViaSms() {
        return sentViaSms;
    }

    public void setSentViaSms(boolean sentViaSms) {
        this.sentViaSms = sentViaSms;
    }

    public LocalDateTime getEmailSentAt() {
        return emailSentAt;
    }

    public void setEmailSentAt(LocalDateTime emailSentAt) {
        this.emailSentAt = emailSentAt;
    }

    public LocalDateTime getPushSentAt() {
        return pushSentAt;
    }

    public void setPushSentAt(LocalDateTime pushSentAt) {
        this.pushSentAt = pushSentAt;
    }

    public LocalDateTime getSmsSentAt() {
        return smsSentAt;
    }

    public void setSmsSentAt(LocalDateTime smsSentAt) {
        this.smsSentAt = smsSentAt;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Enums
    public enum NotificationType {
        MESSAGE("Message"),
        OFFER("Offer"),
        TRANSACTION("Transaction"),
        SYSTEM("System"),
        SECURITY("Security"),
        PROMOTION("Promotion"),
        REMINDER("Reminder"),
        WELCOME("Welcome");

        private final String displayName;

        NotificationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum NotificationCategory {
        GENERAL("General"),
        MESSAGES("Messages"),
        TRANSACTIONS("Transactions"),
        SECURITY("Security"),
        MARKETING("Marketing"),
        SYSTEM("System");

        private final String displayName;

        NotificationCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum NotificationPriority {
        LOW("Low"),
        NORMAL("Normal"),
        HIGH("High"),
        URGENT("Urgent");

        private final String displayName;

        NotificationPriority(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum DeliveryStatus {
        PENDING("Pending"),
        SENT("Sent"),
        DELIVERED("Delivered"),
        FAILED("Failed"),
        CANCELLED("Cancelled");

        private final String displayName;

        DeliveryStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
} 