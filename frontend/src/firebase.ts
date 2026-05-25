import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getFirestore } from "firebase/firestore";
import { getDatabase } from "firebase/database";

const firebaseConfig = {
  apiKey: "AIzaSyAdClH9QB0_9Zk_WS1-GIYrnkB8W6bJ-7A",
  authDomain: "terminal71-ems.firebaseapp.com",
  databaseURL:
    "https://terminal71-ems-default-rtdb.asia-southeast1.firebasedatabase.app",
  projectId: "terminal71-ems",
  storageBucket: "terminal71-ems.firebasestorage.app",
  messagingSenderId: "163017297489",
  appId: "1:163017297489:web:fc543b1093d3d135f73270",
};

const app = initializeApp(firebaseConfig);

export const auth = getAuth(app);
export const db = getFirestore(app);
export const rtdb = getDatabase(app); // Firebase Realtime Database
