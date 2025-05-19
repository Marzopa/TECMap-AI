package Ollama;

public class OllamaResponseParser {
    public static String parseResponse(String input) {
        StringBuilder finalResponse = new StringBuilder();
        String[] lines = input.split("\\r?\\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            int startIndex = line.indexOf("\"content\":\"");
            if (startIndex == -1) continue;
            startIndex += 11;
            int endIndex = line.indexOf("\"", startIndex);
            if (endIndex == -1) continue;
            finalResponse.append(line.substring(startIndex, endIndex));
        }
        return finalResponse.toString();
    }
}
