// File: src/main/java/API/GlobalExceptionHandler.java
package API;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        // Log the exception details
        logger.error("Exception details:", ex);
        logger.error("Exception type: {}", ex.getClass().getName());
        logger.error("Exception message: {}", ex.getMessage());
        logger.error("Stack trace:");
        for (StackTraceElement element : ex.getStackTrace()) {
            logger.error("    at {}.{}({}:{})",
                    element.getClassName(),
                    element.getMethodName(),
                    element.getFileName(),
                    element.getLineNumber());
        }

        return new ResponseEntity<>("Error: " + ex.getLocalizedMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}