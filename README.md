## 🚀 Live Demo

**Frontend:** https://jnu-marketplace.vercel.app

**Backend API:** https://jnu-marketplace-final.onrender.com

---

## 📌 Overview

JNU Marketplace is a secure campus marketplace that allows users to create listings, discover products, communicate with buyers and sellers, manage sale requests, and complete transactions.

The application follows a client-server architecture built with **React, Spring Boot, and MongoDB**, with an AI-powered semantic search layer using **OpenAI embeddings and Qdrant**.

The search system combines semantic similarity with traditional lexical relevance, freshness, and listing quality to provide more relevant results while maintaining a conventional keyword-search fallback when AI services are unavailable.

---

## ✨ Features

### 🔐 Authentication & Security

- JWT-based Authentication
- Secure User Registration & Login
- Password Encryption using Spring Security
- Protected REST API Endpoints
- Role-based Access Control
- Environment-based secret configuration
- Secure fallback when AI services are unavailable

### 🛍 Marketplace

- Create, Update and Delete Listings
- Upload Multiple Product Images
- Browse Products by Category
- Advanced Search and Filtering
- Wishlist Management
- Product Detail Pages
- Sale Request Management
- Listing status management

### 🤖 AI-Powered Search

- Semantic search using vector embeddings
- OpenAI embedding integration
- Qdrant vector database for similarity retrieval
- Hybrid ranking combining:
  - Semantic similarity
  - Lexical relevance
  - Listing freshness
  - Listing quality
- MongoDB remains the source of truth for listing data
- Automatic validation of Qdrant results against MongoDB
- Graceful fallback to conventional keyword search when AI services are unavailable
- Deterministic ranking for consistent results
- Asynchronous listing indexing after marketplace changes

### 💬 Communication

- Buyer-Seller Messaging
- Sale Request Management
- Email Notifications

### 🎨 User Experience

- Responsive Design
- Light & Dark Theme
- Image Gallery
- Pagination
- Modern Dashboard
- Existing marketplace UI integrated with AI search without requiring a separate AI interface

---

## 🧠 AI Search Architecture

The AI search system uses a retrieval-and-ranking architecture rather than an LLM chatbot.

