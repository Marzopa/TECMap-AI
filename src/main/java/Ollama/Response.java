package Ollama;

public record Response(String parsedLLMAnswer, String feedback, int grade) {
}
