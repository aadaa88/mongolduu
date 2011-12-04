<?php
    $id = intval($_GET['id']);
    
    $file = $id. ".jpg";
    
    header("Cache-Control: public, must-revalidate");
    header("Pragma: hack");
    header("Content-Type: application/octet-stream");
    header("Content-Length: " .(string)(filesize($file)) );
    header('Content-Disposition: attachment; filename="'.$file.'"');
    header("Content-Transfer-Encoding: binary\n");
    
    readfile($file);
?>