package com.boha.kasietransie.services;

import com.boha.kasietransie.util.E;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Initializes Firebase
 */
@RequiredArgsConstructor
@Service
public class FirebaseService {

    private static final Logger LOGGER = Logger.getLogger(FirebaseService.class.getSimpleName());

    @Value("${storageBucket}")
    private String storageBucket;
    private FirebaseApp app;
    @Value("${projectId}")
    private String projectId;


    public void initializeFirebase() throws Exception {
        LOGGER.info(E.AMP+E.AMP+E.AMP+ " .... initializing Firebase ....");
        FirebaseOptions options;
        LOGGER.info(E.AMP+E.AMP+E.AMP+
                " Project Id from Properties: "+E.RED_APPLE + " " + projectId);
        try {
            options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.getApplicationDefault())
                    .setDatabaseUrl("https://" + projectId + ".firebaseio.com/")
                    .setStorageBucket(storageBucket)
                    .build();
        } catch (IOException e) {
            throw new Exception("Firebase initialization failed!  " + e.getMessage());
        }

        app = FirebaseApp.initializeApp(options);
        LOGGER.info(E.AMP+E.AMP+E.AMP+E.AMP+E.AMP+
                " Firebase has been initialized: "
                + app.getOptions().getDatabaseUrl()
                + " " + E.RED_APPLE);
    }

}
