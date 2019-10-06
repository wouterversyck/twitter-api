package be.wouterversyck.twitterapi.controllers;

import be.wouterversyck.twitterapi.twitter.TwitterClient;
import be.wouterversyck.twitterapi.twitter.models.Tweet;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class TwitterController {
    private final TwitterClient twitterClient;

    public TwitterController(TwitterClient twitterClient) {
        this.twitterClient = twitterClient;
    }

    @GetMapping(value = "tweet")
    public Flux<ServerSentEvent<Tweet>> twitterStream(@RequestParam("track") String... hashTags) {

        return Flux.from(
                twitterClient.stream(hashTags)
                        .map(e -> ServerSentEvent.<Tweet>builder()
                                .data(e)
                                .id(e.getId())
                                .build()
                        )
        ).onErrorReturn(
                ServerSentEvent.<Tweet>builder()
                    .event("onerror")
                    .data(null)
                    .build()
        );
    }
}
