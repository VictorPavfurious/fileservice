package com.review.viktor.fileservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class License {
    public String key;
    public String name;
    public String spdx_id;
    public String url;
    public String node_id;
}
