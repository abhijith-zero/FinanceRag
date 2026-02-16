# FinanceRag 📚🤖

A Spring Boot application demonstrating **Retrieval-Augmented Generation (RAG)** using **Spring AI**, **pgvector**, and **PDF document ingestion**. Query your financial documents using natural language with the power of LLMs and semantic search.

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-pgvector-blue.svg)](https://github.com/pgvector/pgvector)

---

## 🎯 Overview

**FinanceRag** enables intelligent document querying by combining:
- **PDF document ingestion** with automatic text extraction
- **Vector embeddings** for semantic search
- **PostgreSQL + pgvector** for efficient vector storage
- **Spring AI** for LLM integration
- **RAG architecture** to provide context-aware answers from your documents

---

## ✨ Features

- 📄 **PDF Ingestion**: Automatically read and process PDF documents
- 🔍 **Semantic Search**: Find relevant information using vector similarity
- 💾 **Vector Storage**: Persist embeddings in PostgreSQL with pgvector extension
- 🤖 **RAG Chat**: Answer questions using document context and LLM reasoning
- 🔧 **Configurable**: Easy integration with local LLMs (LM Studio) or cloud providers
- 📊 **Token-Based Splitting**: Intelligent text chunking for optimal embedding quality

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

Before you begin, ensure you have the following installed:

- **Java 17+** ([Download](https://adoptium.net/))
- **Maven 3+** ([Download](https://maven.apache.org/download.cgi))
- **PostgreSQL 12+** with `pgvector` extension ([Install pgvector](https://github.com/pgvector/pgvector))
- **LM Studio** or another local LLM endpoint ([Download LM Studio](https://lmstudio.ai/))

### 1. Clone the Repository

```bash
git clone https://github.com/abhijith-zero/FinanceRag.git
cd FinanceRag
```

### 2. Run the docker yaml file

- This sets up the pg database

### 3. Configure Application Properties

Update `src/main/resources/application.properties` with your database credentials:

```properties
# Database Configuration
spring.application.name=finaceRag
spring.datasource.url=jdbc:postgresql://localhost:55419/finance
spring.ai.openai.base-url=http://localhost:1234/
spring.ai.openai.api-key=dummy
spring.ai.openai.embedding.options.model=nomic-embed-text
spring.ai.openai.chat.options.model=google/gemma-3-4b
spring.ai.vectorstore.pgvector.initialize-schema=true

```

### 4. Add PDF Documents

Place your PDF files in the `src/main/resources/docs/` directory:

```
src/main/resources/docs/
├── article_thebeatoct2024.pdf
├── financial_report_q4.pdf
└── market_analysis.pdf
```

### 5. Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will:
1. ✅ Start the Spring Boot server
2. ✅ Connect to PostgreSQL
3. ✅ Read PDFs from the `docs/` folder
4. ✅ Generate embeddings and store them in the vector database
5. ✅ Be ready to answer questions!

---

## 📡 API Usage

### Chat Endpoint

**Endpoint:** `GET /chat`

**Query Parameter:** `question` (optional, defaults to a sample question)

#### Example Request

```bash
curl "http://localhost:8080/chat?question=How%20did%20the%20federal%20reserve%20policy%20impact%20asset%20classes?"
```

#### Example Response

```
Based on the analyzed documents, the Federal Reserve's recent policy decision 
had varied impacts across asset classes. Equities showed resilience with tech 
stocks outperforming, while fixed income markets experienced yield compression. 
Real estate investment trusts benefited from the lower rate environment...
```

### Test with Your Browser

Simply navigate to:

```
http://localhost:8080/chat?question=What are the key financial trends discussed?
```

---

## 🛠️ Configuration Options

### Text Splitting

Customize chunk size in `IngestionService.java`:

```java
TokenTextSplitter splitter = new TokenTextSplitter(800, 400, 5, 10000, true);
```

Parameters:
- `defaultChunkSize`: 800 tokens per chunk
- `minChunkSizeChars`: Minimum 400 characters
- `minChunkLengthToEmbed`: Skip chunks smaller than 5 tokens
- `maxNumChunks`: Process up to 10,000 chunks
- `keepSeparator`: Preserve paragraph boundaries

### Vector Store Settings

Modify vector store behavior in `application.properties`:

```properties
# HNSW index for faster similarity search
spring.ai.vectorstore.pgvector.index-type=HNSW

# Cosine similarity for semantic matching
spring.ai.vectorstore.pgvector.distance-type=COSINE_DISTANCE

# Embedding dimensions (depends on your model)
spring.ai.vectorstore.pgvector.dimensions=768
```

---

## 📁 Project Structure

```
FinanceRag/
├── src/
│   ├── main/
│   │   ├── java/com/example/finaceRag/
│   │   │   ├── FinanceRagApplication.java    # Main application entry point
│   │   │   ├── IngestionService.java         # PDF ingestion and vectorization
│   │   │   └── ChatController.java           # REST endpoint for RAG queries
│   │   └── resources/
│   │       ├── docs/                         # Place PDF documents here
│   │       └── application.properties        # Configuration file
│   └── test/
├── pom.xml                                    # Maven dependencies
└── README.md
```



## 🧪 Testing

### Manual Testing

1. Add a test PDF to `src/main/resources/docs/`
2. Restart the application
3. Query the chat endpoint:

```bash
curl "http://localhost:8080/chat?question=Summarize the main topics in the document"
```

### Verify Vector Store

Connect to PostgreSQL and check the stored vectors:

```sql
\c finance

-- View the vector store table
SELECT COUNT(*) FROM vector_store;

-- Inspect a sample embedding
SELECT id, content, metadata FROM vector_store LIMIT 5;
```

---

## 🚧 Troubleshooting

### Common Issues

**1. pgvector extension not found**
```
ERROR: extension "vector" does not exist
```
**Solution:** Install pgvector following the [official guide](https://github.com/pgvector/pgvector#installation)

**2. Connection refused to LM Studio**
```
Connection refused: http://localhost:1234
```
**Solution:** 
- Ensure LM Studio is running
- Check that the server is listening on port 1234
- Verify the base URL in `application.properties`

**3. Out of memory during PDF processing**
```
java.lang.OutOfMemoryError: Java heap space
```
**Solution:** Increase heap size:
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx4g"
```

**4. No documents ingested**
- Verify PDFs exist in `src/main/resources/docs/`
- Check application logs for ingestion errors
- Ensure PDFs are not encrypted or password-protected

---

## 🎯 Use Cases

- **Financial Document Q&A**: Query annual reports, research papers, and market analyses
- **Knowledge Base**: Build a searchable repository of company documents
- **Compliance**: Search through regulatory documents and policies
- **Research**: Extract insights from academic papers and whitepapers
- **Customer Support**: Provide AI-powered answers from product documentation

---

## 🔮 Future Enhancements

- [ ] **Multi-format Support**: Add support for DOCX, TXT, and Markdown files
- [ ] **Runtime Ingestion**: API endpoint to upload and process documents dynamically
- [ ] **Web Frontend**: Interactive UI for document management and querying
- [ ] **Multiple Vector Stores**: Support for Weaviate, Pinecone, and Chroma
- [ ] **Query Caching**: Redis-based caching for frequently asked questions
- [ ] **Metadata Filtering**: Filter searches by document type, date, or tags
- [ ] **Batch Processing**: Parallel processing of large document collections
- [ ] **Authentication**: Secure API access with JWT or OAuth2
- [ ] **Analytics Dashboard**: Track usage patterns and query performance

---

## 📚 Additional Resources

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [pgvector GitHub](https://github.com/pgvector/pgvector)
- [RAG Pattern Guide](https://www.anthropic.com/index/retrieval-augmented-generation)
- [LM Studio Documentation](https://lmstudio.ai/docs)

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 🙏 Acknowledgments

- Spring AI team for the excellent framework
- pgvector contributors for vector search capabilities
- LM Studio for local LLM hosting
- The open-source community
- Dan Vega for great content on the topic

---

## 📞 Support

Having issues? Please [open an issue](https://github.com/abhijith-zero/FinanceRag/issues) on GitHub.

For questions or discussions, join our [Discord community](https://discord.gg/your-invite).

---

<div align="center">

**Built with ❤️ using Spring Boot and Spring AI**

[⬆ Back to Top](#financrag-)

</div>
