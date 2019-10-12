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

import static java.lang.String.format;

public class OAuth {

    private static final String OAUTH1_HEADER_AUTH_TYPE = "OAuth";
    private static final String OAUTH_TOKEN = "oauth_token";
    private static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
    private static final String OAUTH_SIGNATURE = "oauth_signature";
    private static final String OAUTH_NONCE = "oauth_nonce";
    private static final String OAUTH_TIMESTAMP = "oauth_timestamp";
    private static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
    private static final String OAUTH_VERSION = "oauth_version";
    private static final String HMAC_SHA1 = "HMAC-SHA1";
    private static final String OAUTH_VERSION_ONE = "1.0";

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
        long timestampSecs = generateTimestamp();
        String nonce = generateNonce();

        OAuthParams.OAuth1Params oAuth1Params = new OAuthParams.OAuth1Params(
                token,
                consumerKey,
                nonce,
                timestampSecs,
                Long.toString(timestampSecs),
                "",
                HMAC_SHA1,
                OAUTH_VERSION_ONE
        );

        String normalized = normalizer.normalize(
                requestUri.getScheme(),
                requestUri.getHost(),
                getPort(requestUri),
                httpMethod.name().toUpperCase(),
                requestUri.getPath(),
                getRequestParams(bodyParams),
                oAuth1Params
        );

        Map<String, String> oauthHeaders = Map.of(
                OAUTH_CONSUMER_KEY, quoted(consumerKey),
                OAUTH_TOKEN, quoted(token),
                OAUTH_SIGNATURE, quoted(getSignature(normalized)),
                OAUTH_SIGNATURE_METHOD, quoted(HMAC_SHA1),
                OAUTH_TIMESTAMP, quoted(Long.toString(timestampSecs)),
                OAUTH_NONCE, quoted(nonce),
                OAUTH_VERSION, quoted(OAUTH_VERSION_ONE)
        );

        return formatHeader(oauthHeaders);
    }

    private List<Request.Pair> getRequestParams(Map<String, String> bodyParams) {
        return bodyParams.entrySet().stream()
                .map(entry -> new Request.Pair(
                        urlEncode(entry.getKey()),
                        urlEncode(entry.getValue()))
                )
                .collect(Collectors.toList());
    }

    private String getSignature(String normalized) {
        try {
            return signer.getString(normalized, tokenSecret, consumerSecret);
        } catch (InvalidKeyException | NoSuchAlgorithmException invalidKeyEx) {
            throw new RuntimeException(invalidKeyEx);
        }
    }

    private String formatHeader(Map<String, String> oauthHeaders) {
        return format("%s %s",
                OAUTH1_HEADER_AUTH_TYPE,
                oauthHeaders.entrySet().stream()
                        .map(Map.Entry::toString)
                        .collect(Collectors.joining(", "))
        );
    }

    private int getPort(URI requestUri) {
        int port = requestUri.getPort();
        if(port > 0) {
            // port already set
            return port;
        }

        if(!requestUri.getScheme().equalsIgnoreCase("https")) {
            throw new IllegalStateException("Bad URI scheme: " + requestUri.getScheme());
        }

        return 443;
    }

    private String quoted(String str) {
        return "\"" + str + "\"";
    }

    private long generateTimestamp() {
        long timestamp = System.currentTimeMillis();
        return timestamp / 1000L;
    }

    private String generateNonce() {
        return Long.toString(Math.abs(secureRandom.nextLong())) + System.currentTimeMillis();
    }

    private String urlEncode(String source) {
        return UriUtils.encode(source, StandardCharsets.UTF_8);
    }

}