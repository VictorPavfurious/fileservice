package com.review.viktor.fileservice.domain.controller;


import com.review.viktor.fileservice.domain.PopularWordService;
import com.review.viktor.fileservice.domain.response.PopularWordDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final PopularWordService popularWordService;

    public FileController(PopularWordService popularWordService) {
        this.popularWordService = popularWordService;
    }

    @GetMapping
    public ResponseEntity<PopularWordDTO> getPopularWords(@RequestParam String path, @RequestParam String token) {
        return ResponseEntity.ok(popularWordService.getPopularWordsByUrlAndCount(path, 3, token));
    }
}
