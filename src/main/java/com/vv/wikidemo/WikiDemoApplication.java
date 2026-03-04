package com.vv.wikidemo;

import com.embabel.agent.config.annotation.EnableAgents;
import com.embabel.agent.spi.LlmService;
import com.embabel.agent.spi.support.springai.SpringAiLlmService;
import com.vv.wikidemo.service.NoopChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableAgents
public class WikiDemoApplication {
    public static void main( String[] args ) {
        SpringApplication.run( WikiDemoApplication.class, args );
    }

    @Bean
    public LlmService<?> noopLlm() {
        return new SpringAiLlmService(
                "noop",          // model name (must match embabel.models.default-llm)
                "noop-provider", // provider label (any string)
                new NoopChatModel()
        );
    }
}

