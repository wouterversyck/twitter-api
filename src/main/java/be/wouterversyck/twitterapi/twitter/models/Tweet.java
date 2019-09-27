package be.wouterversyck.twitterapi.twitter.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Tweet {
    @JsonProperty("id_str")
    private String id;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("text")
    private String text;

    @JsonProperty("user")
    private User user;
}
