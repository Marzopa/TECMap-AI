import org.junit.Test;

import java.io.IOException;

public class LearningMaterialTest {
    @Test
    public void testLearningMaterial() {
        // Create a LearningMaterial object
        Classroom.LearningMaterial learningMaterial = new Classroom.LearningMaterial("Test Title", "Test Content");

        // Check if the title and content are set correctly
        assert "Test Title".equals(learningMaterial.getTitle());
        assert "Test Content".equals(learningMaterial.getContent());

        // Check if the UUID is generated
        assert learningMaterial.getUuid() != null;

        // Save the learning material to a file
        try {
            String filename = learningMaterial.saveToFile("src/test/resources/");
            System.out.println("Learning material saved to: " + filename);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

}
