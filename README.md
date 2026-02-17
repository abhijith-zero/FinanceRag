# FinanceRag 📚🤖

A Spring Boot application demonstrating **Retrieval-Augmented Generation (RAG)** using **Spring AI**, **pgvector**, and **PDF document ingestion**. Query your financial documents using natural language with the power of LLMs and semantic search.

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-pgvector-blue.svg)](https://github.com/pgvector/pgvector)

---

## 🎯 Overview

**FinanceRag** enables intelligent document querying by combining:
- 📄 **PDF document ingestion** with automatic text extraction
- 🔍 **Vector embeddings** for semantic search
- 💾 **PostgreSQL + pgvector** for efficient vector storage
- 🤖 **Spring AI** for LLM integration
- 🎯 **RAG architecture** to provide context-aware answers from your documents

---

## ✨ Features

- **PDF Ingestion**: Automatically read and process PDF documents
- **Semantic Search**: Find relevant information using vector similarity
- **Vector Storage**: Persist embeddings in PostgreSQL with pgvector extension
- **RAG Chat**: Answer questions using document context and LLM reasoning
- **Configurable**: Easy integration with local LLMs (LM Studio) or cloud providers
- **Token-Based Splitting**: Intelligent text chunking for optimal embedding quality
- **Docker Support**: Quick setup with Docker Compose

---

## 🏗️ Architecture

```
┌─────────────────┐
│  PDF Documents  │
│ (src/resources) │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Paragraph Reader│
│  (Spring AI)    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Token Text     │
│    Splitter     │
│  (800 tokens)   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   Embeddings    │
│  (nomic-embed)  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Vector Store   │
│  PostgreSQL +   │
│    pgvector     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   RAG Chat      │
│ QuestionAnswer  │
│    Advisor      │
└────────┬────────┘
         │
         ▼
    User Queries
   (REST API)
```

---

## 🚀 Getting Started

### Prerequisites

Before you begin, ensure you have:

- **Java 17+** ([Download](https://adoptium.net/))
- **Maven 3.6+** ([Download](https://maven.apache.org/download.cgi))
- **Docker & Docker Compose** ([Download](https://www.docker.com/products/docker-desktop))
- **LM Studio** ([Download](https://lmstudio.ai/))

### 1. Clone the Repository

```bash
git clone https://github.com/abhijith-zero/FinanceRag.git
cd FinanceRag
```

### 2. Start PostgreSQL with Docker

The project includes a `compose.yaml` file for easy PostgreSQL setup with pgvector:

```bash
docker-compose up -d
```

This starts PostgreSQL on port `55419` with:
- Database: `finance`
- Username: `postgres`
- Password: `postgres`
- pgvector extension pre-installed

Verify it's running:
```bash
docker-compose ps
```

### 3. Set Up LM Studio

#### Download and Configure Models

1. **Install LM Studio** from [lmstudio.ai](https://lmstudio.ai/)

2. **Download Required Models:**
   - **Embedding Model**: Search for "nomic-embed-text" and download
   - **Chat Model**: Search for "google/gemma-3-4b" (or similar Gemma model) and download

3. **Start the Local Server:**
   - Open LM Studio
   - Navigate to "Local Server" tab
   - Load the chat model (gemma-3-4b)
   - Click "Start Server"
   - Default endpoint: `http://localhost:1234`

4. **Verify Connection:**
```bash
curl http://localhost:1234/v1/models
```

### 4. Configure Application Properties

The default configuration in `src/main/resources/application.properties` is:

```properties
spring.application.name=finaceRag

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:55419/finance
spring.datasource.username=postgres
spring.datasource.password=postgres

# LLM Configuration
spring.ai.openai.base-url=http://localhost:1234/
spring.ai.openai.api-key=dummy
spring.ai.openai.embedding.options.model=nomic-embed-text
spring.ai.openai.chat.options.model=google/gemma-3-4b

# Vector Store Configuration
spring.ai.vectorstore.pgvector.initialize-schema=true

# Ingestion Control - Enable on first run, disable afterwards
financerag.ingest.enabled=true
```

**⚠️ Important Configuration Note:**

The `financerag.ingest.enabled` property controls document ingestion:

- **First Run**: Set to `true` to process PDFs and populate the vector store
- **Subsequent Runs**: Set to `false` to skip ingestion (embeddings are already stored)

This prevents duplicate embeddings and speeds up application startup!

**Note:** Adjust these settings if you're using different ports or credentials.

### 5. Add PDF Documents

Place your PDF files in `src/main/resources/docs/`:

```
src/main/resources/docs/
├── article.pdf                    # Default file referenced in code
├── financial_report_q4.pdf
└── market_analysis.pdf
```

**Note:** The `IngestionService` is configured to read `article.pdf` by default:

```java
@Value("classpath:/docs/article.pdf")
private Resource pdfResource;
```

To process different files, either:
- Rename your PDF to `article.pdf`, or
- Update the `@Value` annotation to reference your filename

### 6. Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

**What happens on startup:**
1. ✅ Spring Boot initializes application context
2. ✅ Connects to PostgreSQL
3. ✅ Initializes vector store schema (if enabled)
4. ✅ Checks `financerag.ingest.enabled` property
5. ✅ If enabled:
   - Reads PDF from `docs/article.pdf`
   - Splits text using ParagraphPdfDocumentReader
   - Generates embeddings with nomic-embed-text
   - Stores vectors in PostgreSQL via `vectorStore.accept()`
6. ✅ Starts REST API on port 8080

**Logs to expect:**
```
INFO ... FinanceRagApplication : Starting FinanceRagApplication
INFO ... IngestionService      : Starting data ingestion process...
INFO ... IngestionService      : Vector store updated with PDF content.
INFO ... TomcatWebServer       : Tomcat started on port 8080
```

---

## 📡 API Usage

### Chat Endpoint

**Endpoint:** `GET /chat`

**Query Parameter:** `question` (string, optional)

#### Example Requests

**Using cURL:**
```bash
curl "http://localhost:8080/chat?question=How%20did%20the%20federal%20reserve%20policy%20impact%20asset%20classes?"
```

**Using Browser:**
```
http://localhost:8080/chat?question=What are the key financial trends discussed?
```

**Default Question:**
If no question is provided, it uses: *"How did the federal reserve's recent policy decision impact various asset classes according to the analysis?"*

#### Example Response

```
Based on the analyzed documents, the Federal Reserve's recent policy decision 
had varied impacts across asset classes. Equities showed resilience with tech 
stocks outperforming, while fixed income markets experienced yield compression. 
Real estate investment trusts benefited from the lower rate environment, seeing 
increased investor interest. However, traditional banking stocks faced headwinds 
due to narrowing interest rate spreads...
```

---

## 🛠️ Configuration Deep Dive

### Ingestion Service Implementation

```java
@Component
@ConditionalOnProperty(
    name = "financerag.ingest.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class IngestionService implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(IngestionService.class);
    
    private final VectorStore vectorStore;
    
    @Value("classpath:/docs/article.pdf")
    private Resource pdfResource;
    
    public IngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting data ingestion process...");
        var pdfReader = new ParagraphPdfDocumentReader(pdfResource);
        TextSplitter splitter = new TokenTextSplitter();
        vectorStore.accept(splitter.apply(pdfReader.get()));
        logger.info("Vector store updated with PDF content.");
    }
}
```

**Key Design Decisions:**

1. **@ConditionalOnProperty**: Only runs when `financerag.ingest.enabled=true`
2. **CommandLineRunner**: Executes after Spring Boot initialization
3. **ParagraphPdfDocumentReader**: Preserves document structure for better semantic chunks
4. **TokenTextSplitter**: Uses default settings (typically 800 tokens per chunk)

### Chat Controller Implementation

```java
@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClient, PgVectorStore vectorStore) {
        this.chatClient = chatClient
            .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build())
            .build();
    }
    
    @GetMapping("/chat")
    public String chat(@RequestParam String question) {
        return chatClient.prompt().user(question).call().content();
    }
}
```

**Implementation Notes:**
- Uses builder pattern for advisor configuration
- Simple, clean API with Spring AI handling all RAG complexity
- No manual vector search or prompt engineering required

### Text Chunking Strategy

The default `TokenTextSplitter` configuration provides a good balance:

- **Chunk Size**: ~800 tokens (configurable)
- **Overlap**: Maintains context between chunks
- **Boundary Respect**: Preserves sentence integrity

**Why ParagraphPdfDocumentReader?**
- Better preservation of document structure
- More semantically meaningful chunks
- Ideal for financial documents with clear paragraphs

To customize chunking, you can modify the splitter:

```java
TextSplitter splitter = new TokenTextSplitter(
    800,   // chunk size in tokens
    400,   // minimum chunk size in characters
    5,     // minimum tokens to embed
    10000, // maximum number of chunks
    true   // keep paragraph separators
);
```

### Vector Store Configuration

Spring AI automatically configures pgvector with sensible defaults:

```properties
spring.ai.vectorstore.pgvector.initialize-schema=true  # Creates tables on startup
spring.ai.vectorstore.pgvector.index-type=HNSW         # Fast approximate search
spring.ai.vectorstore.pgvector.distance-type=COSINE    # Similarity metric
spring.ai.vectorstore.pgvector.dimensions=768          # nomic-embed-text output
```

### RAG Configuration

The `QuestionAnswerAdvisor` is configured using the builder pattern:

```java
QuestionAnswerAdvisor.builder(vectorStore).build()
```

**Default Behavior:**
- Automatically performs vector similarity search
- Retrieves relevant document chunks
- Injects context into LLM prompt
- Returns grounded, accurate responses

**Advanced Customization:**

If you need more control over the RAG behavior, you can customize the advisor:

```java
QuestionAnswerAdvisor.builder(vectorStore)
    .withSearchRequest(
        SearchRequest.defaults()
            .withTopK(5)                      // Top 5 similar chunks
            .withSimilarityThreshold(0.7)     // 70% minimum similarity
    )
    .build()
```

**Tuning Tips:**
- **topK**: Higher values provide more context but may introduce noise
- **similarityThreshold**: Lower values cast a wider net but reduce precision
- Start with defaults and adjust based on response quality

---

## 📁 Project Structure

```
FinanceRag/
├── .mvn/                           # Maven wrapper
├── src/
│   ├── main/
│   │   ├── java/com/example/finaceRag/
│   │   │   ├── FinanceRagApplication.java    # Main entry point
│   │   │   ├── IngestionService.java         # PDF ingestion pipeline
│   │   │   └── ChatController.java           # REST API endpoint
│   │   └── resources/
│   │       ├── docs/                         # PDF documents directory
│   │       │   └── article_thebeatoct2024.pdf
│   │       └── application.properties        # Configuration
│   └── test/
├── compose.yaml                    # Docker Compose for PostgreSQL
├── pom.xml                         # Maven dependencies
├── mvnw                            # Maven wrapper script (Unix)
├── mvnw.cmd                        # Maven wrapper script (Windows)
└── README.md
```

---

## 🔧 Key Dependencies

From `pom.xml`:

```xml
<!-- Spring Boot Starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring AI OpenAI Integration -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
</dependency>

<!-- pgvector Vector Store -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-pgvector-store-spring-boot-starter</artifactId>
</dependency>

<!-- PostgreSQL Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>

<!-- PDF Document Reader -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-pdf-document-reader</artifactId>
</dependency>
```

---

##  Testing & Verification

### 1. Test the Chat Endpoint

```bash
# Basic test
curl "http://localhost:8080/chat"

# Custom question
curl "http://localhost:8080/chat?question=Summarize the document"
```

### 2. Verify Vector Store

Connect to PostgreSQL:

```bash
docker exec -it financera-postgres-1 psql -U postgres -d finance
```

Run queries:

```sql
-- Check vector store table
\dt

-- Count stored embeddings
SELECT COUNT(*) FROM vector_store;

-- View sample data
SELECT id, content, metadata 
FROM vector_store 
LIMIT 3;

-- Check embedding dimensions
SELECT id, array_length(embedding, 1) as dimensions 
FROM vector_store 
LIMIT 1;
```

### 3. Monitor Logs

Watch for successful ingestion:

```
INFO ... IngestionService : Ingested 42 document chunks
INFO ... ChatController   : Received question: ...
```

---

##  Troubleshooting

### Issue: Docker PostgreSQL Won't Start

**Error:** `Port 55419 already in use`

**Solution:**
```bash
# Find process using the port
lsof -i :55419

# Stop existing container
docker-compose down

# Or change port in compose.yaml
```

### Issue: LM Studio Connection Refused

**Error:** `Connection refused: http://localhost:1234`

**Solution:**
1. Verify LM Studio server is running
2. Check the port in LM Studio settings
3. Ensure firewall isn't blocking the connection
4. Test with: `curl http://localhost:1234/v1/models`

### Issue: Embedding Dimension Mismatch

**Error:** `expected 768 dimensions, got 384`

**Solution:**
Check your embedding model in LM Studio. Different models produce different dimensions:
- nomic-embed-text: 768
- all-MiniLM-L6-v2: 384

Update `application.properties` if needed:
```properties
spring.ai.vectorstore.pgvector.dimensions=384
```

### Issue: Out of Memory During Ingestion

**Error:** `java.lang.OutOfMemoryError: Java heap space`

**Solution:**
```bash
# Increase heap size
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx4g"

# Or limit chunks in IngestionService
maxNumChunks: 5000  // Reduce from 10000
```

### Issue: No Documents Found

**Check:**
1. PDFs exist in `src/main/resources/docs/`
2. File is named `article.pdf` (or update `@Value` annotation)
3. PDFs are not encrypted or password-protected
4. Check logs for file reading errors
5. **Verify `financerag.ingest.enabled=true` in application.properties**

### Issue: Ingestion Not Running

**Error:** Application starts but no ingestion logs appear

**Solution:**
Check that `financerag.ingest.enabled=true` in `application.properties`. This property controls the `@ConditionalOnProperty` annotation:

```properties
# This must be set to true for first run
financerag.ingest.enabled=true
```

If you previously set it to `false`, change it back to `true` and restart.

### Issue: Duplicate Embeddings on Restart

**Problem:** Vector store grows with duplicate entries

**Solution:**
After the first successful run, set:
```properties
financerag.ingest.enabled=false
```

This prevents re-ingestion on every restart. The embeddings persist in PostgreSQL.

---

## 🎯 Use Cases

- **Financial Analysis**: Query quarterly reports, earnings calls, market analyses
- **Compliance**: Search regulatory documents and policy papers
- **Research**: Extract insights from academic papers and whitepapers
- **Legal**: Review contracts and legal documents
- **Customer Support**: Answer questions from product documentation
- **Knowledge Management**: Build searchable corporate knowledge bases

---

## 🔮 Future Enhancements

- [ ] **Multi-format Support**: DOCX, TXT, HTML, Markdown ingestion
- [ ] **Dynamic Ingestion**: REST API to upload documents at runtime
- [ ] **Web UI**: React/Vue frontend for interactive querying
- [ ] **Batch Processing**: Parallel document processing for large collections
- [ ] **Citation Tracking**: Return source references with answers
- [ ] **Conversation Memory**: Multi-turn dialogue support
- [ ] **Metadata Filtering**: Search by date, category, or custom tags
- [ ] **Authentication**: Secure API with JWT/OAuth2
- [ ] **Export Functionality**: Save conversations to PDF/DOCX
- [ ] **Analytics Dashboard**: Query metrics and usage statistics
- [ ] **Cloud Deployment**: Kubernetes deployment configs
- [ ] **Alternative Vector Stores**: Pinecone, Weaviate, Qdrant support

---

## 📚 Learning Resources

### Official Documentation
- [Spring AI Reference](https://docs.spring.io/spring-ai/reference/)
- [pgvector GitHub](https://github.com/pgvector/pgvector)
- [Spring Boot Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/)

### Tutorials & Articles
- [Dan Vega's Spring AI Tutorials](https://www.danvega.dev/) - Excellent content on RAG and Spring AI
- [RAG Pattern Overview](https://www.anthropic.com/index/retrieval-augmented-generation)
- [Vector Databases Explained](https://www.pinecone.io/learn/vector-database/)

### Tools
- [LM Studio](https://lmstudio.ai/) - Local LLM hosting
- [pgAdmin](https://www.pgadmin.org/) - PostgreSQL administration
- [Postman](https://www.postman.com/) - API testing

---

## 🤝 Contributing

Contributions are welcome! Here's how you can help:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Development Guidelines
- Follow existing code style and conventions
- Add tests for new features
- Update documentation as needed
- Keep commits atomic and well-described

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

## 🙏 Acknowledgments

- **Spring AI Team** - For the excellent framework that makes RAG accessible
- **pgvector Contributors** - For bringing vector search to PostgreSQL
- **Dan Vega** - For inspiring Spring AI content and tutorials
- **LM Studio Team** - For making local LLM hosting simple
- **The Open Source Community** - For continuous innovation in AI/ML

---

## 📞 Support & Contact

- **Issues**: [GitHub Issues](https://github.com/abhijith-zero/FinanceRag/issues)
- **Discussions**: [GitHub Discussions](https://github.com/abhijith-zero/FinanceRag/discussions)

---

<div align="center">

**Built with ❤️ using Spring Boot and Spring AI**

⭐ Star this repo if you find it helpful!

[⬆ Back to Top](#financera-)

</div>
