$(document).ready(function (e) {
    $('#startGame').click(function (event) {
        startGame();
    });
    $('#newGame').click(function (event) {
        window.location.href = "/game";
    });
    $('#toHighscore').click(function (event) {
        window.location.href = "/highscore";
    });
});

// ajaxCall
function startGame() {

    var token = $('input[name="csrfToken"]').attr('value');
    $.ajaxSetup({
        beforeSend: function (xhr) {
            xhr.setRequestHeader('Csrf-Token', token);
        }
    });

    var data = {data: "newGame"};
    $.ajax({
        url: '/game',
        method: 'POST',
        dataType: 'HTML',
        data: JSON.stringify(data),
        contentType: 'application/json; charset=utf-8',
        success: function (data) {
            window.location.href = "/game";
        },
        error: function (xhr, ajaxOptions, thrownError) {
            alert(xhr.status);
            alert(thrownError);
        }
    });
}