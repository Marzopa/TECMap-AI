package Ollama;

public record GradingResponse(String feedback, GradingStatus grade, String detectedLanguage) {
}
