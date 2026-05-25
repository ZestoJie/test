import { useEffect, useState } from "react";
import { subscribeToRealtimeData } from "../services/realtime.service";

interface UseRealtimeDataOptions {
  onError?: (error: any) => void;
}

/**
 * React hook to subscribe to Firebase Realtime Database updates
 * @param path - Path in the database (e.g., "users/user123")
 * @param options - Optional error handler
 * @returns Object with data, loading state, and error
 */
export const useRealtimeData = (
  path: string,
  options?: UseRealtimeDataOptions,
) => {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<any>(null);

  useEffect(() => {
    if (!path) {
      setData(null);
      setLoading(false);
      return;
    }

    const unsubscribe = subscribeToRealtimeData(
      path,
      (newData) => {
        setData(newData);
        setLoading(false);
        setError(null);
      },
      (err) => {
        setError(err);
        setLoading(false);
        if (options?.onError) {
          options.onError(err);
        }
      },
    );

    return () => {
      unsubscribe();
    };
  }, [path, options]);

  return { data, loading, error };
};

/**
 * Hook for monitoring multiple paths simultaneously
 * @param paths - Array of database paths to monitor
 * @returns Object with data for each path
 */
export const useRealtimeDataMultiple = (paths: string[]) => {
  const [dataMap, setDataMap] = useState<{ [key: string]: any }>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const unsubscribes: (() => void)[] = [];

    paths.forEach((path) => {
      const unsub = subscribeToRealtimeData(path, (data) => {
        setDataMap((prev) => ({
          ...prev,
          [path]: data,
        }));
        setLoading(false);
      });
      unsubscribes.push(unsub);
    });

    return () => {
      unsubscribes.forEach((unsub) => unsub());
    };
  }, [paths]);

  return { dataMap, loading };
};
