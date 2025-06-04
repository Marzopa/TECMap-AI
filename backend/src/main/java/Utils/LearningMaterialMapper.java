package Utils;

import Classroom.AssessmentItem;
import Classroom.AssessmentRecord;
import Classroom.LearningMaterial;
import API.dto.AssessmentItemDto;
import API.dto.AssessmentRecordDto;
import API.dto.LearningMaterialDto;

public class LearningMaterialMapper {
    public static LearningMaterialDto toDto(LearningMaterial entity) {
        LearningMaterialDto dto = new LearningMaterialDto();
        dto.uuid = entity.getUuid();
        dto.title = entity.getTitle();
        dto.content = entity.getContent();
        dto.answerable = entity.isAnswerable();
        dto.approved = entity.isApproved();
        dto.tags = entity.getTags();
        dto.assessmentItem = toDto(entity.getAssessmentItem());
        return dto;
    }

    public static AssessmentItemDto toDto(AssessmentItem entity) {
        if (entity == null) return null;
        AssessmentItemDto dto = new AssessmentItemDto();
        dto.uuid = entity.getUuid();
        dto.submissions = entity.getSubmissions().stream().map(LearningMaterialMapper::toDto).toList();
        return dto;
    }

    public static AssessmentRecordDto toDto(AssessmentRecord entity) {
        AssessmentRecordDto dto = new AssessmentRecordDto();
        dto.uuid = entity.getUuid();
        dto.score = entity.getScore();
        dto.studentAnswer = entity.getAnswer();
        dto.studentId = entity.getStudentId();
        dto.feedback = entity.getFeedback();
        return dto;
    }
}

