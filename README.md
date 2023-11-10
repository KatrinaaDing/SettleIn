# Project for COMP90018 -- SettleIn

This app is designed to help renters organize, manage, and compare different rental properties to make an informed decision. 

10min video demo: https://youtu.be/ermcBt6hqvg

## Authors

* Ziqi Ding, ziqiding@student.unimelb.edu.au
* Lan Lin, lalin2@student.unimelb.edu.au 
* Qiao Chen, qiao.chen3@student.unimelb.edu.au
* Shiwen Zhang, shiwezhang@student.unimelb.edu.au
* Yuanlin Liu, yuanlin1@student.unimelb.edu.au

## Run the project

### Preparation

1. To run the project, you will need:

   1. Google Map API key

      Add this line in `PropertyManagement/local.properties`. Replace `<your_api_key>` with your actual Google Map API key

      ```
      MAPS_API_KEY="<your_api_key>"
      ```

   2. `google-service.json`

      This file is for integrating Firebase services. Put this file in `PropertyManagement/app`.

### Run the app

After placing the file and the api key correctly, you can run the app through steps below.

1. Open Android Studio

2. Select "Open"

3. Select `PropertyManagement` directory.

4. To ensure the app works properly, we recommend using **Pixel 6 API 30** as emulator. Here's the detail of the device.

   ```
   Properties
   avd.ini.displayname              Pixel 6 API 30
   avd.ini.encoding                 UTF-8
   AvdId                            Pixel_6_API_30
   disk.dataPartition.size          6G
   fastboot.chosenSnapshotFile
   fastboot.forceChosenSnapshotBoot no
   fastboot.forceColdBoot           no
   fastboot.forceFastBoot           yes
   hw.accelerometer                 yes
   hw.arc                           false
   hw.audioInput                    yes
   hw.battery                       yes
   hw.camera.back                   virtualscene
   hw.camera.front                  emulated
   hw.cpu.ncore                     4
   hw.device.hash2                  MD5:3db3250dab5d0d93b29353040181c7e9
   hw.device.manufacturer           Google
   hw.device.name                   pixel_6
   hw.dPad                          no
   hw.gps                           yes
   hw.gpu.enabled                   yes
   hw.gpu.mode                      auto
   hw.initialOrientation            Portrait
   hw.keyboard                      yes
   hw.lcd.density                   420
   hw.lcd.height                    2400
   hw.lcd.width                     1080
   hw.mainKeys                      no
   hw.ramSize                       1536
   hw.sdCard                        yes
   hw.sensors.orientation           yes
   hw.sensors.proximity             yes
   hw.trackBall                     no
   image.androidVersion.api         30
   image.sysdir.1                   system-images/android-30/google_apis/arm64-v8a/
   PlayStore.enabled                false
   runtime.network.latency          none
   runtime.network.speed            full
   showDeviceFrame                  yes
   skin.dynamic                     yes
   tag.display                      Google APIs
   tag.id                           google_apis
   vm.heapSize                      228
   ```

5. Should be able to run the application.

## Features

### 1. Account management

- **Register**

  Users can register a new account by providing a valid email address, username and password.

- **Login**

  We have a straightforward login interface that allows users to log in by entering their email and password. If a user forgets their password, we also provide a reset password button on the Login page.

- **Reset Password**

  - We have set up password reset buttons in two places:
    - On the user login interface. If a user forgets their login password, they need to provide their email and click the reset password button, and a link to change the password will be sent to their email. The user can click on the link to set a new password.
    - For users who are already logged in, on the Edit Profile page, there is no need for the user to enter their email. As soon as the user clicks the reset password button, a link to modify the password will be sent to the user's registered email address.

- **Edit Profile**

  Logged-in users can modify the username, email, and password on the Edit Profile page. Note: When users modify their email, they need to provide their password.

### 2. Property management

- **Add New Property**

  - The user has two ways to add a new property:
  - The user can enter a link from the rental app and our app will crawl key information like price, pictures, number of bedrooms, number of bathrooms, and number of parking Spaces. Note: Users are not allowed to manually modify the crawled information if the URL is used to automatically crawl the information.
  - Users can manually input the address, price, number of bedrooms, number of bathrooms, and number of parking spaces to add a new property.

- **Delete a Property**

  The user can delete a property that has been added previously.

- **Sort Properties**

  On the HomePage, the user can sort the properties by Create Time, Price, Beds, Bathrooms, Parking Spaces.

- **Filter Properties**

  On the HomePage, the user can Filter the properties by Inspected, Not Inspected, All.

- **List View**

  On the HomePage, the user can view the properties as a list.

