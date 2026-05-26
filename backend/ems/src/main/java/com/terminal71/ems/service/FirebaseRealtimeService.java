package com.terminal71.ems.service;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@ConditionalOnBean(FirebaseDatabase.class)
public class FirebaseRealtimeService {

  private static final Logger log = LoggerFactory.getLogger(FirebaseRealtimeService.class);
  private final FirebaseDatabase firebaseDatabase; // may be null when not configured

  public FirebaseRealtimeService(@org.springframework.lang.Nullable FirebaseDatabase firebaseDatabase) {
    this.firebaseDatabase = firebaseDatabase;
  }

  /**
   * Write data to the Realtime Database
   */
  public CompletableFuture<Void> writeData(String path, Object data) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    try {
      if (firebaseDatabase == null) {
        throw new IllegalStateException("FirebaseDatabase not configured");
      }
      DatabaseReference ref = firebaseDatabase.getReference(path);
      ref.setValue(data, (error, ref1) -> {
        if (error != null) {
          log.error("Failed to write data to " + path, error.toException());
          future.completeExceptionally(error.toException());
        } else {
          log.info("Successfully wrote data to " + path);
          future.complete(null);
        }
      });
    } catch (Exception e) {
      log.error("Error writing data to " + path, e);
      future.completeExceptionally(e);
    }
    return future;
  }

  /**
   * Read data from the Realtime Database (one-time read)
   */
  public CompletableFuture<DataSnapshot> readData(String path) {
    CompletableFuture<DataSnapshot> future = new CompletableFuture<>();
    try {
      if (firebaseDatabase == null) {
        throw new IllegalStateException("FirebaseDatabase not configured");
      }
      DatabaseReference ref = firebaseDatabase.getReference(path);
      ref.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot snapshot) {
          log.info("Successfully read data from " + path);
          future.complete(snapshot);
        }

        @Override
        public void onCancelled(DatabaseError error) {
          log.error("Failed to read data from " + path, error.toException());
          future.completeExceptionally(error.toException());
        }
      });
    } catch (Exception e) {
      log.error("Error reading data from " + path, e);
      future.completeExceptionally(e);
    }
    return future;
  }

  /**
   * Update specific fields in the database
   */
  public CompletableFuture<Void> updateData(String path, Map<String, Object> updates) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    try {
      if (firebaseDatabase == null) {
        throw new IllegalStateException("FirebaseDatabase not configured");
      }
      DatabaseReference ref = firebaseDatabase.getReference(path);
      ref.updateChildren(updates, (error, ref1) -> {
        if (error != null) {
          log.error("Failed to update data at " + path, error.toException());
          future.completeExceptionally(error.toException());
        } else {
          log.info("Successfully updated data at " + path);
          future.complete(null);
        }
      });
    } catch (Exception e) {
      log.error("Error updating data at " + path, e);
      future.completeExceptionally(e);
    }
    return future;
  }

  /**
   * Delete data from the Realtime Database
   */
  public CompletableFuture<Void> deleteData(String path) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    try {
      if (firebaseDatabase == null) {
        throw new IllegalStateException("FirebaseDatabase not configured");
      }
      DatabaseReference ref = firebaseDatabase.getReference(path);
      ref.removeValue((error, ref1) -> {
        if (error != null) {
          log.error("Failed to delete data at " + path, error.toException());
          future.completeExceptionally(error.toException());
        } else {
          log.info("Successfully deleted data at " + path);
          future.complete(null);
        }
      });
    } catch (Exception e) {
      log.error("Error deleting data at " + path, e);
      future.completeExceptionally(e);
    }
    return future;
  }

  /**
   * Get reference to a path in the database
   */
  public DatabaseReference getReference(String path) {
    if (firebaseDatabase == null) throw new IllegalStateException("FirebaseDatabase not configured");
    return firebaseDatabase.getReference(path);
  }

  /**
   * Get the root reference
   */
  public DatabaseReference getRootReference() {
    if (firebaseDatabase == null) throw new IllegalStateException("FirebaseDatabase not configured");
    return firebaseDatabase.getReference();
  }
}
