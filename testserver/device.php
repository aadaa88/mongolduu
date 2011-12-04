<?php
    include('db.php');
    
    $action = $_GET['action'];
    $c2dmkey = $_GET['c2dmkey'];
    
    if ($action == "register") {
    	register_device($c2dmkey);
    } else if ($action == "deregister"){
    	deregister_device($c2dmkey);
    }
    
    $devices = get_devices();
    header('Content-type: application/json');
    echo json_encode($devices);
?>