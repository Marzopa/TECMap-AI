public class Response {
    private final String response;
    private final String feedback;
    private final int grade;

    public Response(String response, String feedback, int grade) {
        this.response = response;
        this.feedback = feedback;
        this.grade = grade;
    }

    public String getResponse() {
        return response;
    }

    public String getFeedback() {
        return feedback;
    }

    public int getGrade() {
        return grade;
    }
}
