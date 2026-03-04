package com.vv.wikidemo.model;

public record DefinitionResult(
        String term,
        String entityId,
        String label,
        String description,
        String wikidataUrl,
        String wikipediaUrl
) {
}
