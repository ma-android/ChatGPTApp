package com.example.chatgptapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {


    RecyclerView recyclerView;
    TextView welcomeTextView;
    EditText messageEditText;
    ImageButton sendButton;
    List<MessageModal> messageModalList;
    MessageAdapter messageAdapter;



    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageModalList= new ArrayList<>();

        recyclerView= findViewById(R.id.recycler_view);
        welcomeTextView= findViewById(R.id.welcome_text);
        messageEditText= findViewById(R.id.message_edit_text);
        sendButton= findViewById(R.id.send_btn);



        //setup RecyclerView

        messageAdapter = new MessageAdapter(messageModalList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
         recyclerView.setLayoutManager(llm);


         sendButton.setOnClickListener((v )-> {

            String question = messageEditText.getText().toString().trim();
            addToChat(question, MessageModal.SENT_BY_ME);
            messageEditText.setText("");
            welcomeTextView.setVisibility(View.GONE);
            callAPI(question);
             });
    }


           void addToChat(String message, String sentBy){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageModalList.add(new MessageModal(message,sentBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });}

    void addResponse(String response){
        messageModalList.remove(messageModalList.size()-1);
        addToChat(response, MessageModal.SENT_BY_BOT); }



        void callAPI (String question){
            //okhttp

            messageModalList.add(new MessageModal("Typing...",MessageModal.SENT_BY_BOT));

                   JSONObject jsonBody = new JSONObject();

                   try {
                       jsonBody.put("model", "text-davinci-003");
                       jsonBody.put("prompt", question);
                       jsonBody.put("max_tokens", 4000);
                       jsonBody.put("temperature", 0);
                   } catch (JSONException e) { e.printStackTrace();}

                   RequestBody body = RequestBody.create(jsonBody.toString(),JSON);
                   Request request = new Request.Builder()
                           .url("https://api.openai.com/v1/completions")
                           .header("Authorization", "Bearer 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")
                           .post(body)
                           .build();

                   client.newCall(request).enqueue(new Callback() {
                       @Override
                       public void onFailure(@NonNull Call call, @NonNull IOException e) {

                           addResponse("Failed to load response due to"+ e.getMessage());

                       }

                       @Override
                       public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if(response.isSuccessful()){


                                JSONArray jsonArray = null;
                                try {
                                    JSONObject jsonObject= new JSONObject(response.body().string());
                                    jsonArray = jsonObject.getJSONArray("choices");
                                    String result = jsonArray.getJSONObject(0).getString("text");
                                    addResponse(result.trim());
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }

                            }
                            else { addResponse("Failed to load response due to"+ response.body().toString());}
                       }
                   });

               }

           }
