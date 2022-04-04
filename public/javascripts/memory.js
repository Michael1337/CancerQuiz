var cardNames = [20];
var solvedPairs = [];
var flippedCard = [];
var moves = 0;

$(document).ready(function (e) {

    /**
     * Executes when the user clicks the start button.
     */
    $('#startMemory').click(function (event) {
        startMemory();
        $('#startMemory').remove();
    });

    /**
     * Confirms the users choice to skip the memory.
     */
    $('#endMemory').click(function (event) {
        $.confirm({
            title: 'Das Memory überspringen?',
            content: 'Du wirst keinen Multiplikator für Deine bisher erspielten Punkte erhalten! Trotzdem überspringen?',
            buttons: {
                ja: function () {
                    /*causes the multiplier to get set to 1.0 in the Controller after executing the endMemory() call*/
                    moves = 1;
                    endMemory();
                },
                nein: function () {
                }
            }
        });
    });

    /**
     * Main logic: Executes every time a single card is clicked.
     *
     * Allows closed cards to get flipped. If two successively flipped cards are a pair, they stay opened.
     * Otherwise they get flipped back.
     * Counts the already made moves.
     *
     * Ends the memory when every pair is solved and the user confirms wanting to leave the game.
     */
    $('.memory_board_single').click(function (event) {
        var cardID = $(this).attr("id");

        if (!isCardFlipped(cardID) && (flippedCard.length === 0)) {
            flipCard(cardID);
            flippedCard.push(cardID);
        } else if (!isCardFlipped(cardID) && (flippedCard.length === 1)) {
            flipCard(cardID);
            isPairFound(flippedCard[0], cardID);
            moves++;
            $('#neededMoves').text(moves);
            flippedCard.length = 0;
        }

        if (solvedPairs.length === 10) {
            $.confirm({
                title: 'Gut gemacht!',
                content: 'Du hast das Memory mit nur ' + moves + ' Zügen erfolgreich gelöst!',
                buttons: {
                    weiter: function () {
                        endMemory();
                    }
                }
            });
        }
    });
});

/**
 * Starts the memory and saves the names of cards before the card deck gets shuffled.
 */
function startMemory() {
    for (var i = 1; i <= 20; i++) {
        var card = $("#" + i);
        card.children().removeAttr("title");
        card.children().toggleClass('displayedCard hiddenCard');
        cardNames[i - 1] = card.children().attr("title");
    }
    shuffle($("#memory_board"));
}

/**
 * Checks if the clicked card shows the cover or the front image (is flipped).
 * @param cardID Id of the clicked card.
 * @returns {boolean} Returns true if clicked card shows the front image.
 */
function isCardFlipped(cardID) {
    var clickedCard = $("#" + cardID);
    return (clickedCard.attr('class') === "memory_board_single flipped") || (clickedCard.find('#frontImg').attr('class') === "displayedCard");
}

/**
 * Changes the shown image of the clicked card (cover or front image).
 * Adds tooltip if flip opens the card, removes it if flip closes the card.
 * @param cardID Id of the clicked card.
 */
function flipCard(cardID) {
    var clickedCard = $("#" + cardID);
    clickedCard.toggleClass('flipped');
    clickedCard.toggleClass('card_cursor');

    var titleAttr = $(cardID).children().attr('title');
    if (typeof titleAttr !== typeof undefined && titleAttr !== false) {
        clickedCard.children().removeAttr("title");
    } else {
        clickedCard.children().attr("title", cardNames[cardID - 1]);
    }
}

/**
 * Checks if two opened cards are a correct pair and if so adds the image id of the pair to solvedPairs.
 * Flips both cards back to cover if they are not a correct pair.
 * @param card1ID Id of the first card.
 * @param card2ID Id of the second card.
 * @returns {boolean} Returns true if a correct pair was found.
 */
function isPairFound(card1ID, card2ID) {
    var imgID1 = $("#" + card1ID).data("imgid");
    var imgID2 = $("#" + card2ID).data("imgid");
    if (imgID1 === imgID2) {
        solvedPairs.push(imgID1);
        $('#foundPairs').text(solvedPairs.length);
        return true;
    } else {
        setTimeout(function () {
            flipCard(card1ID);
            flipCard(card2ID);
        }, 1000);
    }
    return false;
}

/**
 * Shuffles the passed card deck
 * @param cardDeck The card deck, in this case a div that contains many single divs / cards
 */
function shuffle(cardDeck) {
    var cardDeckArray = cardDeck.children();
    while (cardDeckArray.length) {
        var shuffledCard = cardDeckArray.splice(Math.floor(Math.random() * cardDeckArray.length), 1)[0];
        cardDeck.append(shuffledCard);
    }
    $('.memory_board_single').addClass('card_cursor');
}

/**
 * Ends the current memory game and sends the needed moves per ajax call to the server.
 */
function endMemory() {
    var token = $('input[name="csrfToken"]').attr('value');
    $.ajaxSetup({
        beforeSend: function (xhr) {
            xhr.setRequestHeader('Csrf-Token', token);
        }
    });

    var data = {moves: moves};
    $.ajax({
        url: '/memory',
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


