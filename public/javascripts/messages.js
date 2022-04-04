$(document).ready(function (e) {
    $("#messagesubmit").click(function (e) {
        e.preventDefault();
        if (checkMessage()) {
            sendMessage();
        }
    });

    $("#newMessage").click(function (e) {
        window.location.href = "/message";
    });

    $("#reloadMessages").click(function (e) {
        window.location.reload();
    });

    $(".message-delete").click(function (e) {
        e.preventDefault();
        var message_id = $(this).data("message_id");
        $.confirm({
            title: 'Löschen?',
            content: 'Soll diese Nachricht wirklich gelöscht werden?',
            buttons: {
                ja: function () {
                    deleteMessage(message_id);
                },
                nein: function () {
                }
            }
        });
    });

    $(".message__head").click(function (e) {
        $(this).next(".message__body").toggle();
        readMessage($(this).parent());
    });
});

function checkMessage() {
    var receiver = $('#receiver');
    var title = $('#title');
    var text = $('#text');
    if (!receiver.val() || !title.val() || !text.val()) {
        $('#messageError').text("Empfänger, Titel und Nachricht dürfen nicht leer sein.");
        return false;
    }
    if (!$.isNumeric(receiver.val())) {
        $('#messageError').text("Der Empfänger muss als ID übermittelt werden. Wenn du das liest, bist du ein hax0r!");
        return false;
    }
    return true;

}

function readMessage(message) {
    if (message.hasClass("message--unread")) {
        var message_id = message.attr('id');

        var token = $('input[name="csrfToken"]').attr('value');
        $.ajaxSetup({
            beforeSend: function (xhr) {
                xhr.setRequestHeader('Csrf-Token', token);
            }
        });

        var data = {message_id: message_id};
        $.ajax({
            url: '/messagesRead',
            method: 'POST',
            dataType: 'HTML',
            data: JSON.stringify(data),
            contentType: 'application/json; charset=utf-8',
            success: function (data) {
                message.removeClass("message--unread");
            },
            error: function (xhr, ajaxOptions, thrownError) {
                alert(xhr.status);
                alert(thrownError);
            }
        });
    }
}

function sendMessage() {
    var receiver = document.getElementById("receiver").value;
    var title = document.getElementById("title").value;
    var text = document.getElementById("text").value;

    var token = $('input[name="csrfToken"]').attr('value');
    $.ajaxSetup({
        beforeSend: function (xhr) {
            xhr.setRequestHeader('Csrf-Token', token);
        }
    });

    var data = {receiver_id: receiver, title: title, text: text};
    $.ajax({
        url: '/messagesSend',
        method: 'POST',
        dataType: 'HTML',
        data: JSON.stringify(data),
        contentType: 'application/json; charset=utf-8',
        success: function (data) {
            window.location.href = "/messages";
        },
        error: function (xhr, ajaxOptions, thrownError) {
            alert(xhr.status);
            alert(thrownError);
        }
    });
}

function deleteMessage(message_id) {

    var token = $('input[name="csrfToken"]').attr('value');
    $.ajaxSetup({
        beforeSend: function (xhr) {
            xhr.setRequestHeader('Csrf-Token', token);
        }
    });

    var data = {message_id: message_id};
    $.ajax({
        url: '/messagesDelete',
        method: 'POST',
        dataType: 'HTML',
        data: JSON.stringify(data),
        contentType: 'application/json; charset=utf-8',
        success: function (data) {
            window.location.href = "/messages";
        },
        error: function (xhr, ajaxOptions, thrownError) {
            alert(xhr.status);
            alert(thrownError);
        }
    });
}