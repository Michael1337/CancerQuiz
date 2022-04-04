/**
 * For more information on Datatables: https://datatables.net/examples/basic_init/index.html
 */
$(document).ready(function () {
    $('#highscoreTable').DataTable({
        "language": {
            "search": "Suche:"
        },
        "paging": false,
        "info": false,
        "order": [[2, "desc"]],
        "columnDefs": [{
            "targets": 0,
            "orderable": false
        }]
    });
});