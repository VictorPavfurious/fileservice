package com.review.viktor.fileservice.domain.service;

import com.review.viktor.fileservice.domain.PopularWordService;
import com.review.viktor.fileservice.domain.response.PopularWordDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PopularWordServiceImpl implements PopularWordService {
    private final String GITHUB_API_BASE_URL = "https://api.github.com/";
    private final static String TOKEN_GIT_HUB = "Bearer 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";
    private static final String FILE_README_NAME = "README";
    private static final String LENGTH_WORD_REGEX_PATTERN = "\\b\\w{5,}\\b";


    @Override
    public PopularWordDTO getPopularWordsByUrlAndCount(String url, Integer countWords) {
        final List<String> allFileReadmeContents = new ArrayList<>();
        final RestTemplate restTemplate = new RestTemplate();
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", TOKEN_GIT_HUB);
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<String> entity = new HttpEntity<>(headers);
        String gitHubApiURL = buildAPIUrlToGitHub(url);

        ResponseEntity<List<Map<String, Object>>> reposResponse = restTemplate.exchange(
                gitHubApiURL, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {}
        );

        for (Map<String, Object> repo : Objects.requireNonNull(reposResponse.getBody())) {
            String contentFullURl = (String) repo.get("contents_url");
            String contentURl = contentFullURl.replace("/{+path}", "");

            ResponseEntity<List<Map<String, Object>>> contentResponse = restTemplate.exchange(
                    contentURl, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});

            // get file only readme file from repository contents
            Optional<String> fileReadMe  = Objects.requireNonNull(contentResponse.getBody()).stream()
                    .map(val -> String.valueOf(val.get("name")))
                    .filter(readMeFile -> readMeFile.startsWith(FILE_README_NAME))
                    .findFirst();

            // if fileReadme exist -> get response content and decode it
            if (fileReadMe.isPresent()) {
                String readmeUrl = contentFullURl.replace("{+path}", fileReadMe.get());
                ResponseEntity<Map<String, Object>> readmeResponse = restTemplate.exchange(
                        readmeUrl, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});

                String readmeContent = (String) Objects.requireNonNull(readmeResponse.getBody()).get("content");
                if (readmeContent != null) {
                    try {
                        String decodedContent = new String(Base64.getMimeDecoder().decode(readmeContent.getBytes()));
                        allFileReadmeContents.add(decodedContent);
                    } catch (Exception e) {
                        log.error("Has errors with decode file readme reason {}", e.getMessage());
                        // idk probably need to throw error if we have some trouble with decode? Or continue like now..
                    }
                }
            }
        }

        return new PopularWordDTO(getMostPopularWordsFromContents(allFileReadmeContents, countWords));
    }

    private List<String> getMostPopularWordsFromContents(List<String> contents, Integer countWords) {
        final Map<String, Integer> countWordsMap = new HashMap<>();
        for (String content: contents) {
            Matcher matcher = Pattern.compile(LENGTH_WORD_REGEX_PATTERN).matcher(content);
            while (matcher.find()) {
                String word = matcher.group();
                countWordsMap.merge(word, 1, Integer::sum);
            }
        }

        return getFirstWordsByNumber(countWords, countWordsMap);
    }

    private List<String> getFirstWordsByNumber(Integer limit, Map<String, Integer> countWordsMap) {
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
