package API.Dto;

import Ollama.GradingStatus;

public record GradingResponse(String feedback, GradingStatus grade, String detectedLanguage) {
}
