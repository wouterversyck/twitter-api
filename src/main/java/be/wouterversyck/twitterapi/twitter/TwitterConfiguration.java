package be.wouterversyck.twitterapi.twitter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

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
                .filter((currentRequest, next) ->
                        next.exchange(ClientRequest.from(currentRequest)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                            oAuth.oAuth1Header(
                                                currentRequest.url(),
                                                currentRequest.method(),
                                                    ((LinkedMultiValueMap<String, String>)currentRequest.attribute(OAuth.PARAMS).get()).toSingleValueMap()
                                            )
                                ).build())
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
}
