package com.jnu.marketplace.controller;

import com.jnu.marketplace.dto.ListingRequest;
import com.jnu.marketplace.dto.SearchRequest;
import com.jnu.marketplace.model.Listing;
import com.jnu.marketplace.model.Listing.ListingStatus;
import com.jnu.marketplace.service.ListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ListingController {

    private final ListingService listingService;

    @PostMapping
    public ResponseEntity<Listing> createListing(@Valid @RequestBody ListingRequest request) {
        Listing listing = listingService.createListing(request);
        return ResponseEntity.ok(listing);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Listing> updateListing(@PathVariable String id, @Valid @RequestBody ListingRequest request) {
        Listing listing = listingService.updateListing(id, request);
        return ResponseEntity.ok(listing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteListing(@PathVariable String id) {
        listingService.deleteListing(id);
        return ResponseEntity.ok("Listing deleted successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Listing> getListingById(@PathVariable String id) {
        Listing listing = listingService.getListingById(id);
        listingService.incrementViewCount(id);
        return ResponseEntity.ok(listing);
    }

    @GetMapping
    public ResponseEntity<Page<Listing>> getAllListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean includeSold
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Listing> listings = includeSold ? 
            listingService.getAllListings(pageable) : 
            listingService.getActiveListings(pageable);
        return ResponseEntity.ok(listings);
    }

    @PostMapping("/search")
    public ResponseEntity<Page<Listing>> searchListings(
            @RequestBody SearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        System.out.println("Search request received: " + request);
        System.out.println("Page: " + page + ", Size: " + size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Listing> listings = listingService.searchListings(request, pageable);
        
        System.out.println("Search results: " + listings.getTotalElements() + " total, " + listings.getContent().size() + " in page");
        
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Listing>> getListingsByCategory(@PathVariable String category) {
        List<Listing> listings = listingService.getListingsByCategory(category);
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<Listing>> getListingsBySeller(@PathVariable String sellerId) {
        List<Listing> listings = listingService.getListingsBySeller(sellerId);
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/my-listings")
    public ResponseEntity<List<Listing>> getMyListings() {
        List<Listing> listings = listingService.getMyListings();
        return ResponseEntity.ok(listings);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateListingStatus(
            @PathVariable String id,
            @RequestParam ListingStatus status
    ) {
        listingService.updateListingStatus(id, status);
        return ResponseEntity.ok("Listing status updated successfully");
    }

    @GetMapping("/featured")
    public ResponseEntity<List<Listing>> getFeaturedListings() {
        List<Listing> listings = listingService.getFeaturedListings();
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Listing>> getRecentListings() {
        List<Listing> listings = listingService.getRecentListings();
        return ResponseEntity.ok(listings);
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<String> addToFavorites(@PathVariable String id) {
        listingService.addToFavorites(id);
        return ResponseEntity.ok("Added to favorites");
    }

    @DeleteMapping("/{id}/favorite")
    public ResponseEntity<String> removeFromFavorites(@PathVariable String id) {
        listingService.removeFromFavorites(id);
        return ResponseEntity.ok("Removed from favorites");
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        String uploadsDir = "uploads/";
        File dir = new File(uploadsDir);
        if (!dir.exists()) dir.mkdirs();
        String ext = Optional.ofNullable(file.getOriginalFilename())
            .filter(f -> f.contains("."))
            .map(f -> f.substring(f.lastIndexOf('.')))
            .orElse("");
        String filename = UUID.randomUUID() + ext;
        Path filePath = Paths.get(uploadsDir, filename);
        Files.write(filePath, file.getBytes());
        String fileUrl = "/uploads/" + filename;
        return ResponseEntity.ok(fileUrl);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = Arrays.stream(Listing.Category.values())
            .map(Listing.Category::getDisplayName)
            .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/conditions")
    public ResponseEntity<List<String>> getConditions() {
        // Return list of available conditions
        List<String> conditions = List.of(
                "New", "Like New", "Good", "Fair", "Poor"
        );
        return ResponseEntity.ok(conditions);
    }
} 