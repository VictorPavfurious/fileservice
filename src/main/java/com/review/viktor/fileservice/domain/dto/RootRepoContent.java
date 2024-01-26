package com.review.viktor.fileservice.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RootRepoContent {
    private String name;
    private String path;
    private String sha;
    private int size;
    private String url;
    private String html_url;
    private String git_url;
    private String download_url;
    private String type;
    @JsonProperty("_links")
    private Links links;
}