```text
                    User Search Query
                           │
                           ▼
                    React Search UI
                           │
                           ▼
                 AiSearchController
                           │
                           ▼
                    AiSearchService
                           │
                 ┌─────────┴─────────┐
                 │                   │
                 ▼                   ▼
          OpenAI Embeddings     Search Filters
                 │
                 ▼
            Query Vector
                 │
                 ▼
               Qdrant
                 │
          Candidate Listings
          + Similarity Scores
                 │
                 ▼
              MongoDB
        Canonical Listing Data
                 │
                 ▼
          Eligibility Filtering
                 │
        ┌────────┼─────────┐
        ▼        ▼         ▼
     Lexical  Freshness  Quality
      Score      Score      Score
        │        │         │
        └────────┼─────────┘
                 ▼
            Hybrid Ranker
                 │
                 ▼
          Ranked Listings
                 │
                 ▼
              React UI

Hybrid Ranking
Search results are ranked using:
Final Score =
    0.70 × Semantic Similarity
  + 0.15 × Lexical Relevance
  + 0.10 × Freshness
  + 0.05 × Listing Quality

This allows the system to understand search intent while still preserving the importance of exact terms and marketplace-specific signals.
📦 AI Listing Indexing
Listings are asynchronously indexed whenever relevant marketplace changes occur.
Listing Created / Updated / Status Changed
                    │
                    ▼
          ListingChangedEvent
                    │
                    ▼
          AFTER_COMMIT + @Async
                    │
                    ▼
        ListingIndexingListener
                    │
                    ▼
        ListingIndexingService
                    │
                    ▼
          Listing Text Assembly
                    │
                    ▼
             SHA-256 Hash
                    │
                    ▼
          OpenAI Embedding
                    │
                    ▼
               Qdrant

MongoDB remains the authoritative source of listing information while Qdrant stores the vector representation and retrieval metadata.
Content hashing prevents unnecessary re-embedding when searchable listing content has not changed.
🔄 AI Reliability & Fallback
AI services are treated as an enhancement rather than a dependency of the marketplace.
If any of the following occurs:
- AI is disabled
- Embedding provider is unavailable
- Embedding request fails
- Embedding dimension is invalid
- Qdrant is unavailable
- Qdrant search fails
- No eligible semantic candidates are found
the application falls back to the existing conventional keyword-search implementation.
             AI Search
                 │
        ┌────────┴────────┐
        │                 │
     Success            Failure
        │                 │
        ▼                 ▼
 Hybrid Ranking      Keyword Search
        │                 │
        └────────┬────────┘
                 ▼
            Search Results

This ensures that an AI-service outage does not make the marketplace unavailable.
🏗 System Architecture
                    React + TypeScript
                           │
                      Axios / REST
                           │
                           ▼
                 Spring Boot Backend
                           │
       ┌───────────────────┼───────────────────┐
       │                   │                   │
       ▼                   ▼                   ▼
 Authentication         Listings          Messaging
       │                   │                   │
       │                   │                   │
       └───────────────────┼───────────────────┘
                           │
                     MongoDB Atlas
                           │
                           │
                 ┌─────────┴─────────┐
                 │                   │
                 ▼                   ▼
            AI Search           Marketplace
                 │
          ┌──────┴──────┐
          │             │
          ▼             ▼
   OpenAI Embeddings   Qdrant
          │             │
          └──────┬──────┘
                 ▼
           Hybrid Ranking

🛠 Tech Stack
Category	Technologies
Frontend	React 18, TypeScript, Tailwind CSS, React Router, React Query, Axios
Backend	Spring Boot 3, Java 17
Security	Spring Security, JWT
Database	MongoDB / MongoDB Atlas
AI / Embeddings	OpenAI Embeddings
Vector Database	Qdrant
Search	Semantic Search + Lexical Search + Hybrid Ranking
File Storage	Local File Uploads
API	REST APIs
Documentation	Swagger / OpenAPI
Build Tools	Maven, npm
Testing	JUnit, Mockito
Version Control	Git & GitHub
Deployment	Vercel, Render


🔒 Security
- JWT-based authentication
- Spring Security authorization
- Password encryption
- Protected REST endpoints
- Environment-based configuration for secrets
- No API keys or credentials hardcoded in source configuration
- AI provider failures do not expose infrastructure details to clients
Sensitive configuration should be supplied through environment variables.
🧪 Testing
The backend contains unit and controller tests covering:
- AI configuration
- Embedding generation
- Content hashing
- Listing indexing
- Qdrant vector operations
- Semantic search
- Lexical scoring
- Freshness scoring
- Listing quality scoring
- Hybrid ranking
- Search fallback behavior
- Controller behavior
- Security configuration
- Listing lifecycle events
The current implementation passes:
130 backend tests
0 failures

The backend also builds successfully with Maven, and the frontend production build succeeds.
⚠️ Current Limitations
- Live OpenAI + Qdrant + MongoDB end-to-end verification depends on external service configuration.
- Semantic search uses a bounded candidate pool rather than calculating a complete semantic catalog count.
- Failed indexing currently requires a later lifecycle event or explicit synchronization to retry.
- Ranking is deterministic and rule-based; no learned ranking model is currently used.
- Recommendations and LLM-based functionality are intentionally outside the current scope.
📁 Project Structure
JNU-MARKETPLACE/
│
├── backend/
│   ├── src/main/java/com/jnu/marketplace/
│   │   ├── ai/
│   │   │   ├── embedding/
│   │   │   ├── indexing/
│   │   │   ├── search/
│   │   │   └── vector/
│   │   │
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── event/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── security/
│   │   └── service/
│   │
│   └── src/test/
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   └── types/
│   │
│   └── public/
│
└── README.md

🚀 Running Locally
Backend
cd backend
mvn spring-boot:run

Configure the required environment variables before starting the application.
Frontend
cd frontend
npm install
npm start

The frontend communicates with the Spring Boot REST API through Axios.
👨‍💻 Development
Frontend
React + TypeScript
       │
       ▼
REST APIs
       │
       ▼
Spring Boot
       │
       ├── MongoDB
       │
       ├── OpenAI Embeddings
       │
       └── Qdrant

The project uses Git and GitHub for version control and follows a modular service-oriented backend structure.
📌 Future Improvements
Potential future improvements include:
- Automatic retry/reconciliation for failed vector indexing
- Improved semantic pagination
- Search analytics and interaction tracking
- Learning-to-rank using sufficient interaction data
- Personalized recommendations
- Offline evaluation of semantic search quality
- Additional vector-store filtering and indexing optimizations
