package com.unipi.game_avenue.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SearchGameModel {

    private String title; // Normal and Advanced search

    private String platform; // Normal and Advanced search

    private String genre; // Normal search

    private String sort_by; // Normal search

    private Boolean basic_search; // Normal and Advanced search
}
