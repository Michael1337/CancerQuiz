$(document).ready(function (e) {
    progress = parseInt(document.getElementById("progress").value);

    $('#progressBar').progressbar(
        {
            value: progress
        });
});