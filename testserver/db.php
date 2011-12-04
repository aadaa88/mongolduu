<?php
    $dbfile = "mongolduu.sqlite";
    try {
    	$db = new PDO("sqlite:$dbfile");
    	$db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    } catch (PDOException $e) {
    	die($e->getMessage());
    }
    
    function get_songs() {
    	global $db;
    	
    	$songs = array();
    	foreach ($db->query("SELECT * from songs order by id asc") as $row) {
    		$song = array("id" => $row['id'], "artist" => $row['artist'], "album" => $row['album'], "title" => $row['title'], "genre" => $row['genre']);
    		array_push($songs, $song);
    	}
    	return $songs;
    }
    
    function get_news() {
    	global $db;
    	
		$current_timestamp = time(); 
    	$news = array();
    	foreach ($db->query("SELECT * from news order by timestamp desc") as $row) {
    		array_push($news, array("timestamp" => $row['timestamp'], "current_timestamp" => "$current_timestamp", "text" => $row['text']));
    	}
    	return $news;
    }
    
    function get_devices() {
    	global $db;
    
    	$devices = array();
    	foreach ($db->query("SELECT * from devices") as $row) {
    		array_push($devices, array("c2dmkey" => $row['c2dmkey']));
    	} 
    	return $devices;
    }
    
    function add_news($timestamp, $text) {
    	global $db;
    	
    	try {
    		$db->prepare("INSERT INTO news (timestamp, text) VALUES(?, ?)")->execute(array($timestamp, $text));
    	} catch (PDOException $e) {
    		die($e->getMessage() . "<br/>Make the testserver directory writeable! i.e. chmod a+w testserver");
    	}
    }
    
    function register_device($c2dmkey) {
    	global $db, $devices;
    	
    	$already_exists = false;
    	for ($i = 0; $i < count($devices); $i++) {
    		if ($devices[$i]["c2dmkey"] == $c2dmkey) {
    			$already_exists = true;
    			break;
    		}
    	}
    	
    	if (!$already_exists) {
	    	try {
	    		$db->prepare("INSERT INTO devices (c2dmkey) VALUES(?)")->execute(array($c2dmkey));
	    	} catch (PDOException $e) {
	    		die($e->getMessage() . "<br/>Make the testserver directory writeable! i.e. chmod a+w testserver");
	    	}
    	}
    }
    
    function deregister_device($c2dmkey) {
    	global $db;
    
    	try {
    		$db->prepare("DELETE FROM devices WHERE c2dmkey = ?")->execute(array($c2dmkey));
    	} catch (PDOException $e) {
    		die($e->getMessage() . "<br/>Make the testserver directory writeable! i.e. chmod a+w testserver");
    	}
    }
    
    $songs = get_songs();
    $news = get_news();
    $devices = get_devices();
?>