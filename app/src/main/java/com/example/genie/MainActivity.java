package com.example.genie;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import android.widget.EditText;
import android.util.Log;
import com.example.genie.conversation.ConversationalAgent;
import com.example.genie.utils.GPTApiClient;
import com.example.genie.utils.SessionContext;

public class MainActivity extends AppCompatActivity {

    private TextView transcriptionTextView;

    // 1) Define the ActivityResultLauncher at the class level
    private final ActivityResultLauncher<Intent> speechLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            ArrayList<String> results =
                                    result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                            if (results != null && !results.isEmpty()) {
                                String spokenText = results.get(0);
                                transcriptionTextView.setText(spokenText);
                            }
                        }
                    }
            );

    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 2) Set the layout defined in activity_main.xml
        setContentView(R.layout.activity_main);

        // 3) Initialize UI elements
        transcriptionTextView = findViewById(R.id.transcriptionTextView);
        Button speakButton = findViewById(R.id.speakButton);

        // 4) Set up the Speak button to initiate voice input
        speakButton.setOnClickListener(v -> startVoiceInput());
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        transcriptionTextView = findViewById(R.id.transcriptionTextView);
        Button speakButton = findViewById(R.id.speakButton);

        // Existing speech input
        speakButton.setOnClickListener(v -> startVoiceInput());

        // Dev Test Button
        EditText devInputEditText = findViewById(R.id.devInputEditText);
        Button devTestButton = findViewById(R.id.devTestButton);
        devTestButton.setOnClickListener(v -> {
            String simulatedText = devInputEditText.getText().toString();
            if (!simulatedText.isEmpty()) {
                transcriptionTextView.setText(simulatedText);

                GPTApiClient client = new GPTApiClient();
                client.callGPT4API(simulatedText, new GPTApiClient.GPTCallback() {
                    @Override
                    public void onSuccess(String response) {
                        Log.d("GPT Response", response);
                        runOnUiThread(() -> transcriptionTextView.setText(response));
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("GPT Error", error);
                        Toast.makeText(MainActivity.this, "GPT Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void debugSetTranscribedText(String mockText) {
        transcriptionTextView.setText(mockText);
        // Then pass mockText to the next step, e.g.,
        // conversationalAgent.processUserCommand(mockText, ...);
    }

    // 5) Initiates the speech recognition intent using the new Activity Result API
    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please speak your command");

        // 6) Launch the intent with the ActivityResultLauncher
        speechLauncher.launch(intent);
    }

    // 7) Remove or comment out onActivityResult if you're fully switching to the new API
    // @Override
    // protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    //     super.onActivityResult(requestCode, resultCode, data);
    // }
}
