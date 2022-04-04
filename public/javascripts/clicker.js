$(document).ready(function (e) {
    points = 0;
    $('#start').click(function (event) {
        startGame();
    });

    $('#finish').click(function (event) {
        running = false;
        $.confirm({
            title: 'Den CancerClicker überspringen?',
            content: 'Du wirst keinen Multiplikator für Deine bisher erspielten Punkte erhalten! Trotzdem überspringen?',
            buttons: {
                ja: function () {
                    points = 0; //because you have to be punished...
                    $('#points').text(points);
                    finish();
                },
                nein: function () {
                    running = true;
                }
            }
        });
    });
});

/**
 * Starts the game. Creates a first bad cell and starts the timer. Timer ends after 20 seconds.
 */
function startGame() {
    $('#start_box').remove();
    points = 0;
    addCellBad();

    var seconds = 200;
    running = true;
    timer = setInterval(function () {
        if (running) {
            seconds--;
            $('#timer').text(seconds / 10);
            if (seconds < 50) {
                $('#timer').addClass("clicker-info__item--red");
            }

            if (seconds < 1) {
                finish();
            }
        }
    }, 100);
}

/**
 * Calculates randoms cells and adds them to the board.
 */
function addCell() {
    var random = Math.random();
    if (random < 0.25) {
        addCellGood();
        addCellBad();
        return;
    }
    if (random < 0.9) {
        addCellBad();
        return;
    }
    addCellWorse();
}

/**
 * Adds a bad (good to click) cell to the board.
 */
function addCellBad() {
    createImg("bad");

    $('#bad').click(function (event) {
        $('.clicker__cell').remove();
        badClicked();
    });
}

/**
 * Adds a worse (good to click) cell to the board.
 */
function addCellWorse() {
    createImg("worse");

    $('#worse').click(function (event) {
        $('.clicker__cell').remove();
        worseClicked();
    });
}

/**
 * Adds a good (bad to click) cell to the board.
 */
function addCellGood() {
    createImg("good");

    $('#good').click(function (event) {
        $('.clicker__cell').remove();
        goodClicked();
    });
}

/**
 * Creates the image that gets displayed and can be clicked.
 * @param kind What kind the cell is of: Good/Bad/Worse.
 */
function createImg(kind) {
    var div = $('<div id="' + kind + '" class="clicker__cell clicker__img--' + kind + '"></div>');
    var left = Math.floor((Math.random() * 670)) + "px";
    var top = Math.floor((Math.random() * 490)) + "px";
    var my_css_class = {position: 'absolute', top: top, left: left};
    div.css(my_css_class);
    div.appendTo('#clicker__container');
}

function badClicked() {
    changePoints(1);
    $('#points').text(points);
    addCell();
}

function worseClicked() {
    changePoints(2);
    $('#points').text(points);
    addCell();
}

function goodClicked() {
    changePoints(-1);
    $('#points').text(points);
    addCell();
}

function changePoints(change) {
    points += change;
    if (points < 0) points = 0;
}

/**
 * Finishes the game by stopping the timer and ending the game.
 */
function finish() {
    $('#start_box').remove();
    clearInterval(timer);
    endGame();
    $('#finish').remove();
}

/**
 * Ends game. Displays points to user and sends data to server to store in database.
 */
function endGame() {
    $('.clicker__cell').remove();

    var token = $('input[name="csrfToken"]').attr('value');
    $.ajaxSetup({
        beforeSend: function (xhr) {
            xhr.setRequestHeader('Csrf-Token', token);
        }
    });

    // ajax call
    var data = {points: points};
    $.ajax({
        url: '/clicker',
        method: 'POST',
        dataType: 'HTML',
        data: JSON.stringify(data),
        contentType: 'application/json; charset=utf-8',
        success: function (data) {
            showEndBox();
        },
        error: function (xhr, ajaxOptions, thrownError) {
            alert(xhr.status);
            alert(thrownError);
        }
    });
}

/**
 * Shows points and a button to reload the page.
 */
function showEndBox() {
    var endPoints = $('#endPoints');
    endPoints.text(points);
    var box = $('#end_box');
    box.show();

    var btn = $('#toNextGame');
    btn.click(function (event) {
        window.location.href = "/game";
    });
}