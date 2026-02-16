package com.example.finaceRag;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class IngestionService implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(IngestionService.class);
    private final VectorStore vectorStore;
    @Value("classpath:/docs/article_thebeatoct2024.pdf")
    private Resource pdfResource;
    public IngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }
    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting data ingestion process...");
        var pdfreader= new ParagraphPdfDocumentReader(pdfResource);
        TextSplitter splitter = new TokenTextSplitter();
        vectorStore.accept(splitter.apply(pdfreader.get()));
        logger.info("Vector store updated with PDF content.");
    }

    
}