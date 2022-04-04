$(document).ready(function (e) {
    $("#loginsubmit").click(function (e) {
        e.preventDefault();
        if (checkLoginData()) {
            login();
        }
    });

    $("#registersubmit").click(function (e) {
        e.preventDefault();
        if (checkRegisterData()) {
            register();
        }
    });
});

function checkLoginData() {
    var username = $('#un');
    var password = $('#pw');
    if (!username.val() || !password.val()) {
        $('#error').text("Name und Passwort dürfen nicht leer sein.");
        return false;
    }
    return true;
}

function checkRegisterData() {
    var username = $('#un');
    var email = $('#em');
    var password = $('#pw');
    var password2 = $('#pw2');
    if (!username.val() || !email.val() || !password.val() || !password2.val()) {
        $('#error').text("Name, Email und Passwort dürfen nicht leer sein.");
        return false;
    }
    if (password.val() !== password2.val()) {
        $('#error').text("Das angegebene Passwort stimmt nicht mit der Wiederholung überein.");
        return false;
    }
    if (!validateEmail(email.val())) {
        $('#error').text("Die angegebene E-Mail-Adresse entspricht nicht dem üblichen Format einer E-Mail-Adresse (a@x.yz).");
        return false;
    }
    return true;
}

function validateEmail(email) {
    var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    //https://stackoverflow.com/a/46181/5592707
    //not validated on server
    return re.test(email);
}

function login() {
    var username = $('#un').val();
    var password = $('#pw').val();

    // setup for ajax call to save selected area in session
    var token = $('input[name="csrfToken"]').attr('value');
    $.ajaxSetup({
        beforeSend: function (xhr) {
            xhr.setRequestHeader('Csrf-Token', token);
        }
    });

    // ajax call
    var data = {username: username, password: password};
    $.ajax({
        url: '/login',
        method: 'POST',
        dataType: 'HTML',
        data: JSON.stringify(data),
        contentType: 'application/json; charset=utf-8',
        success: function (data) {
            giveFeedback(data);
        },
        error: function (xhr, ajaxOptions, thrownError) {
            alert(xhr.status);
            alert(thrownError);
        }
    });

}

function register() {
    var username = $('#un').val();
    var email = $('#em').val();
    var password = $('#pw').val();

    // setup for ajax call to save selected area in session
    var token = $('input[name="csrfToken"]').attr('value');
    $.ajaxSetup({
        beforeSend: function (xhr) {
            xhr.setRequestHeader('Csrf-Token', token);
        }
    });

    // ajax call
    var data = {username: username, password: password, email: email};
    $.ajax({
        url: '/register',
        method: 'POST',
        dataType: 'HTML',
        data: JSON.stringify(data),
        contentType: 'application/json; charset=utf-8',
        success: function (data) {
            giveFeedback(data);
        },
        error: function (xhr, ajaxOptions, thrownError) {
            alert(xhr.status);
            alert(thrownError);
        }
    });
}

function giveFeedback(data) {
    var obj = jQuery.parseJSON(data);
    if (obj.error.toString() === "") {
        window.location.href = "/";
    } else {
        $('#error').text(obj.error);
    }
}