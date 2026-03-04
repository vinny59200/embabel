package com.vv.wikidemo.model;

public record WikidataEntityDetails(
        String label,
        String description,
        String wikipediaTitle
) {}