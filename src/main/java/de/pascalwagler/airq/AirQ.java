package de.pascalwagler.airq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.pascalwagler.airq.exception.AirQCommunicationException;
import de.pascalwagler.airq.exception.AirQEncryptionException;
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

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final HttpRequest requestGetData;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String READ_ERROR = "Error while reading data from air-Q.";
    private static final String ENCRYPTION_ERROR = "Error while decrypting data from air-Q. The error is likely caused by the wrong password.";

    public AirQ(String host, String password) throws URISyntaxException {
        this.host = host;
        this.password = password;
        this.requestGetData = HttpRequest.newBuilder()
                .uri(new URI("http://" + this.host + "/data"))
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.of(10, SECONDS))
                .GET()
                .build();
    }

    public String getDataAsString() {
        try {
            HttpResponse<String> response = httpClient.send(requestGetData, HttpResponse.BodyHandlers.ofString());
            AirQContent airQContent = objectMapper.readValue(response.body(), AirQContent.class);
            return decrypt(airQContent.getContent(), password);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new AirQCommunicationException(READ_ERROR, e);
        }
        return null;
    }

    public JsonNode getDataAsJsonNode() {
        try {
            return objectMapper.readTree(getDataAsString());
        } catch (Exception e) {
            throw new AirQCommunicationException(READ_ERROR, e);
        }
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

        // If the plaintext is too short or the first letter is not the character '{' then something went wrong.
        // This can happen if the wrong password is used for decryption.
        if (plainText.length < 1 || plainText[0] != 123) {
            throw new AirQEncryptionException(ENCRYPTION_ERROR);
        }
        return new String(plainText, StandardCharsets.UTF_8);
    }

    /**
     * Truncates or pads the password to obtain a String with the length of 32 characters.
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
