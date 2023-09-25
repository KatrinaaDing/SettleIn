package com.example.property_management.api;

import android.util.Log;

import com.example.property_management.callbacks.AddPropertyCallback;
import com.example.property_management.callbacks.DeletePropertyByIdCallback;
import com.example.property_management.callbacks.GetAllPropertiesCallback;
import com.example.property_management.callbacks.GetPropertyByIdCallback;
import com.example.property_management.data.NewProperty;
import com.example.property_management.data.Property;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;

public class FirebasePropertyRepository {
    private FirebaseFirestore db;
    public FirebasePropertyRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Check if the property exists in the db, if not exists, then add the property
     * To be added successfully, <address + href> must be unique
     * @param newProperty
     * @param callback
     */
    public void addProperty(NewProperty newProperty, AddPropertyCallback callback) {
        Query query = db.collection("properties")
                .whereEqualTo("address", newProperty.getAddress())
                .whereEqualTo("url", newProperty.getUrl());
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
//                Log.d("test", "addProperty: " + task.toString());
                Log.d("test", "addProperty: " + task.getResult().getDocuments());
                if (task.getResult().isEmpty()) { // property does not exist
                    db.collection("properties")
                            .add(newProperty)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d("add-property-success", "DocumentSnapshot written with ID: " + documentReference.getId());
                                    callback.onSuccess(documentReference.getId());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    if (e instanceof FirebaseFirestoreException) {
                                        FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) e;
                                        FirebaseFirestoreException.Code errorCode = firestoreException.getCode();

                                        switch (errorCode) {
                                            case PERMISSION_DENIED:
                                                Log.w("add-property-failure", "Error permission denied", e);
                                                callback.onError("Error permission denied");
                                                break;
                                            case UNAUTHENTICATED:
                                                Log.w("add-property-failure", "Error unauthenticated", e);
                                                callback.onError("Error unauthenticated");
                                            default:
                                                Log.w("add-property-failure", "Error adding property", e);
                                                callback.onError("Error adding property");
                                                break;
                                        }
                                    } else {
                                        Log.w("add-property-failure", "Non-Firebase Error adding property", e);
                                        callback.onError("Non-Firebase Error adding property");
                                    }
                                }
                            });

                } else {
                    callback.onError("Property with the same address and href already exists.");
                }
            } else {
                callback.onError("Error querying Firestore: " + task.getException());
            }
        });

    }

    /**
     * delte a property by id
     *
     * @param documentId
     * @param callback
     */
    public void deletePropertyById(String documentId, DeletePropertyByIdCallback callback) {
        db.collection("properties").document(documentId)
            .delete()
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("delete-property-success", "Property successfully deleted!");
                    callback.onSuccess("Property successfully deleted!");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof FirebaseFirestoreException) {
                        FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) e;
                        FirebaseFirestoreException.Code errorCode = firestoreException.getCode();

                        switch (errorCode) {
                            case PERMISSION_DENIED:
                                Log.w("delete-property-failure", "Error permission denied", e);
                                callback.onError("Error permission denied");
                                break;
                            case NOT_FOUND:
                                Log.w("delete-property-failure", "Error property not found", e);
                                callback.onError("Error property not found");
                                break;
                            default:
                                Log.w("delete-property-failure", "Error deleting property", e);
                                callback.onError("Error deleting property");
                                break;
                        }
                    } else {
                        Log.w("delete-property-failure", "Non-Firebase Error deleting property", e);
                        callback.onError("Non-Firebase Error deleting property");
                    }
                }
            });
    }

    /**
     * get a property by id
     * @param documentId
     * @param callback
     */
    public void getPropertyById(String documentId, GetPropertyByIdCallback callback) {
        DocumentReference docRef = db.collection("properties").document(documentId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("get-property-by-id-success", "DocumentSnapshot data: " + document.getData());
                        callback.onSuccess(document.toObject(Property.class));
                    } else {
                        Log.d("get-property-by-id-failure", "No such property");
                        callback.onError("No such property");
                    }
                } else {
                    Log.d("get-property-by-id-failure", "get failed with ", task.getException());
                    callback.onError("Error getting property");
                }
            }
        });
    }

    /**
     * get all the properties
     * @param callback
     */
    public void getAllProperties(GetAllPropertiesCallback callback) {
        db.collection("properties")
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        ArrayList<Property> properties = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d("get-all-properties-success", document.getId() + " => " + document.getData());
                            Property property = document.toObject(Property.class);
                            property.setPropertyId(document.getId());
                            properties.add(property);
                        }
                        callback.onSuccess(properties);
                    } else {
                        Log.d("get-all-properties-failure", "Error getting all properties: ", task.getException());
                        callback.onError("Error getting all properties");
                    }
                }
            });
    }

}
