package com.learninga_z.myfirstapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaledrone.lib.Listener;
import com.scaledrone.lib.Member;
import com.scaledrone.lib.Room;
import com.scaledrone.lib.RoomListener;
import com.scaledrone.lib.Scaledrone;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements RoomListener {

    private static final String TAG = "MainActivity";

    private Scaledrone scaledrone;
    private String channelID = "r1hXZk6rrSVHGMa0";
    private String roomName = "observable-chat";

    private MessageAdapter messageAdapter;
    private ListView messagesView;
    private EditText sendMessageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendMessageText = findViewById(R.id.send_message_text);

        messageAdapter = new MessageAdapter(this);
        messagesView = findViewById(R.id.messages_view);
        messagesView.setAdapter(messageAdapter);

        MemberData memberData = new MemberData(getRandomName(), getRandomColor());

        scaledrone = new Scaledrone(channelID, memberData);
        scaledrone.connect(new Listener() {
            @Override
            public void onOpen() {
                Log.d(TAG, "Scaledrone connection open");
                scaledrone.subscribe(roomName, MainActivity.this);
            }

            @Override
            public void onOpenFailure(Exception ex) {
                System.err.println(ex);
            }

            @Override
            public void onFailure(Exception ex) {
                System.err.println(ex);
            }

            @Override
            public void onClosed(String reason) {
                System.err.println(reason);
            }
        });

    }

    public void sendMessage(View view) {
        String message = sendMessageText.getText().toString();
        if(message.length() > 0) {
            scaledrone.publish(roomName, message);
            sendMessageText.getText().clear();
        }

        Log.d(TAG, "Message sent: " + message);
    }

    @Override
    public void onOpen(Room room) {
        Log.d(TAG, "Connected to room");
    }

    @Override
    public void onOpenFailure(Room room, Exception ex) {
        Log.d(TAG, ex.toString());
    }

    @Override
    public void onMessage(Room room, final JsonNode json, final Member member) {
        Log.d(TAG, json.toString());
        try {
            ObjectMapper mapper = new ObjectMapper();
            MemberData memberData = mapper.treeToValue(member.getClientData(), MemberData.class);
            boolean belongsToCurrentUser = member.getId().equals(scaledrone.getClientID());
            final Message message = new Message(json.asText(), memberData, belongsToCurrentUser);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messageAdapter.add(message);
                    messagesView.setSelection(messagesView.getCount() - 1);
                }
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private String getRandomName() {
        String[] adjs = {"autumn", "hidden", "bitter", "misty", "silent", "empty", "dry", "dark", "summer", "icy", "delicate", "quiet", "white", "cool", "spring", "winter", "patient", "twilight", "dawn", "crimson", "wispy", "weathered", "blue", "billowing", "broken", "cold", "damp", "falling", "frosty", "green", "long", "late", "lingering", "bold", "little", "morning", "muddy", "old", "red", "rough", "still", "small", "sparkling", "throbbing", "shy", "wandering", "withered", "wild", "black", "young", "holy", "solitary", "fragrant", "aged", "snowy", "proud", "floral", "restless", "divine", "polished", "ancient", "purple", "lively", "nameless"};
        String[] nouns = {"waterfall", "river", "breeze", "moon", "rain", "wind", "sea", "morning", "snow", "lake", "sunset", "pine", "shadow", "leaf", "dawn", "glitter", "forest", "hill", "cloud", "meadow", "sun", "glade", "bird", "brook", "butterfly", "bush", "dew", "dust", "field", "fire", "flower", "firefly", "feather", "grass", "haze", "mountain", "night", "pond", "darkness", "snowflake", "silence", "sound", "sky", "shape", "surf", "thunder", "violet", "water", "wildflower", "wave", "water", "resonance", "sun", "wood", "dream", "cherry", "tree", "fog", "frost", "voice", "paper", "frog", "smoke", "star"};
        return (
            adjs[(int) Math.floor(Math.random() * adjs.length)] + "_" +
            nouns[(int) Math.floor(Math.random() * nouns.length)]
        );
    }

    private String getRandomColor() {
        Random r = new Random();
        StringBuffer sb = new StringBuffer("#");
        while(sb.length() < 7){
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 7);
    }
}
