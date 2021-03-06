<?php
include("db_info.php");

$workoutName = $_POST['workoutName'];
$planID = $_POST['planID'];
$position = $_POST['position'];

$workoutID = gen_id(6,$mysqli);

// Check if the plan_id exist or not

// Not prone to SQL Injection
$planIDquery = $mysqli->query("SELECT plan_id from login_trainer_client where plan_id='$planID'");
if($planIDquery->num_rows==0){
    exit("PLease create a plan with a client");
}

$workoutName = ucwords($workoutName);

$registerWorkout = $mysqli->prepare("INSERT INTO workout (workout_id,workout_name,plan_id,position) VALUES('$workoutID','$workoutName','$planID','$position')"); 
$registerWorkout->execute();

if($registerWorkout){
    echo "Workout Registered";
}

function gen_id($l,$mysqli){
    $generatedID = substr(str_shuffle("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"), 0, $l);
    $clientIdQuery = $mysqli->query("SELECT workout_id from workout where workout_id='$generatedID'");
    // To make sure the id generated is not already used.
    while($clientIdQuery->num_rows>0){
        $generatedID = substr(str_shuffle("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"), 0, $l);
        $clientIdQuery = $mysqli->query("SELECT workout_id from workout where workout_id='$generatedID'");
    }
    return $generatedID;
}