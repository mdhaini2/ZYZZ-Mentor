package com.dhaini.zyzz;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class TrainerSelectedWorkout extends AppCompatActivity {
    private TextView workoutNameBannerTextView;
    private FloatingActionButton floatingActionButtonAddExercise;

    private String addSet_url = "http://10.0.2.2/ZYZZ/set_register_trainer.php";
    private String getExercise_url = "http://10.0.2.2/ZYZZ/get_exercise_and_sets.php";
    private String addExercise_url = "http://10.0.2.2/ZYZZ/exercise_register_trainer.php";
    private ArrayList<TrainerExercise> trainerExerciseList = new ArrayList<>();
    private Workout workoutSelected;
    private TrainerExercise newAddedExerciseTrainer;

    private RecyclerView WorkoutSelectedRecyclerView;
    private TrainerExerciseAdapter trainerExerciseAdapter;
    private RecyclerView.LayoutManager trainerExerciseLayoutManager;

    private AddSetAPI addSetAPI;

    private TrainerExercise selectedExercise;


    private AddExerciseAPI addExerciseAPI;
    private GetExercisesAPI getExercisesAPI;
    private SetTrainer newAddedSet;

    private DeleteExerciseAPI deleteExerciseAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); //hide the actionbar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_trainer_selected_workout);

        workoutSelected = getIntent().getParcelableExtra("Workout");


        workoutNameBannerTextView = (TextView) findViewById(R.id.bannerWorkoutName);
        workoutNameBannerTextView.setText(workoutSelected.getWorkoutName());

        getExercisesAPI = new GetExercisesAPI();
        getExercisesAPI.execute();

        floatingActionButtonAddExercise = (FloatingActionButton) findViewById(R.id.floatingAddExerciseButton);

        floatingActionButtonAddExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddExerciseDialog();
            }
        });


    }


    private void openAddExerciseDialog() {
        // Using the same dialog layout as addWorkout and edit on it

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        View mView = getLayoutInflater().inflate(R.layout.add_workout_dialog, null);

        final EditText exerciseNameEditText = (EditText) mView.findViewById(R.id.workoutNameEditText);
        final TextView titleTextView = (TextView) mView.findViewById(R.id.titleAddWorkoutDialog);
        Button addExerciseBtn = (Button) mView.findViewById(R.id.addWorkoutButton);

        titleTextView.setText("Create Exercise");
        exerciseNameEditText.setHint("Exercise Name");


        addExerciseBtn.setText("Add Exercise");

        mBuilder.setView(mView);
        AlertDialog dialog = mBuilder.create();
        dialog.show();
        addExerciseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String exerciseName = exerciseNameEditText.getText().toString();
                int position;
                if (trainerExerciseList.size() == 0) {
                    position = 0;
                } else {
                    position = trainerExerciseList.size();
                }
                ArrayList<SetTrainer> SetTrainerList = new ArrayList<>();
                newAddedExerciseTrainer = new TrainerExercise(exerciseName, workoutSelected.getWorkoutID(), position, SetTrainerList);
                addExerciseAPI = new AddExerciseAPI();
                addExerciseAPI.execute();
                dialog.dismiss();
            }
        });

    }


    public void openClientFeedback(View view) {
        Intent intent = new Intent(this, ClientSelectedWorkout.class);
        intent.putExtra("Workout", workoutSelected);
        intent.putExtra("User", "Trainer");
        startActivity(intent);
    }


    public void buildRecyclerView() {
        // Initializing the recyclerView to display the card of each client
        WorkoutSelectedRecyclerView = findViewById(R.id.WorkoutSelectedRecyclerView);
        trainerExerciseLayoutManager = new LinearLayoutManager(TrainerSelectedWorkout.this);
        trainerExerciseAdapter = new TrainerExerciseAdapter(trainerExerciseList, this);
        WorkoutSelectedRecyclerView.setLayoutManager(trainerExerciseLayoutManager);

        WorkoutSelectedRecyclerView.setAdapter(trainerExerciseAdapter);

        trainerExerciseAdapter.setOnItemClickListener(new TrainerExerciseAdapter.OnItemClickListener() {
            @Override
            public void onAddSetClick(int position) {
                openAddSetDialog(position);
            }

            @Override
            public void onDeleteExerciseClick(int position) {
                openConfirmationDialog(position);

            }
        });
    }

    private void openConfirmationDialog(int position) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        View mView = getLayoutInflater().inflate(R.layout.confirmation_dialogue, null);

        TextView confirmationMessageTextView = (TextView) mView.findViewById(R.id.messageConfirmation);
        confirmationMessageTextView.setText("Are you sure you want to delete " + trainerExerciseList.get(position).getExerciseName() + " ?");

        Button yesButton = (Button) mView.findViewById(R.id.YesButton);
        Button noButton = (Button) mView.findViewById(R.id.NoButton);

        mBuilder.setView(mView);
        AlertDialog dialog = mBuilder.create();
        dialog.show();

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String deleteExercise_url = "http://10.0.2.2/ZYZZ/delete_exercise.php?exerciseID=" + trainerExerciseList.get(position).getExerciseID();
                deleteExerciseAPI = new DeleteExerciseAPI();
                deleteExerciseAPI.execute(deleteExercise_url);

                trainerExerciseList.remove(position);
                trainerExerciseAdapter.notifyItemRemoved(position);

                dialog.dismiss();
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

    }

    private void openAddSetDialog(int position) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        View mView = getLayoutInflater().inflate(R.layout.add_set_dialog, null);

        EditText setNameEditText = (EditText) mView.findViewById(R.id.setNameInputEditText);
        EditText weightEditText = (EditText) mView.findViewById(R.id.weightInputEditText);
        EditText repsEditText = (EditText) mView.findViewById(R.id.repsInputEditText);

        Button addSetBtn = (Button) mView.findViewById(R.id.addSetButton);

        mBuilder.setView(mView);
        AlertDialog dialog = mBuilder.create();
        dialog.show();

        addSetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedExercise = trainerExerciseList.get(position);

                newAddedSet = new SetTrainer(setNameEditText.getText().toString(), repsEditText.getText().toString(),
                        weightEditText.getText().toString(), selectedExercise.getExerciseID());

                addSetAPI = new AddSetAPI();
                addSetAPI.execute();

                dialog.dismiss();
            }
        });

    }


    public Toast toastMessage(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        return toast;
    }

    //////////////////////////////////////// API Classes ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    class GetExercisesAPI extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            HttpClient http_client = new DefaultHttpClient();
            HttpPost http_post = new HttpPost(getExercise_url);

            BasicNameValuePair workoutIDParam = new BasicNameValuePair("workoutID", workoutSelected.getWorkoutID());

            ArrayList<NameValuePair> name_value_pair_list = new ArrayList<>();
            name_value_pair_list.add(workoutIDParam);

            try {
                // This is used to send the list with the api in an encoded form entity
                UrlEncodedFormEntity url_encoded_form_entity = new UrlEncodedFormEntity(name_value_pair_list);

                // This sets the entity (which holds the list of values) in the http_post object
                http_post.setEntity(url_encoded_form_entity);

                // This gets the response from the post api and returns a string of the response.
                HttpResponse http_response = http_client.execute(http_post);
                InputStream input_stream = http_response.getEntity().getContent();
                InputStreamReader input_stream_reader = new InputStreamReader(input_stream);
                BufferedReader buffered_reader = new BufferedReader(input_stream_reader);
                StringBuilder string_builder = new StringBuilder();
                String buffered_str_chunk = null;
                while ((buffered_str_chunk = buffered_reader.readLine()) != null) {
                    string_builder.append(buffered_str_chunk);
                }
                return string_builder.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONArray exerciseJsonArray = new JSONArray(s);

                if (exerciseJsonArray.length() != 0) {

                    trainerExerciseList = new ArrayList<>();

                    for (int i = 0; i < exerciseJsonArray.length(); i++) {

                        JSONObject ExerciseJsonObject = exerciseJsonArray.getJSONObject(i);

                        JSONArray setJsonArray = new JSONArray(ExerciseJsonObject.getString("0"));

                        ArrayList<SetTrainer> setTrainerList = new ArrayList<>();

                        // Get the sets belonging to the exercise at index i
                        for (int j = 0; j < setJsonArray.length(); j++) {
                            JSONObject setJsonObject = setJsonArray.getJSONObject(j);
                            String setName = setJsonObject.getString("set_name");

                            // If there is no set assigned
                            if (setName.equalsIgnoreCase("null")) {
                                break;
                            } else {
                                String exerciseID = setJsonObject.getString("exercise_id");
                                String reps = setJsonObject.getString("reps");
                                String weight = setJsonObject.getString("weight");
                                String setID = setJsonObject.getString("set_id");

                                SetTrainer setTrainer = new SetTrainer(setName, reps, weight, exerciseID, setID);
                                setTrainerList.add(setTrainer);
                            }
                        }

                        String exerciseName = ExerciseJsonObject.getString("exercise_name");
                        String exerciseID = ExerciseJsonObject.getString("exercise_id");
                        String comments = ExerciseJsonObject.getString("comments");
                        String workoutID = ExerciseJsonObject.getString("workout_id");

                        if (comments.equalsIgnoreCase("null")) comments = "";

                        int workoutPosition = Integer.valueOf(ExerciseJsonObject.getString("position"));

                        TrainerExercise trainerExercise = new TrainerExercise(exerciseID, exerciseName, comments, workoutID, workoutPosition, setTrainerList);
                        trainerExerciseList.add(trainerExercise);

                    }

                    buildRecyclerView();

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    class AddExerciseAPI extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            HttpClient http_client = new DefaultHttpClient();
            HttpPost http_post = new HttpPost(addExercise_url);

            String newExerciseName = newAddedExerciseTrainer.getExerciseName();
            String newExercisePosition = String.valueOf(newAddedExerciseTrainer.getPosition());

            BasicNameValuePair workoutIDParam = new BasicNameValuePair("workoutID", workoutSelected.getWorkoutID());
            BasicNameValuePair exerciseNameParam = new BasicNameValuePair("exerciseName", newExerciseName);
            BasicNameValuePair exercisePositionParam = new BasicNameValuePair("position", newExercisePosition);

            ArrayList<NameValuePair> name_value_pair_list = new ArrayList<>();
            name_value_pair_list.add(workoutIDParam);
            name_value_pair_list.add(exerciseNameParam);
            name_value_pair_list.add(exercisePositionParam);

            try {
                // This is used to send the list with the api in an encoded form entity
                UrlEncodedFormEntity url_encoded_form_entity = new UrlEncodedFormEntity(name_value_pair_list);

                // This sets the entity (which holds the list of values) in the http_post object
                http_post.setEntity(url_encoded_form_entity);

                // This gets the response from the post api and returns a string of the response.
                HttpResponse http_response = http_client.execute(http_post);
                InputStream input_stream = http_response.getEntity().getContent();
                InputStreamReader input_stream_reader = new InputStreamReader(input_stream);
                BufferedReader buffered_reader = new BufferedReader(input_stream_reader);
                StringBuilder string_builder = new StringBuilder();
                String buffered_str_chunk = null;
                while ((buffered_str_chunk = buffered_reader.readLine()) != null) {
                    string_builder.append(buffered_str_chunk);
                }
                return string_builder.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);

                String status = jsonObject.getString("status");
                String newAddedExerciseID = jsonObject.getString("ExerciseID");
                newAddedExerciseTrainer.setExerciseID(newAddedExerciseID);

                trainerExerciseList.add(newAddedExerciseTrainer);

                if (trainerExerciseAdapter == null) {
                    buildRecyclerView();
                } else {
                    trainerExerciseAdapter.notifyDataSetChanged();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    class AddSetAPI extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            HttpClient http_client = new DefaultHttpClient();
            HttpPost http_post = new HttpPost(addSet_url);

            BasicNameValuePair setNameParam = new BasicNameValuePair("setName", newAddedSet.getSetName());
            BasicNameValuePair repsParam = new BasicNameValuePair("reps", newAddedSet.getReps());
            BasicNameValuePair weightParam = new BasicNameValuePair("weight", newAddedSet.getWeight());
            BasicNameValuePair exerciseIDParam = new BasicNameValuePair("exerciseID", newAddedSet.getExerciseID());

            ArrayList<NameValuePair> name_value_pair_list = new ArrayList<>();

            name_value_pair_list.add(setNameParam);
            name_value_pair_list.add(repsParam);
            name_value_pair_list.add(weightParam);
            name_value_pair_list.add(exerciseIDParam);


            try {
                // This is used to send the list with the api in an encoded form entity
                UrlEncodedFormEntity url_encoded_form_entity = new UrlEncodedFormEntity(name_value_pair_list);

                // This sets the entity (which holds the list of values) in the http_post object
                http_post.setEntity(url_encoded_form_entity);

                // This gets the response from the post api and returns a string of the response.
                HttpResponse http_response = http_client.execute(http_post);
                InputStream input_stream = http_response.getEntity().getContent();
                InputStreamReader input_stream_reader = new InputStreamReader(input_stream);
                BufferedReader buffered_reader = new BufferedReader(input_stream_reader);
                StringBuilder string_builder = new StringBuilder();
                String buffered_str_chunk = null;
                while ((buffered_str_chunk = buffered_reader.readLine()) != null) {
                    string_builder.append(buffered_str_chunk);
                }
                return string_builder.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);

                String newAddedSetID = jsonObject.getString("setID");
                newAddedSet.setSet_id(newAddedSetID);
                selectedExercise.addSet(newAddedSet);

                buildRecyclerView();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class DeleteExerciseAPI extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {
            // URL and HTTP initialization to connect to API
            URL url;
            HttpURLConnection http;

            try {
                // Connect to API
                url = new URL(urls[0]);
                http = (HttpURLConnection) url.openConnection();

                // Retrieve API  content
                InputStream in = http.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                // Read API content line by line
                BufferedReader br = new BufferedReader(reader);
                StringBuilder sb = new StringBuilder();

                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }

                br.close();
                // Return content from API
                return sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(String values) {
            super.onPostExecute(values);
            try {
                values = values.replaceAll("[\\n]", "");
                toastMessage(values).show();

            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }


}