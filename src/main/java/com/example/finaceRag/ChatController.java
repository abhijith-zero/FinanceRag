package com.example.finaceRag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClient,PgVectorStore vectorStore) {
        this.chatClient = chatClient.
        defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build())
        .build();
    }
    
    @GetMapping("/chat")
    public String chat() {
        return chatClient.prompt().user("How did the federal reserve's recent policy decision impact various asset classes according to the analysis?").call().content();
    }
    
}
