package com.vv.wikidemo.service;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.vv.wikidemo.model.DefinitionRequest;
import com.vv.wikidemo.model.DefinitionResult;
import com.vv.wikidemo.model.WikidataEntityDetails;
import com.vv.wikidemo.model.WikidataEntityId;
import com.vv.wikidemo.repository.WikidataRepository;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Agent( description = "Define a word using Wikidata (no LLM, no auth)" )
public class WikidataDefinitionAgent {

    private final WikidataRepository repo;

    public WikidataDefinitionAgent( WikidataRepository repo ) {
        this.repo = repo;
    }

    @Action
    public WikidataEntityId findEntityId( DefinitionRequest request ) {
        var hit = repo.searchFirst( request.term() )
                      .orElseThrow( () -> new ResponseStatusException(
                              NOT_FOUND, "No Wikidata entity found for term: " + request.term()
                      ) );
        return new WikidataEntityId( hit.id );
    }

    @Action
    public WikidataEntityDetails fetchDetails( WikidataEntityId id ) {
        return repo.fetchEntityDetails( id.id() );
    }

    @Action
    @AchievesGoal( description = "Return a Wikidata-based definition" )
    public DefinitionResult build( DefinitionRequest request,
                                   WikidataEntityId id,
                                   WikidataEntityDetails details ) {

        String wikidataUrl = WikidataRepository.wikidataUrl( id.id() );
        String wikipediaUrl = WikidataRepository.wikipediaUrl( details.wikipediaTitle() );

        // If Wikidata doesn't have an English label/description, you still get a stable entity link.
        return new DefinitionResult(
                request.term(),
                id.id(),
                details.label(),
                details.description(),
                wikidataUrl,
                wikipediaUrl
        );
    }
}
