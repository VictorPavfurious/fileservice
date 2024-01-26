package com.review.viktor.fileservice.domain.service;

import com.review.viktor.fileservice.domain.PopularWordService;
import com.review.viktor.fileservice.domain.dto.ReadmeRepoContent;
import com.review.viktor.fileservice.domain.dto.RootRepo;
import com.review.viktor.fileservice.domain.dto.RootRepoContent;
import com.review.viktor.fileservice.domain.response.PopularWordDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PopularWordServiceImpl implements PopularWordService {
    private final String GITHUB_API_BASE_URL = "https://api.github.com/";
    private static final String FILE_README_NAME = "README";
    @Value("${token.git.auth}")
    private String tokenGit;
    @Value("${amount.threads.pool:5}")
    private Integer amountThreads;

    private final RestTemplate restTemplate;
    public PopularWordServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public PopularWordDTO getPopularWordsByUrlAndCountAndLength(String url, Integer countWords, Integer lengthWord) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + tokenGit);
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<String> httpEntities = new HttpEntity<>(headers);
        final String gitHubApiURL = buildAPIUrlToGitHub(url);
        final ResponseEntity<List<RootRepo>> reposResponse = restTemplate.exchange(
                gitHubApiURL, HttpMethod.GET, httpEntities, new ParameterizedTypeReference<>() {}
        );
        final List<String> allFileReadmeContents = readAllContentAndAddResultToList(reposResponse, httpEntities);
        final List<String> mostPopularWordsFromContents = getMostPopularWordsFromContents(allFileReadmeContents, countWords, lengthWord);
        return new PopularWordDTO(mostPopularWordsFromContents);
    }

    private List<String> readAllContentAndAddResultToList(ResponseEntity<List<RootRepo>> repositories, HttpEntity<String> httpEntities) {
        final List<RootRepo> repos = Objects.requireNonNullElse(repositories.getBody(), Collections.emptyList());
        final ExecutorService executorService = Executors.newFixedThreadPool(amountThreads);
        final List<Future<String>> contentResult = new CopyOnWriteArrayList<>();
        try {
            for (RootRepo repo : repos) {
                contentResult.add(executorService.submit(() -> getFileReadmeContent(repo, httpEntities)));
            }
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return contentResult.stream()
                .map(this::getValueFromFuture)
                .collect(Collectors.toList());
    }

    private String getValueFromFuture(Future<String> val) {
        try {
            return val.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFileReadmeContent(RootRepo repo, HttpEntity<String> httpEntities) {
        final StringBuilder resultBuilder = new StringBuilder();
        String contentFullURL = repo.getContents_url();
        String contentURl = contentFullURL.replace("/{+path}", "");

        ResponseEntity<List<RootRepoContent>> contentResponse = restTemplate.exchange(
                contentURl, HttpMethod.GET, httpEntities, new ParameterizedTypeReference<>() {});

        if (Optional.ofNullable(contentResponse.getBody()).isPresent()) {
            // get file only readme file from repository contents
            Optional<String> fileReadMe  = contentResponse.getBody().stream()
                    .map(RootRepoContent::getName)
                    .filter(readMeFile -> readMeFile.startsWith(FILE_README_NAME))
                    .findFirst();

            // if fileReadme exist -> get response content and decode it
            if (fileReadMe.isPresent()) {
                String readmeUrl = contentFullURL.replace("{+path}", fileReadMe.get());
                log.info("Prepare to send request via url {}", readmeUrl);
                ResponseEntity<ReadmeRepoContent> readmeResponse = restTemplate.exchange(
                        readmeUrl, HttpMethod.GET, httpEntities, new ParameterizedTypeReference<>() {});

                Optional<ReadmeRepoContent> readMeRepoContent = Optional.ofNullable(readmeResponse.getBody());
                if (readMeRepoContent.isPresent()) {
                    try {
                        final String readmeContent = readMeRepoContent.get().getContent();
                        resultBuilder.append(new String(Base64.getMimeDecoder().decode(readmeContent.getBytes())));
                    } catch (Exception e) {
                        log.error("Has errors with decode file readme reason {}", e.getMessage());
                    }
                }
            }
        }
        return resultBuilder.toString();
    }

    private List<String> getMostPopularWordsFromContents(List<String> contents, Integer countWords, int lengthWord) {
        final Map<String, Integer> countWordsMap = new HashMap<>();
        for (String content: contents) {
            Matcher matcher = Pattern.compile(String.format("\\b\\w{%d,}\\b", lengthWord)).matcher(content);
            while (matcher.find()) {
                String word = matcher.group();
                countWordsMap.merge(word, 1, Integer::sum);
            }
        }

        return getMostPopularWordsByLimit(countWords, countWordsMap);
    }

    private List<String> getMostPopularWordsByLimit(Integer limit, Map<String, Integer> countWordsMap) {
        return countWordsMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private String buildAPIUrlToGitHub(String url) {
        //https://api.github.com/orgs/spotify/repos
        String[] urlParts = url.split("/");
        String repositoryName = urlParts[urlParts.length - 1];
        return GITHUB_API_BASE_URL + "orgs/" + repositoryName + "/repos";
    }
}
