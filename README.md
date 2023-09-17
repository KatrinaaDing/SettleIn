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
│               ├── data (Entity classes)
│               ├── mappers (Map entity to model and vice versa)
│               ├── ui 
│               │   ├── activities
│               │   └── fragments
│               └── utils (helper class)
└── res
    ├── drawable (Icons and images)
    ├── layout
    ├── menu
    ├── mipmap-anydpi-v26
    ├── mipmap-hdpi
    ├── mipmap-mdpi
    ├── mipmap-xhdpi
    ├── mipmap-xxhdpi
    ├── mipmap-xxxhdpi
    ├── navigation
    ├── values (constant values and styles)
    ├── values-night
    └── xml

```

## Cloud Firestore

- Get Instance

  ```java
  FirebasePropertyRepository db = new FirebasePropertyRepository();
  ```

- Add a Property

  ```java
  Property data = new Property();
  data.setAddress("TEST-ADD-PROPERTY");
  db.addProperty(data);
  ```

- Delete a Property by DocumentId

  ```java
  db.deletePropertyById("z2Bc0acDqmNHpz0WbsAP");
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

  ```java
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

  

​	
