/**
 * initialization of the site.
 * creating a js-object out of a json-object.
 * initializing all global variables.
 */
var token = $('input[name="fightJson"]').attr('value');
fight = JSON.parse(token);
actualEnemy = null;
turns = 0;
skipped = false;
updatePlayerStats();

$(document).ready(function (e) {

    /**
     * initial button pressed, shows the main winodw.
     */
    $("#startFight").click(function (event) {
        $("#initialDialog").hide();
        $("#mainWindow").show();
        $("#selectWeapon").show();
    });

    /**
     * shows weapon stats depending on the selected object of the drop down menu.
     */
    $("#weaponSelect").on('change', function (event) {
        var idName = this.value;

        for (var i = 0; i < fight.weapons.length; i++) {
            if (fight.weapons[i].name == idName) {
                var type = fight.weapons[i].defenseTypeName;
                var damage = fight.weapons[i].damage;

                $("#infoText").hide();
                $("#hoverText").html("Typ: " + type + "<br>" + "Schaden: " + damage);
                $("#hoverText").show();
            }
        }
    });

    /**
     * equipping the selected weapon to the player.
     */
    $("#weaponConfirm").click(function (event) {
        var idName = $("#weaponSelect").val();

        for (var i = 0; i < fight.weapons.length; i++) {
            if (fight.weapons[i].name == idName) {
                fight.player.weapon = fight.weapons[i];

                updatePlayerStats();
                updateEnemyStats();

                $("#selectWeapon").hide();
                $("#initialText").hide();
                /* attack shouldn't be disabled for the first time, otherwise it should */
                if (turns != 0) {
                    $("#attack").prop("disabled", true);
                }

                $("#selectAction").show();
                $("#hoverText").hide();
                $("#infoText").show();
            }
        }
    });

    /**
     * an action button got pressed.
     */
    $(".actionButton").click(function (event) {
        var id = $(this).attr("id");
        if (id == "attack") attack();
        if (id == "block") block();
        if (id == "changeWeapon") changeWeapon();
    });

    /**
     * shows a confirm dialog asking if the player really want to skip the game.
     */
    $('#skipGame').click(function (event) {
        $.confirm({
            title: 'Den Kampf überspringen?',
            content: 'Du wirst keinen Multiplikator für Deine bisher erspielten Punkte erhalten! Trotzdem überspringen?',
            buttons: {
                ja: function () {
                    skipped = true;
                    endGameCommunication();
                },
                nein: function () {
                }
            }
        });
    });

});

function updatePlayerStats() {
    $("#playerHealth").text(fight.player.health);
    try {
        $("#playerWeapon").text(fight.player.weapon.name);
    } catch (err) {
        $("#playerWeapon").text("nicht ausgewählt");
    }
}

function updateEnemyStats() {
    iterateEnemies();
    $("#fightWindowEnemyText").show();
    $("#actualEnemy").text(actualEnemy.name);
    $("#enemyHealth").text(actualEnemy.health);
}

/**
 * iterates through all enemies and checks players health.
 */
function iterateEnemies() {
    if (actualEnemy == null) {
        index = 0;
        actualEnemy = fight.enemies[index];
        $("#infoText").text(actualEnemy.name + " erscheint!");
    } else {
        if (fight.player.health > 1) {
            if (actualEnemy.health < 1) {
                index++;
                if (index < fight.enemies.length) {
                    actualEnemy = fight.enemies[index];
                    $("#infoText").text(actualEnemy.name + " erscheint!");
                } else {
                    actualEnemy.health = 0;
                    endGame();
                }
            }
        } else {
            fight.player.health = 0;
            endGame();
        }
    }
}

function attack() {
    /* setting new health for the enemy */
    actualEnemy.health = actualEnemy.health - calculateAttack();
    /* giving feedback on the effectiveness of the attack */
    var attack = calculateAttack();
    if (calculateAttack() > 0) {
        if (calculateAttack() < 6) {
            $("#infoText").text("Die Attacke war kaum effektiv!");
        } else {
            $("#infoText").text("Die Attacke war effektiv!");
        }
    } else {
        $("#infoText").text("Die Attacke wurde geblockt!");
    }
    /* if the enemy still has life, his attack is executed */
    if (actualEnemy.health > 0) {
        fight.player.health -= enemyTurn();
    }

    turns += 1;
    updateEnemyStats();
    updatePlayerStats();
}

/**
 * checks the defense stats of the enemy and calculates the attack.
 * @returns {number} the final attack value.
 */
function calculateAttack() {
    var damage = fight.player.weapon.damage;
    var type = fight.player.weapon.defenseType;
    var attack = 0;
    for (var i = 0; i < actualEnemy.defenseValues.length; i++) {
        if (i + 1 == type) {
            attack = damage - actualEnemy.defenseValues[i];
        }
    }
    /* attack should not increase enemies health, that's why it returns 0. */
    if (attack < 0) {
        return 0;
    } else {
        return attack;
    }
}

/**
 * blocking the attack by dividing its value by 4.
 */
function block() {
    var attack = Math.round(enemyTurn() / 4);
    fight.player.health -= attack;
    $("#infoText").text("Du blockst!");
    turns += 1;

    updateEnemyStats();
    updatePlayerStats();
    /* re-enables the attack button. */
    $("#attack").prop("disabled", false);
}

function changeWeapon() {
    $("#selectAction").hide();
    $("#selectWeapon").show();
}

function enemyTurn() {
    var attack = actualEnemy.damage;
    return attack;
}

/**
 * checks players health and shows final dialog plus needed turns.
 */
function endGame() {
    $("#selectAction").hide();
    $("#skipGameDiv").hide();
    $("#endGame").show();
    $("#endGame2").show();

    if (fight.player.health > 0) {
        $("#infoText").text("Du hast gewonnen!");
    } else {
        $("#infoText").text("Du hast verloren!");
    }
    $("#turns").text("Benötigte Züge: " + turns);

    endGameCommunication();
}

function endGameCommunication() {
    var token = $('input[name="csrfToken"]').attr('value');
    $.ajaxSetup({
        beforeSend: function (xhr) {
            xhr.setRequestHeader('Csrf-Token', token);
        }
    });

    /* if the player is death or skips the game, turns should be null. */
    var finalTurns;
    if (skipped == true) {
        finalTurns = 1;
    } else {
        if (fight.player.health < 1) {
            finalTurns = 1;
        } else {
            finalTurns = turns;
        }
    }

    /* ajax call */
    var data = {turns: finalTurns};
    $.ajax({
        url: '/fightGame',
        method: 'POST',
        dataType: 'HTML',
        data: JSON.stringify(data),
        contentType: 'application/json; charset=utf-8',
        success: function (data) {
            /* if the player skipped the game, the page should redirect automatically */
            if (skipped == false) {
                var btn = $('#toNextGame');
                btn.click(function (event) {
                    window.location.href = "/game";
                });
            } else {
                window.location.href = "/game";
            }
        },
        error: function (xhr, ajaxOptions, thrownError) {
            alert(xhr.status);
            alert(thrownError);
        }
    });
}