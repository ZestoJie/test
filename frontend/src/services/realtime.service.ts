import { ref, set, get, update, remove, onValue } from "firebase/database";
import { rtdb } from "../firebase";

// Set data in Realtime Database
export const setRealtimeData = async (path: string, data: any) => {
  try {
    await set(ref(rtdb, path), data);
    console.log(`Data set successfully at ${path}`);
  } catch (error) {
    console.error(`Error setting data at ${path}:`, error);
    throw error;
  }
};

// Get data from Realtime Database (one-time read)
export const getRealtimeData = async (path: string) => {
  try {
    const snapshot = await get(ref(rtdb, path));
    if (snapshot.exists()) {
      return snapshot.val();
    } else {
      console.log(`No data found at ${path}`);
      return null;
    }
  } catch (error) {
    console.error(`Error getting data from ${path}:`, error);
    throw error;
  }
};

// Update data in Realtime Database
export const updateRealtimeData = async (path: string, updates: any) => {
  try {
    await update(ref(rtdb, path), updates);
    console.log(`Data updated successfully at ${path}`);
  } catch (error) {
    console.error(`Error updating data at ${path}:`, error);
    throw error;
  }
};

// Delete data from Realtime Database
export const deleteRealtimeData = async (path: string) => {
  try {
    await remove(ref(rtdb, path));
    console.log(`Data deleted successfully at ${path}`);
  } catch (error) {
    console.error(`Error deleting data at ${path}:`, error);
    throw error;
  }
};

// Subscribe to real-time updates
export const subscribeToRealtimeData = (
  path: string,
  callback: (data: any) => void,
  errorCallback?: (error: any) => void,
) => {
  const dbRef = ref(rtdb, path);

  const unsubscribe = onValue(
    dbRef,
    (snapshot) => {
      if (snapshot.exists()) {
        callback(snapshot.val());
      } else {
        callback(null);
      }
    },
    (error) => {
      console.error(`Error subscribing to ${path}:`, error);
      if (errorCallback) {
        errorCallback(error);
      }
    },
  );

  return unsubscribe; // Return function to unsubscribe
};

// Batch write operations
export const batchRealtimeUpdate = async (updates: { [path: string]: any }) => {
  try {
    const promises = Object.entries(updates).map(([path, value]) =>
      set(ref(rtdb, path), value),
    );
    await Promise.all(promises);
    console.log("Batch update completed successfully");
  } catch (error) {
    console.error("Error in batch update:", error);
    throw error;
  }
};
