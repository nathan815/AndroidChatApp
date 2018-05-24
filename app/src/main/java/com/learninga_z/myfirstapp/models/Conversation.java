package com.learninga_z.myfirstapp.models;

import android.text.TextUtils;

import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Conversation {

    public String conversationId;
    public String name;
    public String color;
    public String latestMessage;
    public Timestamp createdOn;
    public Timestamp updatedOn;
    public Map<String, Boolean> users;

    public Conversation(String id, String name) {
        conversationId = id;
        createdOn = updatedOn = new Timestamp(new Date());
        this.name = TextUtils.isEmpty(name) ? generateRandomName() : name;
        color = generateRandomColor();
        users = new HashMap<>();
    }

    public Conversation() {}

    public Map<String, Boolean> getUsers() {
        return users;
    }

    private String generateRandomName() {
        String[] adjs = {"autumn", "hidden", "bitter", "misty", "silent", "empty", "dry", "dark", "summer", "icy", "delicate", "quiet", "white", "cool", "spring", "winter", "patient", "twilight", "dawn", "crimson", "wispy", "weathered", "blue", "billowing", "broken", "cold", "damp", "falling", "frosty", "green", "long", "late", "lingering", "bold", "little", "morning", "muddy", "old", "red", "rough", "still", "small", "sparkling", "throbbing", "shy", "wandering", "withered", "wild", "black", "young", "holy", "solitary", "fragrant", "aged", "snowy", "proud", "floral", "restless", "divine", "polished", "ancient", "purple", "lively", "nameless"};
        String[] nouns = {"waterfall", "river", "breeze", "moon", "rain", "wind", "sea", "morning", "snow", "lake", "sunset", "pine", "shadow", "leaf", "dawn", "glitter", "forest", "hill", "cloud", "meadow", "sun", "glade", "bird", "brook", "butterfly", "bush", "dew", "dust", "field", "fire", "flower", "firefly", "feather", "grass", "haze", "mountain", "night", "pond", "darkness", "snowflake", "silence", "sound", "sky", "shape", "surf", "thunder", "violet", "water", "wildflower", "wave", "water", "resonance", "sun", "wood", "dream", "cherry", "tree", "fog", "frost", "voice", "paper", "frog", "smoke", "star"};
        String adj = adjs[(int) Math.floor(Math.random() * adjs.length)];
        adj = adj.substring(0,1).toUpperCase() + adj.substring(1);
        String noun = nouns[(int) Math.floor(Math.random() * nouns.length)];
        noun = noun.substring(0,1).toUpperCase() + adj.substring(1);
        return adj + " " + noun;
    }

    private String generateRandomColor() {
        Random r = new Random();
        StringBuffer sb = new StringBuffer("#");
        while(sb.length() < 7){
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 7);
    }
}
