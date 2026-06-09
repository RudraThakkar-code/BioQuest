package com.gate.bioquest.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class OllamaClient {

    private final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generate(String model, String prompt) {
        try {
            URL url = new URL(OLLAMA_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            String jsonInputString = String.format("{\"model\": \"%s\", \"prompt\": \"%s\", \"stream\": false}", model, escapeJson(prompt));

            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            if (code == 200) {
                JsonNode root = objectMapper.readTree(conn.getInputStream());
                return root.path("response").asText();
            } else {
                return "Error from Ollama: " + code;
            }
        } catch (Exception e) {
            return "Failed to connect to Ollama. Ensure it is running on localhost:11434. Error: " + e.getMessage();
        }
    }

    private String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }
}
