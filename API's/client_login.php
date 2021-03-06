<?php 
include('db_info.php');

$username = $_POST['Username'];
$password = $_POST['Password'];

$name="";
$clientID="";
$planID = "";

$usernameQuery = $mysqli->prepare("SELECT username from client where username=?");
$usernameQuery->bind_param("s",$username);
$usernameQuery->execute();


// Check if the username exist
if($usernameQuery->get_result()->num_rows==0){
    $result = "user does not exist!"; 
}

else{
// Get the hashed password of the account from the database
$passwordQuery = $mysqli->prepare("SELECT password from client where username=?");
$passwordQuery->bind_param("s",$username);
$passwordQuery->execute();

$fetchHashedPassword = mysqli_fetch_array($passwordQuery->get_result());

$hashedPassword = $fetchHashedPassword["password"];

// Verify if the password is correct
$verifyPassword = password_verify($password,$hashedPassword);

if($verifyPassword){
    // Get the name of the user to welcome him
    $fullNameQuery = $mysqli->query("SELECT full_name, client_id from client where username='$username'");

    $fetchFullName = mysqli_fetch_assoc($fullNameQuery);
    $fullName = $fetchFullName['full_name'];
    $clientID = $fetchFullName['client_id'];

    $splitFullName = explode(" ", $fullName);
    $name = $splitFullName[0];
    
    $planIDQuery = $mysqli->query("SELECT plan_id from login_trainer_client where client_id='$clientID'");
    
    if($planIDQuery->num_rows>0){
        $fetchPlanID = mysqli_fetch_assoc($planIDQuery);
        $planID = $fetchPlanID["plan_id"];
    }
    
    $result= "accepted";
}
else{
    $result  = "Wrong password!";
}

}

$response= array("status" =>$result,"Name"=> $name,"clientID"=>$clientID,"planID"=>$planID);
echo json_encode($response);    