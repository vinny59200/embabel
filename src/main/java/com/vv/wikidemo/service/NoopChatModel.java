package com.vv.wikidemo.service;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

public class NoopChatModel implements ChatModel {

    @Override
    public ChatResponse call( Prompt prompt ) {
        var msg = new AssistantMessage(
                "NOOP LLM: no real LLM configured (this demo doesn't need one)."
        );
        return new ChatResponse( List.of( new Generation( msg ) ) );
    }
}
