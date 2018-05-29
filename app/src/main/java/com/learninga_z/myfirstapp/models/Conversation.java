package com.learninga_z.myfirstapp.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Conversation implements Parcelable {

    public String conversationId;
    public String name;
    public String color;
    public String latestMessage;
    public Timestamp createdOn;
    public Timestamp updatedOn;
    public Map<String, Boolean> users;

    public Conversation(String id, String name) {
        this();
        conversationId = id;
        createdOn = updatedOn = Timestamp.now();
        this.name = TextUtils.isEmpty(name) ? generateRandomName() : name;
        color = generateRandomColor();
    }

    public Conversation() {
        users = new HashMap<>();
    }

    public Map<String, Boolean> getUsers() {
        return users;
    }

    public void deleteForUser(String userId) {
        users.put(userId, false);
    }

    private String generateRandomName() {
        String[] adjs = {"autumn", "hidden", "bitter", "misty", "silent", "empty", "dry", "dark", "summer", "icy", "delicate", "quiet", "white", "cool", "spring", "winter", "patient", "twilight", "dawn", "crimson", "wispy", "weathered", "blue", "billowing", "broken", "cold", "damp", "falling", "frosty", "green", "long", "late", "lingering", "bold", "little", "morning", "muddy", "old", "red", "rough", "still", "small", "sparkling", "throbbing", "shy", "wandering", "withered", "wild", "black", "young", "holy", "solitary", "fragrant", "aged", "snowy", "proud", "floral", "restless", "divine", "polished", "ancient", "purple", "lively", "nameless"};
        String[] nouns = {"waterfall", "river", "breeze", "moon", "rain", "wind", "sea", "morning", "snow", "lake", "sunset", "pine", "shadow", "leaf", "dawn", "glitter", "forest", "hill", "cloud", "meadow", "sun", "glade", "bird", "brook", "butterfly", "bush", "dew", "dust", "field", "fire", "flower", "firefly", "feather", "grass", "haze", "mountain", "night", "pond", "darkness", "snowflake", "silence", "sound", "sky", "shape", "surf", "thunder", "violet", "water", "wildflower", "wave", "water", "resonance", "sun", "wood", "dream", "cherry", "tree", "fog", "frost", "voice", "paper", "frog", "smoke", "star"};
        String adj = adjs[(int) Math.floor(Math.random() * adjs.length)];
        adj = adj.substring(0,1).toUpperCase() + adj.substring(1);
        String noun = nouns[(int) Math.floor(Math.random() * nouns.length)];
        noun = noun.substring(0,1).toUpperCase() + noun.substring(1);
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

    @Override
    public boolean equals(Object obj) {
        return this.conversationId.equals( ((Conversation) obj).conversationId );
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "conversationId='" + conversationId + '\'' +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", latestMessage='" + latestMessage + '\'' +
                ", createdOn=" + createdOn +
                ", updatedOn=" + updatedOn +
                ", users=" + users +
                '}';
    }

    // Parcelable methods

    public Conversation(Parcel in) {
        this();
        conversationId = in.readString();
        name = in.readString();
        color = in.readString();
        latestMessage = in.readString();
        createdOn = new Timestamp(in.readLong(), in.readInt());
        updatedOn = new Timestamp(in.readLong(), in.readInt());

        // reconstruct the users hashmap
        int usersSize = in.readInt();
        for(int i = 0; i < usersSize; i++) {
            users.put(in.readString(), in.readInt() != 0);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(conversationId);
        out.writeString(name);
        out.writeString(color);
        out.writeString(latestMessage);
        out.writeLong(createdOn.getSeconds());
        out.writeInt(createdOn.getNanoseconds());
        out.writeLong(updatedOn.getSeconds());
        out.writeInt(updatedOn.getNanoseconds());

        // write the size of the users hashmap so we can loop to reconstruct it
        out.writeInt(users.size());
        for(String key : users.keySet()) {
            out.writeString(key);
            out.writeInt(users.get(key) ? 1 : 0);
        }
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Conversation createFromParcel(Parcel in) {
            return new Conversation(in);
        }

        public Conversation[] newArray(int size) {
            return new Conversation[size];
        }
    };
}
