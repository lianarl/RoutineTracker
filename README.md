# Routine Tracker

An Android app for creating daily routines and keeping track of whether you actually do them. You set up routines like studying, exercising, or socialising, choose when they happen, and the app reminds you ahead of time and quietly records whether you stuck to them.

## What it does

- Create, edit, and delete routines with a name, type, start/end time, days of the week, and an optional reminder.
- See all your routines in one list, each marked as completed or missed.
- Open a routine to view its execution history and a short suggestion tailored to the current conditions.
- Get a notification before a routine starts, with advice based on the weather, the room's light level, and the routine type. For example, an exercise routine suggests staying indoors when it's very hot or very cold, and a study routine flags when the lighting is poor.
- Adjust your location, how early reminders fire, and whether notifications are on at all in the settings. There's also a reset option.

## How completion is tracked

The app doesn't ask you to tick anything off. Instead, it records when it was last in the foreground and background, and an hourly background job checks whether you had the app open during a routine's time window. If you did, the routine counts as completed for that day. If the window has passed and you didn't, it's marked as missed.

## Architecture

The project follows MVVM. Fragments talk to ViewModels, which talk to a single Repository that hides where the data actually comes from. Underneath, the Repository pulls from three sources: a local Room database for routines and their execution history, the OpenWeatherMap API for weather, and the device's light sensor. Settings and the foreground/background timestamps live in SharedPreferences.

It uses a single-activity setup, with one `MainActivity` hosting fragments through the Navigation Component. Most screens are classic XML layouts with View Binding, except the routine list, which is written in Jetpack Compose. Database work and network calls run off the main thread using coroutines, and the UI stays in sync through LiveData.

## Tech stack

- Kotlin, coroutines, LiveData
- Room (SQLite) with two related entities
- Jetpack Compose (routine list) and View Binding / XML (everything else)
- Navigation Component, single-activity
- WorkManager for the periodic completion check
- AlarmManager and a BroadcastReceiver for scheduled reminders
- Retrofit + Gson for the weather API
- SensorManager for the ambient light reading
- SharedPreferences and PreferenceFragmentCompat for settings

## Screenshot of the main screen
<img width="540" height="1200" alt="Screenshot_20260608_111928" src="https://github.com/user-attachments/assets/b4471a5c-3a8a-479a-97b7-6517f5dbb444" />

## How to run

1. Clone the repo and open it in Android Studio.
2. Get a free API key from [OpenWeatherMap](https://openweathermap.org/api) and use your own key for the weather request in `RoutineRepository`.
3. Build and run on a device or emulator running Android 8.1 (API 27) or newer.

For weather suggestions to work you'll need an internet connection, and the light-based suggestions rely on the device having a light sensor.
