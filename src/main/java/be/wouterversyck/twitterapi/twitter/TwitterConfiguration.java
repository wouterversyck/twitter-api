package be.wouterversyck.twitterapi.twitter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

import static be.wouterversyck.twitterapi.twitter.TwitterClient.TWITTER_PARAMS;


@Configuration
public class TwitterConfiguration {
    // Provide data with environment variables
    @Value("${CONSUMER_KEY}")
    private String consumerKey;
    @Value("${CONSUMER_SECRET}")
    private String consumerSecret;
    @Value("${TOKEN}")
    private String token;
    @Value("${TOKEN_SECRET}")
    private String tokenSecret;

    @Bean
    public WebClient webClient(OAuth oAuth) {

        return WebClient.builder()
                .filter((currentRequest, next) -> {
                    if(currentRequest.attribute(TWITTER_PARAMS).isEmpty()) {
                        throw new IllegalStateException("Body params myst be set to complete Twitter OAuth");
                    }
                    Map<String, String> map = attributesToTwitterBodyParams(currentRequest.attribute(TWITTER_PARAMS).get());
                    return next.exchange(
                            ClientRequest.from(currentRequest)
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    oAuth.oAuth1Header(
                                            currentRequest.url(),
                                            currentRequest.method(),
                                            map
                                    )
                            ).build());
                    }

                ).build();
    }

    @Bean
    public OAuth twitterOauth() {
        return new OAuth(
                consumerKey,
                consumerSecret,
                token,
                tokenSecret
        );
    }

    @Bean
    public TwitterClient twitterClient(WebClient webClient) {
        return new TwitterClient(webClient);
    }

    private Map<String, String> attributesToTwitterBodyParams(Object multiMap) {
        //noinspection unchecked
        return ((LinkedMultiValueMap<String, String>)multiMap).toSingleValueMap();
    }
}
