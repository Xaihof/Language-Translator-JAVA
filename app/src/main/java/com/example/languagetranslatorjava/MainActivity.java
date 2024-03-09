package com.example.languagetranslatorjava;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.languagetranslatorjava.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    String[] fromLanguages = {
            "From",
            "ENGLISH",
            "AFRIKAANS",
            "ARABIC",
            "BELARUSIAN",
            "BENGALI",
            "CATALAN",
            "CZECH",
            "WELSH",
            "HINDI",
            "URDU"
    };

    String[] toLanguages = {"To",
            "ENGLISH",
            "AFRIKAANS",
            "ARABIC",
            "BELARUSIAN",
            "BENGALI",
            "CATALAN",
            "CZECH",
            "WELSH",
            "HINDI",
            "URDU"
    };

    private static final int REQUEST_PERMISSION_CODE = 1;
    String languageCode, fromLanguageCode, toLanguageCode = "0";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.idFromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromLanguageCode = getLanguageCode(fromLanguages[position]);


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // TODO: 3/9/2024 check for error
        ArrayAdapter fromAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, fromLanguages);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.idFromSpinner.setAdapter(fromAdapter);


        binding.idToSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLanguageCode = getLanguageCode(toLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter toAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, toLanguages);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.idToSpinner.setAdapter(toAdapter);


        binding.idBtnTranslate.setOnClickListener(v -> {
            binding.idTVTranslate.setText("");
            if (binding.sourceEdit.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please, Enter text to translate.", Toast.LENGTH_SHORT).show();
            } else if (fromLanguageCode == "0") {
                Toast.makeText(this, "Please, select source language.", Toast.LENGTH_SHORT).show();
            } else if (toLanguageCode == "0") {
                Toast.makeText(this, "Please, select translation language. ", Toast.LENGTH_SHORT).show();
            } else {
                String sourceText = binding.sourceEdit.getText().toString();
                translateText(fromLanguageCode, toLanguageCode, sourceText);
            }
        });

        binding.idIVMic.setOnClickListener(v -> {
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to convert into text.");
            try {
                startActivityForResult(i, REQUEST_PERMISSION_CODE);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == REQUEST_PERMISSION_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra((RecognizerIntent.EXTRA_RESULTS));
                binding.sourceEdit.setText(result.get(0));
            }
        }
    }

    private void translateText(String fromLanguageCode, String toLanguageCode, String sourceText) {

        binding.idTVTranslate.setText("Downloading Model...");
        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(String.valueOf(fromLanguageCode))
                        .setTargetLanguage(String.valueOf(toLanguageCode))
                        .build();

        final Translator translator =
                Translation.getClient(options);

        DownloadConditions conditions = new DownloadConditions.Builder()
                .build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                binding.idTVTranslate.setText("Translating...");
                translator.translate(sourceText).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {

                        binding.idTVTranslate.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to Translate" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Failed to download language model " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getLanguageCode(String language) {
        String languageCode = "0";

        switch (language) {
            case "ENGLISH":
                languageCode = TranslateLanguage.ENGLISH;
                break;
            case "AFRIKAANS":
                languageCode = TranslateLanguage.AFRIKAANS;
                break;
            case "ARABIC":
                languageCode = TranslateLanguage.ARABIC;
                break;
            case "BELARUSIAN":
                languageCode = TranslateLanguage.BELARUSIAN;
                break;
            case "BENGALI":
                languageCode = TranslateLanguage.BENGALI;
                break;
            case "CATALAN":
                languageCode = TranslateLanguage.CATALAN;
                break;
            case "CZECH":
                languageCode = TranslateLanguage.CZECH;
                break;
            case "WELSH":
                languageCode = TranslateLanguage.WELSH;
                break;
            case "HINDI":
                languageCode = TranslateLanguage.HINDI;
                break;
            case "URDU":
                languageCode = TranslateLanguage.URDU;
                break;
            default:
                languageCode = "0";
        }
        return languageCode;
    }
}