$(document).ready(function() {
    $("#inputEmail3").blur(function() {
        var email = $("#inputEmail3").val();
        $.ajax({
            url: "register/check",
            type: "post",
            data: "email="+email
        }).success(function(response) {
            if (response === "error") {
                $("#mailError").text("Email already exists");
            }
        })
    })
});


$(document).ready(function () {
    var valueOfSelect;
    var saveValueOfSelect = "";
    var alreadyOnRoute = "";
    var destinationOffice = $('#getDestinationOffice').val();
    var initialOffice = $('#getInitialOffice').val();
    var counter=0;

    // for any form on this page do the following
    $('#addToRoute').click(function () {
        valueOfSelect = $('.selectOffice :selected').text();
        alreadyOnRoute += $('.selectOffice :selected').text() + ",";
        var alreadyOnRouteSplitted = alreadyOnRoute.split(",");

        $.ajax({
            url: "/adminpanel/makeroute/create",
            method: "POST",
            data: "name=" + valueOfSelect
        }).success(function (response) {
            var str = response;

            console.log("Response = " + response);
            var splitted = str.split(",");
            $('.selectOffice').empty();
            saveValueOfSelect += valueOfSelect + ",";


            $('#finalRoute').attr("value", saveValueOfSelect);
            for (var j = 0; j < alreadyOnRouteSplitted.length - 1; j++) {

                for (var i = 0; i < splitted.length; i++) {
                    if (alreadyOnRouteSplitted[j] == splitted[i] || splitted[i]==initialOffice) {
                        var index = splitted.indexOf(splitted[i]);
                        if (index > -1) {
                            splitted.splice(index, 1);
                        }
                    }
                }
            }

            if (counter>0) {

                $('.selectOffice').empty();
                return;

            }

            for (var i = 0; i < splitted.length; i++) {
                $('.selectOffice').append("<option value=" + splitted[i] + ">" + splitted[i] + "</option>");
                if (splitted[i]==destinationOffice) {
                    counter++;
                }

            }

            var splitFirstOffice = saveValueOfSelect.split(',');
            if(splitFirstOffice[0]== destinationOffice){
                $('.selectOffice').empty();
                return;
            }

            $('#clearFromRoute').click(function(){

                window.location.reload();
            });


        }).error(function (response) {
        });
    });

    $("#search").keyup(function() {
        var search = $("#search").val();
        var exp = new RegExp(search, "i");
        $.ajax({
            beforeSend: function() {
                $("#searchPackages").html("Searching...")
            },
            url: '/adminpanel/package/json',
            type: 'POST',
            success: function(response) {
                console.log(response);
                var tbody = '<thead><tr><th>#</th><th>Tracking Number</th><th>Destination</th></tr></thead><tbody>';
                $.each(response, function(key, val) {
                    if (val.destination.search(exp) != -1) {
                        tbody += '<tr class="succes"><td>' + val.id + '</td>'
                        tbody += '<td>' + val.trackingNum + '</td>';
                        tbody += '<td>' + val.destination + '</td>';
                        tbody += '</tr>';
                    }
                });
                tbody += '</tbody>';
                $("#searchPackages").html(tbody);
            }
        });
    });
});

$(document).ready(function(){
    $('body').on('click', 'a[data-role="delete"]', function (e) {
        e.preventDefault();
        $toDelete = $(this);
        var conf = bootbox.confirm("Delete?", function (result) {
            if (result == true) {
                $.ajax({
                    url: $toDelete.attr("href"),
                    method: "delete"
                }).success(function (response) {
                    $toDelete.parents($toDelete.attr("data-delete-parent")).remove();
                });
            }
        });
    });
});


$(".dropdown-menu li a").click(function () {
    var selText = $(this).text();
    $(this).parents('.btn-group').find('.dropdown-toggle').html(selText + ' <span class="caret"></span>');
});

$("#btnSearch").click(function () {
    alert($('.btn-select').text() + ", " + $('.btn-select2').text());
});


function checkEmail() {
    var email = document.getElementById("inputEmail3").value;
    var filter = /^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i;

    if (!filter.test(email)) {
        document.getElementById("mailError").innerHTML = "Invalid E-mail.";
    } else {
        document.getElementById("mailError").innerHTML = "";
    }
}

function checkFirstName() {

    var firstName = document.getElementById("inputName").value;
    var filter = /^[a-zA-Z]+$/;

    if (firstName.length == 0) {
        document.getElementById("nameError").innerHTML = "This field is required";
    } else {

        if (!filter.test(firstName)) {
            document.getElementById("nameError").innerHTML = "Only letters allowed.";
        } else {
            document.getElementById("nameError").innerHTML = "";
        }
    }
}

function checkLastName() {

    var lastName = document.getElementById("inputLastName").value;
    var filter = /^[a-zA-Z]+$/;

    if (lastName.length == 0) {
        document.getElementById("lastNameError").innerHTML = "This field is required";
    } else {

        if (!filter.test(lastName)) {
            document.getElementById("lastNameError").innerHTML = "Only letters allowed.";
        } else {
            document.getElementById("lastNameError").innerHTML = "";
        }
    }
}

function checkPassword() {
    var password = document.getElementById("inputPassword3").value;

    if (password.length == 0) {
        document.getElementById("passError").innerHTML = "This field is required";
    } else {


        if (password.length < 6) {
            document.getElementById("passError").innerHTML = "Password must have at least 6 characters.";
        } else if (password.search(/\d/) == -1) {
            document.getElementById("passError").innerHTML = "Password must have at least one number.";
        } else if (password.search(/[a-zA-Z]/)) {
            document.getElementById("passError").innerHTML = "Password must have at least one letter.";
        } else if (password.length > 6 && !password.search(/[a-zA-Z]/) && password.search(/\d/) != -1) {
            document.getElementById("passError").innerHTML = "";
        }
    }
}

function checkPasswords() {
    var password = document.getElementById("inputPassword3").value;
    var repassword = document.getElementById("inputPassword4").value;

    if (password != repassword) {
        document.getElementById("repassError").innerHTML = "Password must be same.";
    } else {
        document.getElementById("repassError").innerHTML = "";
    }
}

