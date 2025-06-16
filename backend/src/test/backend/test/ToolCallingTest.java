package test;

import Ollama.OllamaClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = API.MicroserviceApp.class)
public class ToolCallingTest {
    @Autowired
    private OllamaClient ollamaClient;

    @Test
    public void testRawToolCalling() throws Exception {

    }
}
