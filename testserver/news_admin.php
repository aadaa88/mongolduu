<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>News Admin</title>
	</head>
	<body>
		<form action="<?php echo $_SERVER["PHP_SELF"]; ?>" method="POST">
			News text (<font color="red"><b>No XSS, SQL filter applied!</b></font>):
			<br/>
			(To add song information use the following tag: <b>&lt;a href=&quot;http://songinfo?id=SONG_ID&quot;&gt;Song information&lt;/a&gt;</b>)
			<br/>
			<textarea name="newstext" rows="5" cols="40"></textarea>
			<br/>
			<input type="submit" value="Create News"/>
		</form>
	
<?php
    include('db.php');
    
    # curl https://www.google.com/accounts/ClientLogin -d Email=YOUR_MAIL -d Passwd=YOUR_PASSWORD -d accountType=HOSTED_OR_GOOGLE -d source=Mongolduu -d service=ac2dm
    $GOOGLE_AUTH = "DQAAANkAAAB62boFb9KdPbmVJBW9NhOjwjYR1G7DL5SJUV0ecveM6NSN-KA1z0QcNx8SzzMeCfaTyTckSLaB6kvRaAuATjB4WpGkyWjoFq4tvG2f_jLmJFyQJj-D58V0A3IGOGdflt39Ksobsk0ydxwLhBN2Mv66G3KqctGiDAzKmMnWGmm_JM81hFs6qu214FL6nkD4ki_NahRzudC-5me1oCUDc1DSR2YCK6IN4E86It-JKK1Iz_WPwa-GEyJVGPhX9krifRyYXtl-kVw_9xeF7stiphlyVm2L2-lmlN7S15lVejqq0A";
    
    if (isset($_POST["newstext"])) {
    	$timestamp = time();
    	$text = $_POST["newstext"];
		if (get_magic_quotes_gpc()) {
			$text = stripslashes($text);
		}
    	add_news($timestamp, $text);
    	
    	$message = json_encode(array("timestamp" => $timestamp, "text" => $text));
    	
    	for ($i = 0; $i < count($devices); $i++) {
    		$c2dmkey = $devices[$i]["c2dmkey"];
    		$data = array(
    		            'registration_id' => $c2dmkey,
    		            'collapse_key' => "something",
    		            'data.message' => $message
    		);
    		$ch = curl_init();
    		curl_setopt($ch, CURLOPT_URL, "https://android.apis.google.com/c2dm/send");
    		curl_setopt($ch, CURLOPT_HTTPHEADER, array('Authorization: GoogleLogin auth=' . $GOOGLE_AUTH));
    		curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    		curl_setopt($ch, CURLOPT_POST, true);
    		curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    		curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
    		$response = curl_exec($ch);
    		curl_close($ch);
    		echo $c2dmkey . "<br/>";
    		echo $response . "<br/>";
    	}
    }
    
    $news = get_news();
    for ($i = 0; $i < count($news); $i++) {
    	$timestamp = $news[$i]["timestamp"];
    	echo "<br/>" . date("d.m.Y - H:i:s", $timestamp) . "<br/>";
    	echo $news[$i]["text"] . "<br/>";
    }
?>
	</body>
</html>