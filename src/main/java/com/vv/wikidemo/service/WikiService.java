package com.vv.wikidemo.service;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.vv.wikidemo.model.DefinitionRequest;
import com.vv.wikidemo.model.DefinitionResult;
import org.springframework.stereotype.Service;

@Service
public class WikiService {

    private final AgentPlatform                     agentPlatform;
    private final AgentInvocation<DefinitionResult> invocation;

    public WikiService( AgentPlatform agentPlatform ) {
        this.agentPlatform = agentPlatform;
        this.invocation = AgentInvocation
                .builder( agentPlatform )
                .build( DefinitionResult.class );
    }

    public DefinitionResult define( String term ) {
        return invocation.invoke( new DefinitionRequest( term ) );
    }
}