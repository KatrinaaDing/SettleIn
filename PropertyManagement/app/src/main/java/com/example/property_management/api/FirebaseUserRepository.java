package com.example.property_management.api;

import android.util.Log;

import com.example.property_management.callbacks.GetAllPropertiesCallback;
import com.example.property_management.callbacks.GetAllUsersCallback;
import com.example.property_management.callbacks.GetPropertyByIdCallback;
import com.example.property_management.callbacks.GetUserInfoByIdCallback;
import com.example.property_management.data.Property;
import com.example.property_management.data.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;

public class FirebaseUserRepository {
    private FirebaseFirestore db;
    public FirebaseUserRepository() {
        db = FirebaseFirestore.getInstance();
    }
    public void addUser(User user) {
        db.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("add-user-success", "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("add-user-failure", "Error adding document", e);
                    }
                });
    }
    public void getUserInfoById(String documentId, GetUserInfoByIdCallback callback) {
        DocumentReference docRef = db.collection("users").document(documentId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("get-userinfo-by-id-success", "DocumentSnapshot data: " + document.getData());
                        callback.onSuccess(document.toObject(User.class));
                    } else {
                        Log.d("get-userinfo-by-id-failure", "No such document");
                        callback.onError(new Exception("No such document"));
                    }
                } else {
                    Log.d("get-userinfo-by-id-failure", "get failed with ", task.getException());
                    callback.onError(task.getException());
                }
            }
        });
    }

    public void getAllUsers(GetAllUsersCallback callback) {
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<User> users = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("get-all-users-success", document.getId() + " => " + document.getData());
                                User user = document.toObject(User.class);
                                users.add(user);
                            }
                            callback.onSuccess(users);
                        } else {
                            Log.d("get-all-users-failure", "Error getting documents: ", task.getException());
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    public void deleteUserById(String documentId) {
        db.collection("users").document(documentId)
            .delete()
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("delete-user-success", "DocumentSnapshot successfully deleted!");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w("delete-user-failure", "Error deleting document", e);
                }
            });
    }
}
