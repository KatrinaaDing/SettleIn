package com.example.property_management.api;

import android.util.Log;

import com.example.property_management.callbacks.AddUserCallback;
import com.example.property_management.callbacks.DeletePropertyByIdCallback;
import com.example.property_management.callbacks.DeleteUserByIdCallback;
import com.example.property_management.callbacks.GetAllUserPropertiesCallback;
import com.example.property_management.callbacks.GetAllUsersCallback;
import com.example.property_management.callbacks.GetUserInfoByIdCallback;
import com.example.property_management.callbacks.UpdateUserCallback;
import com.example.property_management.callbacks.DeleteInterestedFacilityCallback;
import com.example.property_management.data.Property;
import com.example.property_management.data.User;
import com.example.property_management.data.UserProperty;
import com.example.property_management.utils.DateTimeFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseUserRepository {
    private FirebaseFirestore db;
    public FirebaseUserRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Adds a new user to the database and handles the result.
     *
     * @param user The User object containing the user's information.
     * @param callback The AddUserCallback that will be called after the operation is complete.
     *                 It will provide either the userId upon success or an error message on failure.
     */
    public void addUser(User user, AddUserCallback callback) {
        Map<String, Object> userPayload = new HashMap<>();
        db.collection("users")
                .document(user.getUserId())
                .set(userPayload)
                .addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.d("add-user-success", "DocumentSnapshot written with ID: " + user.getUserId());
                        callback.onSuccess(user.getUserId());
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
                                    Log.w("add-user-failure", "Error permission denied", e);
                                    callback.onError("Error permission denied");
                                    break;
                                default:
                                    Log.w("add-user-failure", "Error adding user", e);
                                    callback.onError("Error adding user");
                                    break;
                            }
                        } else {
                            Log.w("add-user-failure", "Non-Firebase Error adding user", e);
                            callback.onError("Non-Firebase Error adding property");
                        }
                    }
                });
    }

    /**
     * retrieves user information by a given document ID from the Firestore database.
     * @param documentId The ID of the user document to fetch from the database.
     * @param callback   The GetUserInfoByIdCallback that will handle the response or error.
     */
    public void getUserInfoById(String documentId, GetUserInfoByIdCallback callback) {
        DocumentReference docRef = db.collection("users").document(documentId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("get-userinfo-by-id-success", "DocumentSnapshot data: " + document.getData());
                        User user = new User(
                                document.getId(),
                                document.get("interestedFacilities") == null ? new ArrayList<>() : (ArrayList<String>) document.get("interestedFacilities"),
                                document.get("interestedLocations") == null ? new ArrayList<>() : (ArrayList<String>) document.get("interestedLocations"),
                                document.get("locationNames") == null ? new ArrayList<>() : (ArrayList<String>) document.get("locationNames"),
                                document.get("properties") == null ? new HashMap<>() : (HashMap<String, UserProperty>) document.get("properties")
                        );
                        callback.onSuccess(user);
                    } else {
                        Log.d("get-userinfo-by-id-failure", "No such user");
                        callback.onError("No such user");
                    }
                } else {
                    Log.d("get-userinfo-by-id-failure", "get failed with ", task.getException());
                    callback.onError("Get userinfo by id failed");
                }
            }
        });
    }

    /**
     * update one or more User fields by the document id
     *
     * Example usage:
     *  HashMap<String, Object> updates = new HashMap<>();
     *  updates.put("userEmail", "test@email.com");
     *  updates.put("userName", "Bob");
     *  db.updateUserFields(documentId, updates, new UpdateUserCallback() {...});
     *
     * @param documentId
     * @param updates
     * @param callback
     */
    public void updateUserFields(String documentId, HashMap<String, Object> updates, UpdateUserCallback callback) {
        db.collection("users").document(documentId)
            .update(updates)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d("update-user-fields", "onSuccess: " + "Document " + documentId + " successfully updated.");
                    callback.onSuccess("Document " + documentId + " successfully updated.");
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
                                Log.w("update-user-fields", "Error permission denied", e);
                                callback.onError("Error permission denied");
                                break;
                            case NOT_FOUND:
                                Log.w("update-user-fields", "Error user not found", e);
                                callback.onError("Error user not found");
                                break;
                            default:
                                Log.w("update-user-fields", "Error updating user", e);
                                callback.onError("Error updating user");
                                break;
                        }
                    } else {
                        Log.w("update-user-fields", "Non-Firebase Error updating user", e);
                        callback.onError("Non-Firebase Error updating user");
                    }
                }
            });
    }

    /**
     * get all user properties
     * @param callback
     */
    public void getAllUserProperties(GetAllUserPropertiesCallback callback) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        DocumentReference docRef = db.collection("users").document(currentUser.getUid());
        CollectionReference collecRef = db.collection("properties");
        //        Log.d("test-get-user-properties", "getAllUserProperties: " + currentUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    User user = snapshot.toObject(User.class);
                    List<String> docIds = new ArrayList<>();
                    if (user != null) {
                        HashMap<String, UserProperty> userProperties = task.getResult().toObject(User.class).getProperties();
                        if (userProperties != null && !userProperties.isEmpty()) {
                            for (String docId : userProperties.keySet()) {
                                docIds.add(docId);
                            }
                        }

                        Log.d("get-all-user-properties-success", docIds.toString());
                        if (docIds != null && !docIds.isEmpty()) {
                            collecRef.whereIn(FieldPath.documentId(), docIds)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@androidx.annotation.NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                ArrayList<Property> properties = new ArrayList<>();
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    Log.d("get-all-user-properties-success",
                                                            document.getId() + " => " + document.getData());
                                                    Property property = document.toObject(Property.class);
                                                    property.setPropertyId(document.getId());
                                                    property.setInspected(userProperties.get(document.getId()).getInspected());
                                                    property.setCreatedAt(userProperties.get(document.getId()).getCreatedAt());
                                                    property.setInspectionDate(
                                                            DateTimeFormatter.stringToDate(userProperties.get(document.getId()).getInspectionDate())
                                                    );
                                                    property.setInspectionTime(
                                                            DateTimeFormatter.stringToTime(userProperties.get(document.getId()).getInspectionTime())
                                                    );

                                                    properties.add(property);
                                                }
                                                callback.onSuccess(properties);
                                            } else {
                                                Log.d("get-all-user-properties-failure",
                                                        "Error getting all user properties: ",
                                                        task.getException());
                                                callback.onError("Error getting all user properties");
                                            }
                                        }
                                    });
                        } else {
                            callback.onSuccess(new ArrayList<>());
                        }
                    }
                } else {
                    Log.d("get-all-user-properties-failure", "Error getting all properties of the user: ", task.getException());
                    callback.onError("Error getting all properties of the user");
                }
            }
        });
    }

    /**
     * delete a property from the user's properties
     * @param documentId property id
     * @param callback callback
     */
    public void deleteUserProperty(String documentId, DeletePropertyByIdCallback callback) {
        // retrieve current user id
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        // create key
        String key = "properties." + documentId;
        // create payload
        HashMap<String, Object> payload = new HashMap<>();
        payload.put(key, FieldValue.delete());
        // update user
        updateUserFields(userId, payload, new UpdateUserCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d("delete-user-property", "onSuccess: " + message);
                callback.onSuccess("Property deleted successfully");
            }

            @Override
            public void onError(String message) {
                Log.d("delete-user-property", "onError: " + message);
                callback.onError("Cannot delete property:" + message);
            }
        });

    }

    public void deleteInterestedFacilityLocation(ArrayList<String> propertyIds, Boolean isFacility, ArrayList<String> interestedList, ArrayList<String> locationNamesList, String interest_, DeleteInterestedFacilityCallback callback) {
        // retrieve current user id
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        DocumentReference userRef = db.collection("users").document(userId);

        // remove the facility from the interestedList
        HashMap<String, Object> payload = new HashMap<>();

        if (isFacility) {
            // update the interestedFacilities field in firebase
            payload.put("interestedFacilities", interestedList);
        } else {
            // update the interestedLocations field in firebase
            payload.put("interestedLocations", interestedList);
            payload.put("locationNames", locationNamesList);
        }
        userRef.update(payload);

        // Create a WriteBatch for this document
        WriteBatch batch = db.batch();
        Map<String, Object> updates = new HashMap<>();

        // keep only numbers and letters of interested facility/location to generate key
        String interest = interest_.replaceAll("[^a-zA-Z0-9]", "");

        // Iterate over each propertyId
        for (String propertyId : propertyIds) {
            String path = "properties."+propertyId+".distances."+interest;
            // Specify the fields to delete
            updates.put(path, FieldValue.delete());
        }

        // Update the document in the batch
        batch.update(userRef, updates);

        // Commit the batch
        batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Successfully deleted the specified fields
                // Handle success here
                callback.onSuccess("Successfully deleted "+interest);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof FirebaseFirestoreException) {
                    FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) e;
                    FirebaseFirestoreException.Code errorCode = firestoreException.getCode();

                    switch (errorCode) {
                        case PERMISSION_DENIED:
                            Log.w("update-user-fields", "Error permission denied", e);
                            callback.onError("Error permission denied");
                            break;
                        case NOT_FOUND:
                            Log.w("update-user-fields", "Error user not found", e);
                            callback.onError("Error user not found");
                            break;
                        default:
                            Log.w("update-user-fields", "Error updating user", e);
                            callback.onError("Error updating user");
                            break;
                    }
                } else {
                    Log.w("delete facility", "Non-Firebase Error deleting facility", e);
                    callback.onError("Non-Firebase Error deleting facility");
                }
            }
        });

    }
}
