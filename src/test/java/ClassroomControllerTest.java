import Classroom.*;

import Ollama.OllamaClient;
import Ollama.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClassroomControllerTest {

    private ClassroomController controller;

    @BeforeEach
    void setUp() {
        controller = new ClassroomController();
    }

    @Test
    void testGetProblem_ValidCredentials() throws IOException, InterruptedException {
        // Mock the static method in OllamaClient
        try (MockedStatic<OllamaClient> ollamaMock = Mockito.mockStatic(OllamaClient.class)) {
            // Mock the response from OllamaClient
            LearningMaterial mockMaterial = new LearningMaterial("Arrays", "Solve this problem", true);
            ollamaMock.when(() -> OllamaClient.createLearningMaterialFromResponse("arrays", 3))
                    .thenReturn(mockMaterial);

            // Add valid credentials to the controller
            ClassroomController.StudentCredential credential = new ClassroomController.StudentCredential("oscar", "password");
            ClassroomController.getCredentials().put("705123456", credential);

            // Call the method
            ClassroomController.ProblemResponse response = controller.getProblem("705123456", "password");

            // Verify the response
            assertNotNull(response);
            assertEquals("Arrays", response.title());
            assertEquals("Solve this problem", response.problem());
        }
    }

    @Test
    void testGetProblem_InvalidCredentials() {
        // Add invalid credentials
        ClassroomController.getCredentials().put("705123456", new ClassroomController.StudentCredential("oscar", "password"));

        // Call the method with invalid password
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                controller.getProblem("705123456", "wrongpassword"));

        // Verify the exception message
        assertEquals("Student credentials are not valid.", exception.getMessage());
    }

    @Test
    void testSubmitSolution_ValidSubmission() throws IOException, InterruptedException {
        // Mock the static method in OllamaClient
        try (MockedStatic<OllamaClient> ollamaMock = Mockito.mockStatic(OllamaClient.class)) {
            // Mock the response from OllamaClient
            Response mockResponse = new Response("Correct solution", "Good job!", 100);
            ollamaMock.when(() -> OllamaClient.solutionRequest(anyString(), anyString()))
                    .thenReturn(mockResponse);

            // Add valid credentials and a problem to the controller
            ClassroomController.StudentCredential credential = new ClassroomController.StudentCredential("oscar", "password");
            ClassroomController.getCredentials().put("705123456", credential);

            LearningMaterial mockMaterial = new LearningMaterial("Arrays", "Solve this problem", true);
            mockMaterial.setAssessmentItem(new AssessmentItem(100));
            ClassroomController.currentAssignments.put("705123456", mockMaterial);

            // Call the method
            ClassroomController.SubmissionResponse response = controller.submitSolution("705123456", "password", "solution");

            // Verify the response
            assertNotNull(response);
            assertEquals("Good job!", response.feedback());
            assertEquals(100, response.grade());
        }
    }

    @Test
    void testSubmitSolution_NoAssignedProblem() {
        // Add valid credentials
        ClassroomController.getCredentials().put("705123456", new ClassroomController.StudentCredential("oscar", "password"));

        // Call the method without assigning a problem
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                controller.submitSolution("705123456", "password", "solution"));

        // Verify the exception message
        assertEquals("No problem is currently assigned to the student.", exception.getMessage());
    }
}