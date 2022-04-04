$(document).ready(function (e) {
    progress = parseInt(document.getElementById("progress").value);

    $('.answer').click(function (event) {
        sendAnswerToServer($(this).attr('id'));
    });

    $('#progressBar').progressbar(
        {
            value: progress
        });
});

// ajaxCall
function sendAnswerToServer(answerID) {
    var question_id = document.getElementById("questionId").value;

    var token = $('input[name="csrfToken"]').attr('value');
    $.ajaxSetup({
        beforeSend: function (xhr) {
            xhr.setRequestHeader('Csrf-Token', token);
        }
    });

    var data = {question_id: question_id, answer_id: answerID};
    $.ajax({
        url: '/quiz',
        method: 'POST',
        dataType: 'HTML',
        data: JSON.stringify(data),
        contentType: 'application/json; charset=utf-8',
        success: function (data) {
            var obj = jQuery.parseJSON(data);
            updateFeedback(obj.status.toString(), obj.score.toString(), obj.answers, answerID);
        },
        error: function (xhr, ajaxOptions, thrownError) {
            alert(xhr.status);
            alert(thrownError);
        }
    });
}

function updateFeedback(status, score, answers, answerID) {
    $('#currentScore').text(score);
    $('#gameScoreCalc').text(parseInt($('#gameScore').val()) + parseInt(score));
    if (status === "true") {
        $('#answer-feedback__text').text("Deine Antwort ist richtig!");
        $('#' + answerID).addClass('answer--correct');
    } else if (status === "false") {
        var correctAnswers = "";
        for (var i = 0; i < answers.length; i++) {
            correctAnswers += answers[i].answer_text;
        }
        $('#answer-feedback__text').html(
            "Deine Antwort ist leider falsch! Richtig ist: " + "<strong>" + correctAnswers + "</strong>");
        $('#' + answerID).addClass('answer--wrong');
    }
    showBtnToNext();
}

function showBtnToNext() {
    var btn = $('#toNextQuestion');
    btn.show();
    btn.click(function (event) {
        window.location.href = "/quiz";
    });
}