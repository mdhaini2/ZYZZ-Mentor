<?php 
include("db_info.php");

$clientID = $_GET["clientID"];
// Not prone to SQL Injection
$clientInfoQuery = $mysqli->query("SELECT * FROM client_info WHERE client_id = '$clientID'");

while($row =mysqli_fetch_assoc($clientInfoQuery))
{
    $emparray[] = $row;
}
echo json_encode($emparray);

?>