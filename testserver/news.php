<?php
    include('db.php');
    
    $timestamp = isset($_GET['timestamp']) ? intval($_GET['timestamp']) : time();
    $size = isset($_GET['size']) ? intval($_GET['size']) : 10;
    
    $result = array();
    for ($i = 0; $i < count($news); $i++) {
    	if ($timestamp > $news[$i]["timestamp"]) {
    		array_push($result, $news[$i]);
    		if (count($result) == $size) {
    			break;
    		}
    	}
    }
    
    header('Content-type: application/json');
    echo json_encode($result);
?>