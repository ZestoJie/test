package com.terminal71.ems.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;

@Service
@ConditionalOnBean(Firestore.class)
public class FirebaseFirestoreService {

  private static final Logger log = LoggerFactory.getLogger(FirebaseFirestoreService.class);
  private final Firestore firestore;

  public FirebaseFirestoreService(@org.springframework.lang.Nullable Firestore firestore) {
    this.firestore = firestore;
  }

  public DocumentSnapshot readDocument(@Nonnull String collection, @Nonnull String documentId)
      throws InterruptedException, ExecutionException {
    if (firestore == null) {
      throw new IllegalStateException("Firestore not configured");
    }
    ApiFuture<DocumentSnapshot> future = firestore.collection(collection).document(documentId).get();
    DocumentSnapshot snapshot = future.get();
    log.info("Read Firestore document {}/{}", collection, documentId);
    return snapshot;
  }

  public void writeDocument(@Nonnull String collection, @Nonnull String documentId, @Nonnull Object data)
      throws InterruptedException, ExecutionException {
    if (firestore == null) {
      throw new IllegalStateException("Firestore not configured");
    }
    firestore.collection(collection).document(documentId).set(data).get();
    log.info("Wrote Firestore document {}/{}", collection, documentId);
  }

  public QuerySnapshot queryCollection(@Nonnull String collection, @Nonnull String field, Object value)
      throws InterruptedException, ExecutionException {
    if (firestore == null) {
      throw new IllegalStateException("Firestore not configured");
    }
    ApiFuture<QuerySnapshot> future = firestore.collection(collection).whereEqualTo(field, value).get();
    QuerySnapshot snapshot = future.get();
    log.info("Queried Firestore collection {} for {}={}", collection, field, value);
    return snapshot;
  }

  public List<QueryDocumentSnapshot> getAllDocuments(@Nonnull String collection) throws InterruptedException, ExecutionException {
    if (firestore == null) {
      throw new IllegalStateException("Firestore not configured");
    }
    ApiFuture<QuerySnapshot> future = firestore.collection(collection).get();
    QuerySnapshot snapshot = future.get();
    log.info("Read all Firestore documents from collection {}", collection);
    return snapshot.getDocuments();
  }
}
