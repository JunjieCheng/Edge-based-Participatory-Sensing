<?php

error_reporting(E_ALL);
ini_set('display_errors', 1);

$entityBody = file_get_contents('php://input');

$task = explode("=", $entityBody)[1];

echo $task;

?>
