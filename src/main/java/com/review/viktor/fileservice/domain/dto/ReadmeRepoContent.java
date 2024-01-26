package com.review.viktor.fileservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReadmeRepoContent extends RootRepoContent {
    private String content;
    private String encoding;
}
