package com.lahiriproductions.lambrk_messenger.Utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class Controller {

    private HashMap<DatabaseReference, ValueEventListener> hashMap = new HashMap<>();


    public static void removeValueEventListener(HashMap<DatabaseReference, ValueEventListener> hashMap) {
        for (Map.Entry<DatabaseReference, ValueEventListener> entry : hashMap.entrySet()) {
            DatabaseReference databaseReference = entry.getKey();
            ValueEventListener valueEventListener = entry.getValue();
            databaseReference.removeEventListener(valueEventListener);
        }
    }

}
