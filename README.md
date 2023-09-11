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
│               │   ├── PropertyAdapter.java
│               │   └── UserAdapter.java
│               ├── api (Firebase related classes)
│               │   ├── FirebasePropertyRepository.java
│               │   └── FirebaseUserRepository.java
│               ├── data (Entity classes)
│               │   ├── Property.java
│               │   └── User.java
│               ├── mappers (Map entity to model and vice versa)
│               │   └── PropertyMapper.java
│               ├── ui 
│               │   ├── activities
│               │   │   ├── AddPropertyActivity.java
│               │   │   └── MainActivity.java
│               │   └── fragments
│               │       ├── dashboard
│               │       │   ├── DashboardFragment.java
│               │       │   └── DashboardViewModel.java
│               │       ├── home
│               │       │   ├── HomeFragment.java
│               │       │   └── HomeViewModel.java
│               │       ├── profile
│               │       │   ├── ProfileFragment.java
│               │       │   └── ProfileViewModel.java
│               │       └── property
│               └── utils
└── res
    ├── drawable (Icons and images)
    ├── layout
    │   ├── activity_add_property.xml
    │   ├── activity_main.xml
    │   ├── fragment_dashboard.xml
    │   ├── fragment_home.xml
    │   └── fragment_profile.xml
    ├── menu
    │   └── bottom_nav_menu.xml
    ├── mipmap-anydpi-v26
    ├── mipmap-hdpi
    ├── mipmap-mdpi
    ├── mipmap-xhdpi
    ├── mipmap-xxhdpi
    ├── mipmap-xxxhdpi
    ├── navigation
    ├── values (constant values and styles)
    │   ├── colors.xml
    │   ├── dimens.xml
    │   ├── strings.xml
    │   └── themes.xml
    ├── values-night
    │   └── themes.xml
    └── xml

```

