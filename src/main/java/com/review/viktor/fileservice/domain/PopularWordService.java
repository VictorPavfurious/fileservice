package com.review.viktor.fileservice.domain;

import com.review.viktor.fileservice.domain.response.PopularWordDTO;

import java.util.List;

public interface PopularWordService {

    PopularWordDTO getPopularWordsByUrlAndCount(String url, Integer countWords, String tokenGit);
}
