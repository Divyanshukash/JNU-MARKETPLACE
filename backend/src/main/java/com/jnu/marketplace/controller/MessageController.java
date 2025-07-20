package com.jnu.marketplace.controller;

import com.jnu.marketplace.model.Message;
import com.jnu.marketplace.repository.MessageRepository;
import com.jnu.marketplace.repository.UserRepository;
import com.jnu.marketplace.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, String> payload, Authentication authentication) {
        String senderEmail = authentication.getName();
        User sender = userRepository.findByEmail(senderEmail).orElse(null);
        if (sender == null) {
            return ResponseEntity.status(401).body("Sender not found");
        }
        String recipientId = payload.get("recipientId");
        String listingId = payload.get("listingId");
        String content = payload.get("content");
        if (recipientId == null || content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Missing recipient or content");
        }
        Message message = new Message();
        message.setSenderId(sender.getId());
        message.setReceiverId(recipientId);
        message.setListingId(listingId);
        message.setContent(content);
        
        // Handle attachment if provided
        String attachmentUrl = payload.get("attachmentUrl");
        if (attachmentUrl != null && !attachmentUrl.trim().isEmpty()) {
            message.addAttachment(attachmentUrl);
        }
        
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());
        messageRepository.save(message);
        return ResponseEntity.ok("Message sent!");
    }

    @GetMapping
    public ResponseEntity<?> getUserMessages(Authentication authentication) {
        System.out.println("Getting messages for user: " + authentication.getName());
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            System.out.println("User not found for email: " + userEmail);
            return ResponseEntity.status(401).body("User not found");
        }
        System.out.println("User found: " + user.getId());
        
        // Find messages where the user is sender or receiver
        var sent = messageRepository.findBySenderId(user.getId());
        var received = messageRepository.findByReceiverId(user.getId());
        System.out.println("Sent messages: " + sent.size() + ", Received messages: " + received.size());
        
        // Combine and sort by createdAt descending
        java.util.List<Message> all = new java.util.ArrayList<>();
        all.addAll(sent);
        all.addAll(received);
        all.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        // Return messages with senderName and receiverName
        var result = all.stream().map(m -> {
            var map = new HashMap<String, Object>();
            map.put("id", m.getId());
            map.put("senderId", m.getSenderId());
            map.put("receiverId", m.getReceiverId());
            
            // Get sender name
            User sender = userRepository.findById(m.getSenderId()).orElse(null);
            String senderName = sender != null ? 
                sender.getFirstName() + " " + sender.getLastName() : 
                m.getSenderId();
            map.put("senderName", senderName);
            
            // Get receiver name
            User receiver = userRepository.findById(m.getReceiverId()).orElse(null);
            String receiverName = receiver != null ? 
                receiver.getFirstName() + " " + receiver.getLastName() : 
                m.getReceiverId();
            map.put("receiverName", receiverName);
            
            map.put("listingId", m.getListingId());
            map.put("content", m.getContent());
            map.put("createdAt", m.getCreatedAt());
            map.put("attachments", m.getAttachments());
            map.put("attachmentUrl", m.getAttachments() != null && !m.getAttachments().isEmpty() ? m.getAttachments().get(0) : null);
            return map;
        }).collect(Collectors.toList());
        
        System.out.println("Returning " + result.size() + " messages");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> getUserConversations(Authentication authentication) {
        System.out.println("Getting conversations for user: " + authentication.getName());
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            System.out.println("User not found for email: " + userEmail);
            return ResponseEntity.status(401).body("User not found");
        }
        System.out.println("User found: " + user.getId());
        
        // Find all messages where the user is sender or receiver
        var sent = messageRepository.findBySenderId(user.getId());
        var received = messageRepository.findByReceiverId(user.getId());
        System.out.println("Sent messages: " + sent.size() + ", Received messages: " + received.size());
        
        java.util.List<Message> all = new java.util.ArrayList<>();
        all.addAll(sent);
        all.addAll(received);
        // Group by otherUserId only (ignore listingId)
        java.util.Map<String, Message> lastMessageMap = new java.util.HashMap<>();
        for (Message m : all) {
            String otherUserId = m.getSenderId().equals(user.getId()) ? m.getReceiverId() : m.getSenderId();
            String key = otherUserId;
            Message existing = lastMessageMap.get(key);
            if (existing == null || m.getCreatedAt().isAfter(existing.getCreatedAt())) {
                lastMessageMap.put(key, m);
            }
        }
        System.out.println("Conversations found: " + lastMessageMap.size());
        
        // Build response with user names
        var result = lastMessageMap.entrySet().stream().map(entry -> {
            Message m = entry.getValue();
            String otherUserId = m.getSenderId().equals(user.getId()) ? m.getReceiverId() : m.getSenderId();
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("otherUserId", otherUserId);
            
            // Get other user's name
            User otherUser = userRepository.findById(otherUserId).orElse(null);
            String otherUserName = otherUser != null ? 
                otherUser.getFirstName() + " " + otherUser.getLastName() : 
                otherUserId;
            map.put("otherUserName", otherUserName);
            
            map.put("lastMessage", m.getContent());
            map.put("lastMessageTime", m.getCreatedAt());
            map.put("lastMessageSenderId", m.getSenderId());
            
            // Get actual unread count for this conversation
            long unreadCount = messageRepository.countByReceiverIdAndReadFalseAndSenderId(user.getId(), otherUserId);
            map.put("unreadCount", (int) unreadCount);
            
            // Get profile picture for the other user
            if (otherUser != null && otherUser.getProfilePicture() != null) {
                map.put("otherUserProfilePicture", otherUser.getProfilePicture());
            }
            
            return map;
        }).collect(java.util.stream.Collectors.toList());
        
        System.out.println("Returning " + result.size() + " conversations");
        return ResponseEntity.ok(result);
    }

    @PutMapping("/mark-read/{otherUserId}")
    public ResponseEntity<?> markMessagesAsRead(@PathVariable String otherUserId, Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }

        // Find all unread messages from otherUserId to current user
        var unreadMessages = messageRepository.findByReceiverIdAndReadFalse(user.getId());
        var messagesFromOtherUser = unreadMessages.stream()
            .filter(message -> message.getSenderId().equals(otherUserId))
            .collect(java.util.stream.Collectors.toList());
        
        // Mark them as read
        for (Message message : messagesFromOtherUser) {
            message.setRead(true);
            message.setReadAt(LocalDateTime.now());
            messageRepository.save(message);
        }

        return ResponseEntity.ok("Messages marked as read");
    }
} 