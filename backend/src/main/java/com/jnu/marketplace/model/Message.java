package com.jnu.marketplace.model;


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
import java.util.ArrayList;
import java.util.List;

/**
 * Message entity for communication between users
 * 
 * This entity handles messaging between buyers and sellers,
 * including text messages and images.
 * 
 * @author JNU Marketplace Team
 * @version 1.0.0
 */
@Document(collection = "messages")
public class Message {

    @Id
    private String id;

    @NotNull(message = "Sender ID is required")
    @Indexed
    @Field("sender_id")
    private String senderId;

    @NotNull(message = "Receiver ID is required")
    @Indexed
    @Field("receiver_id")
    private String receiverId;

    @Field("listing_id")
    @Indexed
    private String listingId;

    @NotBlank(message = "Message content is required")
    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    @Field("content")
    private String content;

    @Field("message_type")
    private MessageType messageType = MessageType.TEXT;

    @Field("attachments")
    private List<String> attachments = new ArrayList<>();

    @Field("read")
    private boolean read = false;

    @Field("read_at")
    private LocalDateTime readAt;

    @Field("delivered")
    private boolean delivered = false;

    @Field("delivered_at")
    private LocalDateTime deliveredAt;

    @Field("edited")
    private boolean edited = false;

    @Field("edited_at")
    private LocalDateTime editedAt;

    @Field("reply_to")
    private String replyTo;

    @Field("forwarded_from")
    private String forwardedFrom;

    @Field("system_message")
    private boolean systemMessage = false;

    @Field("message_status")
    private MessageStatus status = MessageStatus.SENT;

    @Field("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Field("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructors
    public Message() {}

    public Message(String senderId, String receiverId, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
    }

    public Message(String senderId, String receiverId, String listingId, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.listingId = listingId;
        this.content = content;
    }

    // Helper methods
    public void markAsRead() {
        this.read = true;
        this.readAt = LocalDateTime.now();
    }

    public void markAsDelivered() {
        this.delivered = true;
        this.deliveredAt = LocalDateTime.now();
    }

    public void markAsEdited() {
        this.edited = true;
        this.editedAt = LocalDateTime.now();
    }

    public void addAttachment(String attachmentUrl) {
        if (this.attachments == null) {
            this.attachments = new ArrayList<>();
        }
        this.attachments.add(attachmentUrl);
    }

    public void removeAttachment(String attachmentUrl) {
        if (this.attachments != null) {
            this.attachments.remove(attachmentUrl);
        }
    }

    public boolean isSystemMessage() {
        return systemMessage;
    }

    public boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }

    public boolean isReply() {
        return replyTo != null && !replyTo.isEmpty();
    }

    public boolean isForwarded() {
        return forwardedFrom != null && !forwardedFrom.isEmpty();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
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

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public LocalDateTime getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(LocalDateTime editedAt) {
        this.editedAt = editedAt;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public String getForwardedFrom() {
        return forwardedFrom;
    }

    public void setForwardedFrom(String forwardedFrom) {
        this.forwardedFrom = forwardedFrom;
    }

    public boolean getSystemMessage() {
        return systemMessage;
    }

    public void setSystemMessage(boolean systemMessage) {
        this.systemMessage = systemMessage;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
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
    public enum MessageType {
        TEXT("Text"),
        IMAGE("Image"),
        FILE("File"),
        LOCATION("Location"),
        OFFER("Offer"),
        SYSTEM("System");

        private final String displayName;

        MessageType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum MessageStatus {
        SENT("Sent"),
        DELIVERED("Delivered"),
        READ("Read"),
        FAILED("Failed"),
        DELETED("Deleted");

        private final String displayName;

        MessageStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
} 