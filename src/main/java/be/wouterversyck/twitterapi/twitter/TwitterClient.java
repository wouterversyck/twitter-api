package be.wouterversyck.twitterapi.twitter;

import be.wouterversyck.twitterapi.twitter.models.Tweet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Optional;

@Slf4j
public class TwitterClient {

    private WebClient webClient;
    static final String TWITTER_PARAMS = "twitter_params";

    public TwitterClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<Tweet> stream(String... hashTags) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        String tracks = String.join(",", hashTags);
        body.add("track", tracks);

        return webClient
                .post()
                .uri(TwitterEndpoints.STREAM)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .attribute(TWITTER_PARAMS, body)
                .body(BodyInserters.fromFormData(body))
                .exchange()
                .filter(this::isEnhanceYourCalmStatus)
                .flatMapMany(clientResponse -> clientResponse.bodyToFlux(Tweet.class))
                .doFinally((signal) -> log.info("Stream closed, {}", signal.name()));
    }

    private boolean isEnhanceYourCalmStatus(ClientResponse clientResponse) {
        // Twitter status meaning slow the f*** down, made to many requests
        return clientResponse.statusCode().value() != 420;
    }
}
