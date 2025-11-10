# Location-Based Notes App

This is an Android note-taking application built with **Kotlin** and **Jetpack Compose**. The app allows users to create, edit, and view notes, with integrated location tracking and optional image attachments. Each note is linked to the user’s current location, and users can view their notes on a map with pins marking where they were created.  

This project demonstrates modern Android development patterns, state management with Compose, Firebase Authentication, and location services integration.

---

## Features

- **User Authentication**  
  Secure login and signup functionality using Firebase Authentication. The app remembers the last logged-in user, allowing for persistent login across app launches.

- **Create & Edit Notes**  
  Users can create notes with a title, body, and optional image. Empty titles or bodies are prevented, with immediate feedback provided via snackbars. Notes can be edited or deleted later.

- **Location Integration**  
  Notes automatically capture the user’s current city using the Fused Location Provider and Geocoder API. Proper runtime permission handling ensures the user experience remains smooth.

- **Map View with Pins**  
  A map mode displays all notes as pins at their recorded locations. Tapping a pin shows the note details, making it easy to visualize where notes were created.

- **Image Attachments**  
  Users can attach photos from the camera or gallery. Photos are displayed in both the edit and view screens. Notes without images show a placeholder to maintain UI consistency.

- **Modern UI with Jetpack Compose**  
  The app uses Compose for a clean, responsive, and interactive UI. State management, animations, and layouts are optimized for smooth user experience.

- **Persistent Login**  
  The app stores the last logged-in user locally using `SharedPreferences`. Users returning to the app do not need to re-enter credentials, streamlining the workflow.

---
