package be.wouterversyck.twitterapi.twitter;

import com.twitter.joauth.Normalizer;
import com.twitter.joauth.OAuthParams;
import com.twitter.joauth.Request;
import com.twitter.joauth.Signer;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

public class OAuth {

    private static final String OAUTH1_HEADER_AUTHTYPE = "OAuth ";
    private static final String OAUTH_TOKEN = "oauth_token";
    private static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
    private static final String OAUTH_SIGNATURE = "oauth_signature";
    private static final String OAUTH_NONCE = "oauth_nonce";
    private static final String OAUTH_TIMESTAMP = "oauth_timestamp";
    private static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
    private static final String OAUTH_VERSION = "oauth_version";
    private static final String HMAC_SHA1 = "HMAC-SHA1";
    private static final String ONE_DOT_OH = "1.0";

    private final String consumerKey;
    private final String consumerSecret;
    private final String token;
    private final String tokenSecret;
    private final Normalizer normalizer;
    private final Signer signer;
    private final SecureRandom secureRandom;

    public OAuth(final String consumerKey, final String consumerSecret, final String token, final String tokenSecret) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.token = token;
        this.tokenSecret = tokenSecret;
        this.normalizer = Normalizer.getStandardNormalizer();
        this.signer = Signer.getStandardSigner();
        this.secureRandom = new SecureRandom();
    }

    String oAuth1Header(URI requestUri, HttpMethod httpMethod, Map<String, String> bodyParams) {
        List<Request.Pair> requestParams = new ArrayList<>(bodyParams.size());
        for (Map.Entry<String, String> entry : bodyParams.entrySet()) {
            requestParams.add(new Request.Pair(urlEncode(entry.getKey()), urlEncode(entry.getValue())));
        }

        long timestampSecs = this.generateTimestamp();
        String nonce = this.generateNonce();
        OAuthParams.OAuth1Params oAuth1Params = new OAuthParams.OAuth1Params(this.token, this.consumerKey, nonce, timestampSecs,
                Long.toString(timestampSecs), "", HMAC_SHA1, ONE_DOT_OH);

        int port = requestUri.getPort();

        if(port <= 0) {
            if(!requestUri.getScheme().equalsIgnoreCase("https")) {
                throw new IllegalStateException("Bad URI scheme: " + requestUri.getScheme());
            }
            port = 443;
        }

        String normalized = this.normalizer.normalize(requestUri.getScheme(), requestUri.getHost(), port, httpMethod.name().toUpperCase(),
                requestUri.getPath(), requestParams, oAuth1Params);

        String signature;
        try {
            signature = this.signer.getString(normalized, this.tokenSecret, this.consumerSecret);
        } catch (InvalidKeyException | NoSuchAlgorithmException invalidKeyEx) {
            throw new RuntimeException(invalidKeyEx);
        }

        Map<String, String> oauthHeaders = new HashMap<>();
        oauthHeaders.put(OAUTH_CONSUMER_KEY, this.quoted(this.consumerKey));
        oauthHeaders.put(OAUTH_TOKEN, this.quoted(this.token));
        oauthHeaders.put(OAUTH_SIGNATURE, this.quoted(signature));
        oauthHeaders.put(OAUTH_SIGNATURE_METHOD, this.quoted(HMAC_SHA1));
        oauthHeaders.put(OAUTH_TIMESTAMP, this.quoted(Long.toString(timestampSecs)));
        oauthHeaders.put(OAUTH_NONCE, this.quoted(nonce));
        oauthHeaders.put(OAUTH_VERSION, this.quoted(ONE_DOT_OH));

        return OAUTH1_HEADER_AUTHTYPE
                + oauthHeaders.entrySet().stream().map(Map.Entry::toString).collect(Collectors.joining(", "));
    }

    private String quoted(String str) {
        return "\"" + str + "\"";
    }

    private long generateTimestamp() {
        long timestamp = System.currentTimeMillis();
        return timestamp / 1000L;
    }

    private String generateNonce() {
        return Long.toString(Math.abs(this.secureRandom.nextLong())) + System.currentTimeMillis();
    }

    private String urlEncode(String source) {
        return UriUtils.encode(source, StandardCharsets.UTF_8);
    }

}