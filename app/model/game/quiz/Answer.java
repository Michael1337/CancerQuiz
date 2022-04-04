package model.game.quiz;

public class Answer {
    private int answer_id;
    private int question_id;
    private String answer_text;
    private boolean correct;

    /**
     * Creates an Answer to a question.
     *
     * @param answer_id   The unique id of the answer.
     * @param question_id The question the answer belongs to.
     * @param answer_text The actual answer.
     * @param correct     If the answer is correct for its question.
     */
    public Answer(int answer_id, int question_id, String answer_text, boolean correct) {
        this.answer_id = answer_id;
        this.question_id = question_id;
        this.answer_text = answer_text;
        this.correct = correct;
    }

    public int getAnswer_id() {
        return answer_id;
    }

    public int getQuestion_id() {
        return question_id;
    }

    public String getAnswer_text() {
        return answer_text;
    }

    public boolean isCorrect() {
        return correct;
    }
}
