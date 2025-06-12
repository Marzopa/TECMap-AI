package API.Dto;

import Ollama.GradingStatus;

public class AssessmentRecordDto {
    public String uuid;
    public GradingStatus score;
    public String studentAnswer;
    public int studentId;
    public String feedback;
}

