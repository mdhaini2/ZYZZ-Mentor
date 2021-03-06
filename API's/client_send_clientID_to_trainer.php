<?php 
include("db_info.php");

$clientID = $_POST['clientID'];
$trainerUsername = $_POST['trainerUsername'];

// Get the email of the trainer
$trainerEmailQuery = $mysqli->prepare("SELECT email from trainer where username='$trainerUsername'");
$trainerEmailQuery->execute();

$fetchEmail = mysqli_fetch_array($trainerEmailQuery->get_result());
$trainerEmail = $fetchEmail["email"];

// Get the name of the trainer
$trainerNameQuery = $mysqli->prepare("SELECT full_name from trainer where username='$trainerUsername'");
    
$fetchTrainerName = mysqli_fetch_assoc($trainerNameQuery->get_result());
$trainerFullName = $fetchTrainerName["full_name"];

// Get the name of the client
// Not prone to SQL Injection
$clientNameQuery = $mysqli->query("SELECT full_name from client where client_id='$clientID'");
$fetchClientName = mysqli_fetch_assoc($clientNameQuery);
if(!$fetchClientName){
    exit("Please Add your info in 'MY INFO'");
}
$clientFullName = $fetchClientName["full_name"];

// To send HTML mail, the Content-type header must be set
$headers  = 'MIME-Version: 1.0' . "\r\n";
$headers .= "Content-type: text/html; charset=utf-8\r\n";
$headers .= "X-Priority: 3\r\n";
$headers .= "X-Mailer: PHP". phpversion() ."\r\n" ;
$from = "ZYZZ Mentor";
// Create email headers
$headers .= 'From: '.$from."\r\n".'Reply-To: '.$from."\r\n" . 'X-Mailer: PHP/' . phpversion();
 
// Compose a simple HTML email message
$message = '<html><body>';
$message .= '<h1 style="color:#000000;">Hello '.$trainerFullName.'</h1>';
$message .= '<p style="color:#000000;font-size:18px;">We have '.$clientFullName.' that want to be coached by you!</p>';
$message .= '<p style="color:#000000;font-size:18px;"> There client ID is: <u>'.$clientID.'</u> </p>';
$message .= '<p style="color:#000000;font-size:15px;"><b> Please do not share it with anyone </b></p>';
$message .= '</body></html>';
 
// Send the email to the trainer
if(mail($trainerEmail,'Client Registration',$message,$headers)){
echo "Request sent to ".$trainerFullName;
}
 
?>

