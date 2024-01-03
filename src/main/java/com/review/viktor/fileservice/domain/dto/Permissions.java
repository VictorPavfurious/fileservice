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
    public boolean admin;
    public boolean maintain;
    public boolean push;
    public boolean triage;
    public boolean pull;
}
