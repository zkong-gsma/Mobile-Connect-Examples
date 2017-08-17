# India MNV Plus Client

Android Example that consumes the Serverside components.

the client will perform below.

- Check to make sure the phone has the correct connectivity.
- Check with the check_network API (from server side example) that the client's source is supported.
- Perform Mobile Connect request.

you will need to configure the "hostname" to where you host the server side examples.

    private final String check_network_url = "http://localhost/mnv_plus_php/check_network.php";
    private final String start_mc_url = "http://localhost/mnv_plus_php/start_mc.php";
    private final String more_info_url = "http://localhost/mnv_plus_php/more_info.php";
