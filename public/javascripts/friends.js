$(document).ready(function (e) {
    $("#requestFriend").click(function () {
        var userid = $(this).data("userid");
        requestFriend(userid);
    });
    $("#acceptFriend").click(function () {
        var userid = $(this).data("userid");
        acceptFriend(userid);
    });
    $("#deleteFriend").click(function () {
        var userid = $(this).data("userid");
        $.confirm({
            title: 'Freund entfernen?',
            content: 'Möchtest Du diesen Freund wirklich aus Deiner Freundesliste entfernen?',
            buttons: {
                ja: function () {
                    deleteFriend(userid);
                },
                nein: function () {
                }
            }
        });
    });
});

function requestFriend(userid) {
    var token = $('input[name="csrfToken"]').attr('value');
    $.ajaxSetup({
        beforeSend: function (xhr) {
            xhr.setRequestHeader('Csrf-Token', token);
        }
    });

    var data = {userid: userid};
    $.ajax({
        url: '/friendsRequest',
        method: 'POST',
        dataType: 'HTML',
        data: JSON.stringify(data),
        contentType: 'application/json; charset=utf-8',
        success: function (data) {
            location.reload();
        },
        error: function (xhr, ajaxOptions, thrownError) {
            alert(xhr.status);
            alert(thrownError);
        }
    });

};

function acceptFriend(userid) {
    var token = $('input[name="csrfToken"]').attr('value');
    $.ajaxSetup({
        beforeSend: function (xhr) {
            xhr.setRequestHeader('Csrf-Token', token);
        }
    });

    var data = {userid: userid};
    $.ajax({
        url: '/friendsAccept',
        method: 'POST',
        dataType: 'HTML',
        data: JSON.stringify(data),
        contentType: 'application/json; charset=utf-8',
        success: function (data) {
            location.reload();
        },
        error: function (xhr, ajaxOptions, thrownError) {
            alert(xhr.status);
            alert(thrownError);
        }
    });

};

function deleteFriend(userid) {
    var token = $('input[name="csrfToken"]').attr('value');
    $.ajaxSetup({
        beforeSend: function (xhr) {
            xhr.setRequestHeader('Csrf-Token', token);
        }
    });

    var data = {userid: userid};
    $.ajax({
        url: '/friendsDelete',
        method: 'POST',
        dataType: 'HTML',
        data: JSON.stringify(data),
        contentType: 'application/json; charset=utf-8',
        success: function (data) {
            location.reload();
        },
        error: function (xhr, ajaxOptions, thrownError) {
            alert(xhr.status);
            alert(thrownError);
        }
    });

};