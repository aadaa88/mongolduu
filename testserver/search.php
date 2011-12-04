<?php
    include('db.php');
    
    $result = array();
    
    if (isset($_GET['id'])) {
    	$id = intval($_GET['id']);
    	
    	for ($i = 0; $i < count($songs); $i++) {
    		if ($songs[$i]["id"] == $id) {
    			array_push($result, $songs[$i]);
    			break;
    		}
    	}
    } else {
	    $searchstring = strtolower($_GET['searchstring']);
	    $offset = isset($_GET['offset']) ? intval($_GET['offset']) : 0;
	    $size = isset($_GET['size']) ? intval($_GET['size']) : 10;
	    
	    for ($i = 0; $i < count($songs); $i++) {
	        if (strlen($searchstring) == 0) {
	            array_push($result, $songs[$i]);
	        } else if (strpos(strtolower($songs[$i]["title"]), $searchstring) !== false || strpos(strtolower($songs[$i]["artist"]), $searchstring) !== false) {
	            array_push($result, $songs[$i]);
	        }
	    }
	    
	    $result = array_slice($result, $offset, $size);
    }
    
    header('Content-type: application/json');
    echo json_encode($result);
?>