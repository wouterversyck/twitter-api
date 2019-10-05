package be.wouterversyck.twitterapi.controllers;

import be.wouterversyck.twitterapi.twitter.TwitterClient;
import be.wouterversyck.twitterapi.twitter.models.Tweet;
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
    public Flux<Tweet> twitterStream(@RequestParam("track") String... hashTags) {
        if(hashTags.length == 0) {
            return Flux.empty();
        }
        return twitterClient.stream(hashTags);
    }
}
