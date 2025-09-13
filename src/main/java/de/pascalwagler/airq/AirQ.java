package de.pascalwagler.airq;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.pascalwagler.airq.exception.AirQCommunicationException;
import de.pascalwagler.airq.exception.AirQRuntimeException;
import de.pascalwagler.airq.model.airq.AirQContent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;

import static java.time.temporal.ChronoUnit.SECONDS;

@Slf4j
public class AirQ {

    @Getter
    private final String host;

    @Getter
    private final String password;

    public enum AirQRequest {
        DATA("data", true),
        CONFIG("config", true),
        STANDARDPASS("standardpass", false),
        SSID("ssid", true),
        AVERAGE("average", true),
        BLINK("blink", false),
        HEALTH("health", false),
        LOG("log", true),
        PING("ping", true),
        VERSION("version", false);

        private final String value;
        private final boolean isEncrypted;

        AirQRequest(String value, boolean isEncrypted) {
            this.value = value;
            this.isEncrypted = isEncrypted;
        }
    }


    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String READ_ERROR = "Error while reading data from air-Q.";

    public AirQ(String host, String password) {
        this.host = host;
        this.password = password;
    }

    @SuppressWarnings("squid:S1075")
    public String getResponseAsString(AirQRequest airQRequest) {
        HttpRequest request;
        //noinspection HttpUrlsUsage
        final String url = "http://" + this.host + "/" + airQRequest.value;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .version(HttpClient.Version.HTTP_1_1)
                    .timeout(Duration.of(10, SECONDS))
                    .GET()
                    .build();
        } catch (URISyntaxException e) {
            throw new AirQRuntimeException("The URL '" + url + "' is malformed.", e);
        }

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (airQRequest.isEncrypted) {
                AirQContent airQContent = objectMapper.readValue(response.body(), AirQContent.class);
                return decrypt(airQContent.getContent(), password);
            } else {
                return response.body();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new AirQCommunicationException(READ_ERROR, e);
        }
        return null;
    }

    public static String decrypt(String messageBase64, String password) throws InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException {

        String preparedPassword = preparePassword(password);
        byte[] message = Base64.getDecoder().decode(messageBase64);

        byte[] initializationVector = Arrays.copyOfRange(message, 0, 16);
        byte[] cypherText = Arrays.copyOfRange(message, 16, message.length);

        IvParameterSpec iv = new IvParameterSpec(initializationVector);
        SecretKey secretKey = new SecretKeySpec(preparedPassword.getBytes(StandardCharsets.UTF_8), "AES");
        // The air-Q sends padded data, so ignore the SonarLint warning about disabling padding.
        @SuppressWarnings("java:S5542")
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);

        byte[] plainText = cipher.doFinal(cypherText);
        return new String(plainText, StandardCharsets.UTF_8);
    }

    /**
     * Truncates or pads the password to get a String with the length of 32 characters.
     *
     * @param password the air-Q password
     * @return the truncated or padded password String with a length of 32
     */
    public static String preparePassword(String password) {
        String paddedPassword = password;
        if (paddedPassword.length() < 32) {
            paddedPassword = password + ("0".repeat(32 - password.length()));
        } else {
            if (password.length() > 32) {
                paddedPassword = password.substring(0, 23);
            }
        }
        return paddedPassword;
    }
}
