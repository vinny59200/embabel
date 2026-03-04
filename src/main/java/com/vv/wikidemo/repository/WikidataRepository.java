package com.vv.wikidemo.repository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vv.wikidemo.model.WikidataEntityDetails;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class WikidataRepository {

    private final RestClient wikidata;

    public WikidataRepository( RestClient.Builder builder ) {
        this.wikidata = builder
                .baseUrl( "https://www.wikidata.org" )
                .defaultHeader( HttpHeaders.USER_AGENT, "wikidemo/0.0.1 (SpringBoot+Embabel demo)" )
                .build();
    }

    /**
     * Step 1: find the first matching Q-id for a term
     */
    public Optional<SearchItem> searchFirst( String term ) {
        SearchResponse response = wikidata.get()
                                          .uri( uriBuilder -> uriBuilder
                                                  .path( "/w/api.php" )
                                                  .queryParam( "action", "wbsearchentities" )
                                                  .queryParam( "search", term )
                                                  .queryParam( "language", "en" )
                                                  .queryParam( "format", "json" )
                                                  .queryParam( "limit", "1" )
                                                  .build() )
                                          .retrieve()
                                          .body( SearchResponse.class );

        if ( response == null || response.search == null || response.search.isEmpty() ) {
            return Optional.empty();
        }
        return Optional.ofNullable( response.search.get( 0 ) );
    }

    /**
     * Step 2: fetch label/description (and Wikipedia title if present) from Special:EntityData
     */
    public WikidataEntityDetails fetchEntityDetails( String entityId ) {
        EntityDataResponse data = wikidata.get()
                                          .uri( "/wiki/Special:EntityData/{id}.json", entityId )
                                          .retrieve()
                                          .body( EntityDataResponse.class );

        if ( data == null || data.entities == null || !data.entities.containsKey( entityId ) ) {
            return new WikidataEntityDetails( null, null, null );
        }

        Entity entity = data.entities.get( entityId );
        String label = valueOf( entity.labels, "en" );
        String desc = valueOf( entity.descriptions, "en" );
        String wikiTitle = (entity.sitelinks != null && entity.sitelinks.containsKey( "enwiki" ))
                ? entity.sitelinks.get( "enwiki" ).title
                : null;

        return new WikidataEntityDetails( label, desc, wikiTitle );
    }

    public static String wikidataUrl( String entityId ) {
        return "https://www.wikidata.org/wiki/" + entityId;
    }

    public static String wikipediaUrl( String title ) {
        if ( title == null || title.isBlank() ) {
            return null;
        }
        String normalized = title.replace( ' ', '_' );
        return "https://en.wikipedia.org/wiki/" + URLEncoder.encode( normalized, StandardCharsets.UTF_8 );
    }

    private static String valueOf( Map<String, LangValue> map, String lang ) {
        if ( map == null ) {
            return null;
        }
        LangValue lv = map.get( lang );
        return lv == null ? null : lv.value;
    }

    // --- DTOs for JSON mapping (minimal fields only) ---

    @JsonIgnoreProperties( ignoreUnknown = true )
    static class SearchResponse {
        @JsonProperty( "search" )
        public List<SearchItem> search;
    }

    @JsonIgnoreProperties( ignoreUnknown = true )
    public static class SearchItem {
        @JsonProperty( "id" )
        public String id;

        @JsonProperty( "label" )
        public String label;

        @JsonProperty( "description" )
        public String description;
    }

    @JsonIgnoreProperties( ignoreUnknown = true )
    static class EntityDataResponse {
        @JsonProperty( "entities" )
        public Map<String, Entity> entities;
    }

    @JsonIgnoreProperties( ignoreUnknown = true )
    static class Entity {
        @JsonProperty( "labels" )
        public Map<String, LangValue> labels;

        @JsonProperty( "descriptions" )
        public Map<String, LangValue> descriptions;

        @JsonProperty( "sitelinks" )
        public Map<String, Sitelink> sitelinks;
    }

    @JsonIgnoreProperties( ignoreUnknown = true )
    static class LangValue {
        @JsonProperty( "value" )
        public String value;
    }

    @JsonIgnoreProperties( ignoreUnknown = true )
    static class Sitelink {
        @JsonProperty( "title" )
        public String title;
    }
}