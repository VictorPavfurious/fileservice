package com.review.viktor.fileservice.domain;

import com.review.viktor.fileservice.domain.response.PopularWordDTO;

public interface PopularWordService {

    PopularWordDTO getPopularWordsByUrlAndCountAndLength(String url, Integer countWords, Integer lengthWord);
}
