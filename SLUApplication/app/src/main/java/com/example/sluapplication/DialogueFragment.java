package com.example.sluapplication;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Locale;

import static com.example.sluapplication.Utility.log;

public class DialogueFragment extends Fragment {

    private int REQUEST_MIC = 100;
    DialogueClassifier dialogueClassifier;
    ImageButton btn_mic;
    TextView txt_result;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dialogue, container, false);
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dialogueClassifier = new DialogueClassifier(this);
        dialogueClassifier.run();

        btn_mic = view.findViewById(R.id.btn_mic);
        txt_result = view.findViewById(R.id.txt_result);
        btn_mic.setOnClickListener(voiceListener);

    }

    /* speech recognition */
    private View.OnClickListener voiceListener = v -> {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Need to speak");
        try {
            startActivityForResult(intent, REQUEST_MIC);
            log("startActivityForResult");
        } catch (ActivityNotFoundException a) {
            log("Sorry your device not supported。");
            Toast.makeText(getActivity(), "Sorry your device not supported", Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_MIC) {
            if (resultCode == Activity.RESULT_OK && null != data) {
                ArrayList result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result == null) throw new AssertionError();
                classify(result.get(0).toString());
                log("onActivityResult " + result.get(0).toString());
            } else {
                String text = "Please help me find the Short Program saga.";
                classify(text);
            }
        }
    }

    private void classify(String text) {
        log("DialogueClassifier text " + text);
        String result =  dialogueClassifier.classify(text);
        log("DialogueClassifier result " + result);
        txt_result.setText(result);
    }
}

