rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    // This allows any logged-in user to read/write to the "users" collection
    match /users/{userId} {
      allow read, write: if request.auth != null;
    }
    
    // If you have a tasks collection, add this as well:
    match /tasks/{taskId} {
      allow read, write: if request.auth != null;
    }
  }
}