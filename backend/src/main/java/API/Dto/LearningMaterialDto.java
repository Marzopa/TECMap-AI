package API.Dto;

import java.util.List;

public class LearningMaterialDto {
    public String uuid;
    public String title;
    public String content;
    public boolean answerable;
    public boolean approved;
    public AssessmentItemDto assessmentItem;
    public List<String> tags;
}
