<?php
    include('db.php');

    $type = strtolower($_GET['type']);
    
    $result = array();
    
    if (strcmp($type, 'daily') == 0) {
        $result = array_slice($songs, 0, 5);
    } else if (strcmp($type, 'weekly') == 0) {
        $result = array_slice($songs, 5, 5);
    } else if (strcmp($type, 'alltime') == 0) {
        $result = array_slice($songs, 10, 5);
    } else if (strcmp($type, 'newest') == 0) {
        $result = array_slice($songs, 15, 5);
    } else {
        
    }
    
    header('Content-type: application/json');
    echo json_encode($result);
?>