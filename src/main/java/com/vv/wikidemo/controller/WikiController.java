package com.vv.wikidemo.controller;

import com.vv.wikidemo.model.DefinitionResult;
import com.vv.wikidemo.service.WikiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping( "/api/wiki" )
public class WikiController {

    private final WikiService wikiService;

    public WikiController( WikiService wikiService ) {
        this.wikiService = wikiService;
    }

    @GetMapping( "/define" )
    public DefinitionResult define( @RequestParam( "term" ) String term ) {
        return wikiService.define( term );
    }
}