- **Map View**

  On the HomePage, the user can view the added properties on the map. The red marker refers to the uninspected properties and the green marker refers to the inspected properties.

### 3. View property detail

- **Show property basic information**

  - When users enter the Property Detail page, they will first see the basic composition and transaction information of the property, including:
    - Preview images of the property (provided by the rental platform).
    - The price of the property, the number of bedrooms, bathrooms, and parking spaces.
    - The date the user added this property to the app.

- **Property web page Navigation** 

  Users can click the “view site”button to access the platform page where the property listing is posted.

- **Set property inspection date** 

  - Users can choose the exact date in which they want to inspect the property. They can select a date on a calendar and choose the time of that day on a clock view.
  - On the right side of the inspection date selector, the check box indicates whether the property has been inspected. Users can simply click this checkbox to set its inspection status.

- **Show property location through map**

  Users can see the location of the property on an embedded Google Maps, by which they can scroll and zoom the map page to display map details or panoramic views.

- **Show interested facilities information**
  - Once a series of interested facilities were added in the user profile page, users will see the location and traffic Information about each facility being listed.
  - The name and detailed address of this facility are displayed in the content area, in which users can be navigated to a google map showing the location of this facility by clicking the content area.
  - Users can clearly see the distance between the property and each facility, as well as the estimated travel time by car, public transit, or walking to reach that facility.
  - Clicking the interested facility/location item in property detail page can redirect to google map to show routes

- **Property inspection data illustration**
  - Once users have conducted their first inspection, the inspection data of each room will be illustrated, including photos, noise, notes, brightness and orientation.
  - If they have not inspected the property, there are notice information in property condition and note areas.
  - If there is no photo in the room, the photo UI will be invisible. If the photo cannot be found locally by the uploaded URL, the UI will display one place holder.
  - The noise has three levels including green normal (< 35 dB), yellow risk (35~50 dB) and red high risk (>50 dB). When user click such tag, they can see information about appropriate noise level for home
  - User can click information icon in the line of brightness to see appropriate light level in home

### 4. Property inspection process

- **Set inspection time + calendar event**
  - Users can set inspection dates and time as well as reset them. They can both enter by text board and choose in calendar
  -  The app will check whether the inspection time is valid. If not, it will notify users.
  -  After setting a valid inspection date, the time can be added into the local calendar with a duration of 30 minutes. This inspection event includes property location, date, time, duration and note.
- **Nearby inspection alert**
  - When users go to property and they come nearby it, the app will automatically alter user
- **Data collection**
  - There will be a pop-up tutorial about how to use sensors to collect room data, when users first download the app
  - Write notes by text or voice input during inspection
  - Users can edit room names as they like. But they cannot enter a duplicate name
  - Users can collect inspection data about lounge room, bedrooms or others by photos or measuring noise, brightness and window orientation
  - The light level is in Lux, audio is in dB and window orientation is in degree and orientation, which is clear for user
  - Users can collect light, audio and compass data by pressing the corresponding “Test” button.
  - The sensors will collect data in 3 seconds and give the average number of data
  - When the sensor is working, the “Test” button changes to the red “Cancel” button. Pressing such a “Cancel” button can stop the target sensor from working and UI becomes to previous data.
  - The “Cancel” button becomes “Test” after sensors’ 3-second work 
  - Use can take photo by camera sensor or upload photos from their library
  - Users can see how many photos are added in each room as well as check or delete the added photos. If the photo is lost, there will be a placeholder.
  - Users can upload all collected data by “finish” button to firebase, then they will enter the property details page automatically and see the inspected data.
  - Users can view collected property data at the property detail page.

## Project src file structure

```shell
.
├── PropertyManagement			# <==== MAPS_API_KEY goes into local.properties
│   ├── app						# <==== google-services.json goes here
│   │   ├── build
│   │   └── src
│   │       ├── androidTest
│   │       ├── main
│   │       │   ├── java
│   │       │   │   └── com
│   │       │   │       └── example
│   │       │   │           └── property_management
│   │       │   │               ├── adapters		# Adapters
│   │       │   │               ├── api				# Repository and firebase helpers
│   │       │   │               ├── callbacks		# Call back interfaces
│   │       │   │               ├── data			# Data class
│   │       │   │               ├── sensors			# Sensor class
│   │       │   │               ├── ui
│   │       │   │               │   ├── activities	# Activity class
│   │       │   │               │   └── fragments	# Fragment class
│   │       │   │               └── utils			# Helpers
│   │       │   └── res
│   │       └── test
│   └── gradle					
└── firebase
    └── functions
```
