<%@page import="org.example.mobileconnect.*"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>More Info</title>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
</head>
<body>
<table>
<tr>
<td>PCR</td>
<td id="pcr"></td>
</tr>
<tr>
<td>token_payload</td>
<td><textarea id="token" cols="115" style="width:100%"></textarea></td>
</tr>
<tr>
<td>userinfo_payload</td>
<td><textarea id="userinfo" cols="115" style="width:100%"></textarea></td>
</tr>
</table>
<script>
function autoheight(a) {
    if (!$(a).prop('scrollTop')) {
        do {
            var b = $(a).prop('scrollHeight');
            var h = $(a).height();
            $(a).height(h - 5);
        }
        while (b && (b != $(a).prop('scrollHeight')));
    };
    $(a).height($(a).prop('scrollHeight') + 20);
}

 $("#pcr").html("<% out.print(session.getAttribute("pcr")); %>");
    var token_payload = JSON.parse('<% out.print(session.getAttribute("token_payload")); %>');
    $("#token").val(JSON.stringify(token_payload, null, 2));
    autoheight($("#token"));
    var userinfo_payload = JSON.parse('<% out.print(session.getAttribute("userinfo_payload")); %>');
    $("#userinfo").val(JSON.stringify(userinfo_payload, null, 2));
    autoheight($("#userinfo"));
</script>
</body>
</html>