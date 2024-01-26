package com.review.viktor.fileservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Permissions {
    private boolean admin;
    private boolean maintain;
    private boolean push;
    private boolean triage;
    private boolean pull;
}
