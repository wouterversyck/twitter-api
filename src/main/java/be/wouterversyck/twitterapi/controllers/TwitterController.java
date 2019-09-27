package be.wouterversyck.twitterapi.controllers;

import be.wouterversyck.twitterapi.twitter.TwitterClient;
import be.wouterversyck.twitterapi.twitter.models.Tweet;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
public class TwitterController {
    private final TwitterClient twitterClient;

    public TwitterController(TwitterClient twitterClient) {
        this.twitterClient = twitterClient;
    }

    @GetMapping(value = "tweet", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Tweet> twitterStream(@RequestParam("track") String... hashTags) {
        return twitterClient.stream(hashTags);
    }
}
