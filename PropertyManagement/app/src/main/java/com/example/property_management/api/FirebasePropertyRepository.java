package com.example.property_management.api;

import android.util.Log;

import com.example.property_management.callbacks.AddPropertyCallback;
import com.example.property_management.callbacks.DeletePropertyByIdCallback;
import com.example.property_management.callbacks.GetAllUserPropertiesCallback;
import com.example.property_management.callbacks.GetPropertyByIdCallback;
import com.example.property_management.data.NewProperty;
import com.example.property_management.data.Property;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        // check if the property exists by having the same address or href
        Query sameAddressQuery = db.collection("properties")
                .whereEqualTo("address", newProperty.getAddress());

        // handle result by a helper function
        sameAddressQuery.get().addOnCompleteListener(addressTask -> {
            String documentIdWidthSameAddress = checkExistProperty(addressTask, callback);
            if (documentIdWidthSameAddress == null) {
                // if result is null, return error
                callback.onError("Error querying Firestore: " + addressTask.getException());

            } else if (documentIdWidthSameAddress.equals("")) {
                // if the address does not exist in the database, check for duplicate href
                if (newProperty.getHref() == null) {
                    // if the newProperty's href is null, which means this is a manual input property
                    // add the property straight away
                    addNewProperty(newProperty, callback);
                } else {
                    // if the href is not null, check if the href is unique
                    Query sameHrefQuery = db.collection("properties")
                            .whereEqualTo("href", newProperty.getHref());
                    sameHrefQuery.get().addOnCompleteListener(hrefTask -> {
                        String documentIdWithSameHref = checkExistProperty(hrefTask, callback);
                        if (documentIdWithSameHref == null) {
                            // if result is null, return error
                            callback.onError("Error querying Firestore: " + hrefTask.getException());
                        } else if (documentIdWithSameHref.equals("")) {
                            // if the href is also not exist in the database, add the property
                            addNewProperty(newProperty, callback);
                        } else {
                            // if the href exists, return the document id of the property
                            callback.onSuccess(documentIdWithSameHref);
                        }
                    });
                }
            } else {
                // if the address exists, return the document id of the property
                callback.onSuccess(documentIdWidthSameAddress);
            }
        });

    }

    /**
     * Delete a property by id
     *
     * @param documentId the document id of the property
     * @param callback callback to handle success and error
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
     * add a new property into the database
     * @param newProperty the new property to be added
     * @param callback callback to handle success and error
     */
    private void addNewProperty(NewProperty newProperty, AddPropertyCallback callback){
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
    }

    /**
     * handle the result of checking if the property exists (identical href or address)
     * if the property does not exist, add the property
     * @param task the task to check if the property exists
     * @param callback callback to handle success and error
     */
    private String checkExistProperty(Task<QuerySnapshot> task, AddPropertyCallback callback) {
        if (task.isSuccessful()) {
            Log.d("check-exist-property", "addProperty: " + task.getResult().getDocuments());
            QuerySnapshot querySnapshot = task.getResult();
            if (querySnapshot != null) {
                // property does not exist, add the property
                if (querySnapshot.isEmpty()) {
                    return "";
                } else {
                    List<DocumentSnapshot> documents = querySnapshot.getDocuments();
                    if (!documents.isEmpty()) {
                        String documentId = documents.get(0).getId();
                        Log.d("add-property", "property exists with document id " + documentId);
                        return documentId;
                    } else {
                        return "";
                    }
                }
            }
        }
        return null;
    }
}
