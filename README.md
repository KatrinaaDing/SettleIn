# COMP90018-Property-Management

link to UI design: https://balsamiq.cloud/sl7n0yg/pxcxa1b/r42DE



## Open the project

1. Open Android Studio
2. Select "Open"
3. Select `PropertyManagement` directory.
4. Should be able to run the application



## Project src file structure

```
PropertyManagement/app/src/main
├── AndroidManifest.xml
├── java
│   └── com
│       └── example
│           └── property_management
│               ├── adapters
│               ├── api (Firebase related classes)
│               ├── callbacks (Callback interfaces)
│               ├── data (Entity classes)
│               ├── sensors (Sensor classes)
│               ├── ui 
│               │   ├── activities
│               │   └── fragments
│               └── utils (helper class)
└── res
    ├── drawable (Icons and images)
    ├── layout
    ├── menu
    ├── mipmap
    ├── navigation
    ├── values (constant values and styles)
    └── xml

```
## [Cloud Firestore](https://github.com/KatrinaaDing/COMP90018-Property-Management/tree/db-update#cloud-firestore)

Access Property Collection:

- Get Instance

  ```java
  FirebasePropertyRepository db = new FirebasePropertyRepository();
  ```

- Add a Property

  ```java
  Property p1 = new Property();
  p1.setAddress("address");
  p1.setHref("href");
  db.addProperty(p1, new AddPropertyCallback() {
      @Override
      public void onSuccess(String documentId) {
          Log.d("test", "onSuccess: successfully added property" + documentId);
      }
  
      @Override
      public void onError(String msg) {
          Log.d("test", "onError: " + msg);
      }
  });
  ```

- Delete a Property by DocumentId

  ```java
  db.deletePropertyById("Jc6QLF7fDiqHbYIdgyIC", new DeletePropertyByIdCallback() {
      @Override
      public void onSuccess(String msg) {
          Log.d("test-delete", "onSuccess: " + msg);
      }
  
      @Override
      public void onError(String msg) {
  
      }
  });
  ```

- Get a Property Object by DocumentId

  ```java
  db.getPropertyById("wi4G8Ezy1TEq9nk1QhGs", new GetPropertyByIdCallback() {
      @Override
      public void onSuccess(Property property) {
          Log.d("get-property-by-id-onSuccess", "onSuccess: " + property.getAddress());
      	// access property object here
      }
  
      @Override
      public void onError(Exception e) {
  	
      }
  });
  ```

- Get all Properties

  ```
  db.getAllProperties(new GetAllPropertiesCallback() {
      @Override
      public void onSuccess(ArrayList<Property> properties) {
          Log.d("get-all-properties-onSuccess", "onSuccess: " + properties.size());
      }
  
      @Override
      public void onError(Exception e) {
  
      }
  });
  ```

  

Access User Collection:

- Get Instance:

  ```java
  FirebaseUserRepository db = new FirebaseUserRepository();
  ```

- Update User fields

  ```java
  HashMap<String, Object> updates = new HashMap<>();
  updates.put("userEmail", "test@email.com");
  updates.put("userName", "Bob");
  db.updateUserFields("co9z891lbRxIIDV7zP8L", updates, new UpdateUserCallback() {
      @Override
      public void onSuccess(String msg) {
          Log.d("test", "onSuccess: " + msg);
      }
  
      @Override
      public void onError(String msg) {
  
      }
  });
  ```

  
