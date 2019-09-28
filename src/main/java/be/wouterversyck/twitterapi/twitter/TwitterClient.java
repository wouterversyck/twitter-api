package be.wouterversyck.twitterapi.twitter;

import be.wouterversyck.twitterapi.twitter.models.Tweet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

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
                .flatMapMany(clientResponse -> clientResponse.bodyToFlux(Tweet.class));
    }
}
