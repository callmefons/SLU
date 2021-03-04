package com.example.sluapplication;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.metadata.MetadataExtractor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.example.sluapplication.Utility.log;

public class DialogueClassifier {

    private Fragment fragment;
    private final Map<String, Integer> dic = new HashMap<>();

    private static final int SENTENCE_LEN = 135; // The maximum length of an input sentence.
    private static final int NUM_CLASSES = 7; // The number of classes.
    private static final String UNKNOWN = "<OOV>";
    private static final String MODEL_PATH = "intent_classification.tflite";
    private Interpreter tflite;
    private String[] INTENTS = {"Add To Playlist", "Play Music", "Book Restaurant", "Get Weather", "Rate Book", "Search Creative Work", "Search Screening Event"};

    public DialogueClassifier(Fragment fragment) {
        this.fragment = fragment;
    }

    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = fragment.getContext().getAssets().open("vocab.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    /** Load the TF Lite model and dictionary so that the client can start classifying text. */
    public void run() {
        loadModel();
    }

    /** Load TF Lite model. */
    private synchronized void loadModel() {
        try {
            // Load the TF Lite model
            ByteBuffer buffer = loadModelFile(fragment.getContext().getAssets(), MODEL_PATH);
            tflite = new Interpreter(buffer);
            log("DialogueClassifier TFLite model loaded.");

            // Use metadata extractor to extract the dictionary and label files.
            MetadataExtractor metadataExtractor = new MetadataExtractor(buffer);

            // Extract and load the dictionary file.
            loadDictionaryFile();
            log("DialogueClassifier Dictionary loaded.");

        } catch (IOException ex) {
            log("DialogueClassifier Error loading TF Lite model.\n" + ex);
        }
    }

    /** Load TF Lite model from assets. */
    private static MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath)
            throws IOException {
        try (AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
             FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor())) {
                FileChannel fileChannel = inputStream.getChannel();
                long startOffset = fileDescriptor.getStartOffset();
                long declaredLength = fileDescriptor.getDeclaredLength();
                return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }

    /* Loading the vocabulary from the JSON file */
    private void loadDictionaryFile(){
        try {
            JSONObject jsonObject = new JSONObject(loadJSONFromAsset());
            JSONArray keys = jsonObject.names ();
            for (int i = 0; i < keys.length(); i++) {
                String key = keys.getString(i);
                Integer value = jsonObject.getInt(key);
                dic.put(key , value);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private float[][] tokenizeInputText(String text) {

        // pre-process text
        String filters = "[^\\w\\s-]";
        String cleanText = text.toLowerCase().replaceAll(filters, "").trim();
        String[] words = cleanText.split(" ");
        log("words " + Arrays.deepToString(words));

        // get the indices
        float[] tmp = new float[SENTENCE_LEN];
        int index = 0;
        for (String word: words) {
            if (!word.trim().equals("")){
                if (index >= SENTENCE_LEN) {
                    break;
                }
                tmp[index++] = dic.containsKey(word) ? dic.get(word) : (int) dic.get(UNKNOWN);
            }
        }

        //pad sequences
        Arrays.fill(tmp, index, SENTENCE_LEN - 1, 0);
        float[][] ans = {tmp};

        return ans;
    }

    public String classify(String text){

        float[][] inputs = tokenizeInputText(text);
        float[][] outputs = new float[1][NUM_CLASSES];
        tflite.run(inputs, outputs);

        String result = null;
        for (int i = 0; i < NUM_CLASSES; i++) {
            log("DialogueClassifier predicted " + i + " " + INTENTS[i] + " : " +  Math.round(outputs[0][i]));
            int predicted = Math.round(outputs[0][i]);
            if(predicted == 1) {
                result = INTENTS[i];
                break;
            }
        }

        return result;
    }
}
