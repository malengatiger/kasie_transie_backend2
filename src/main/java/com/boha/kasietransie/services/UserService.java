package com.boha.kasietransie.services;

import com.boha.kasietransie.data.dto.Association;
import com.boha.kasietransie.data.dto.User;
import com.boha.kasietransie.data.repos.AssociationRepository;
import com.boha.kasietransie.data.repos.UserRepository;
import com.boha.kasietransie.util.E;
import com.boha.kasietransie.util.FileToUsers;
import com.google.api.core.ApiFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final MailService mailService;
    private final AssociationRepository associationRepository;
    final CloudStorageUploaderService cloudStorageUploaderService;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger logger = Logger.getLogger(UserService.class.getSimpleName());
    private static final String MM = "\uD83D\uDD35\uD83D\uDC26\uD83D\uDD35\uD83D\uDC26\uD83D\uDD35\uD83D\uDC26 ";

    public UserService(UserRepository userRepository, MailService mailService, AssociationRepository associationRepository, CloudStorageUploaderService cloudStorageUploaderService) {
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.associationRepository = associationRepository;
        this.cloudStorageUploaderService = cloudStorageUploaderService;
        logger.info(MM + " UserService constructed ");

    }

    public void createUserQRCode(User user) throws Exception {
        try {
            String barcodeText = gson.toJson(user);
            QRCodeWriter barcodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix =
                    barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, 800, 800);

            BufferedImage img = MatrixToImageWriter.toBufferedImage(bitMatrix);

            String reg = user.getUserId().replace(" ", "");

            File file = CommuterService.getQRCodeFile(reg);
            ImageIO.write(img, "png", file);
            logger.info(E.COFFEE + "File created and qrCode ready for uploading");
            String url = cloudStorageUploaderService.uploadFile(file.getName(), file);
            user.setQrCodeUrl(url);

            boolean delete = Files.deleteIfExists(file.toPath());
            logger.info(E.LEAF + E.LEAF + E.LEAF +
                    " QRCode generated, url: " + url + " for user: " + gson.toJson(user)
                    + E.RED_APPLE + " - temp file deleted: " + delete);
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public User updateUser(User user) throws Exception {
        logger.info("\uD83E\uDDE1\uD83E\uDDE1 create user : " + gson.toJson(user));
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        logger.info("\uD83E\uDDE1\uD83E\uDDE1 createRequest  .... ");
        String storedPassword = user.getPassword();

        try {
            UserRecord.UpdateRequest updateRequest = new UserRecord.UpdateRequest(user.getUserId());
            updateRequest.setPhoneNumber(user.getCellphone());
            updateRequest.setDisplayName(user.getName());
            updateRequest.setPassword(user.getPassword());
            if (user.getEmail() == null) {
                String name = user.getName();
                String mName = name.replace(" ","").toLowerCase(Locale.getDefault());
                String email = mName+System.currentTimeMillis()+"@kasietransie.com";
                user.setEmail(email);
                updateRequest.setEmail(email);
                logger.info("\uD83E\uDDE1\uD83E\uDDE1 createUserAsync  .... email: " + email);

            } else {
                updateRequest.setEmail(user.getEmail());
            }

            ApiFuture<UserRecord> userRecordFuture = firebaseAuth.updateUserAsync(updateRequest);
            UserRecord userRecord = userRecordFuture.get();
            logger.info("\uD83E\uDDE1\uD83E\uDDE1 userRecord from Firebase : " + userRecord.getEmail());
            if (userRecord.getUid() != null) {
                String uid = userRecord.getUid();
                user.setUserId(uid);
                user.setPassword(null);
                userRepository.save(user);
                //
                user.setPassword(storedPassword);
                logger.info("\uD83E\uDDE1\uD83E\uDDE1 KasieTransie user updated. " + gson.toJson(user));
            } else {
                throw new Exception("userRecord.getUid() == null. We have a problem with Firebase, Jack!");
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            throw e;
        }

        return user;
    }

    public User createUser(User user) throws Exception {
        logger.info("\uD83E\uDDE1\uD83E\uDDE1 create user : " + gson.toJson(user));
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        logger.info("\uD83E\uDDE1\uD83E\uDDE1 createRequest  .... ");
        String storedPassword = user.getPassword();

        try {
            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest();
            createRequest.setPhoneNumber(user.getCellphone());
            createRequest.setDisplayName(user.getName());
            createRequest.setPassword(user.getPassword());
            if (user.getEmail() == null) {
                String name = user.getName();
                String mName = name.replace(" ","").toLowerCase(Locale.getDefault());
                String email = mName+System.currentTimeMillis()+"@kasietransie.com";
                user.setEmail(email);
                createRequest.setEmail(email);
                logger.info("\uD83E\uDDE1\uD83E\uDDE1 createUserAsync  .... email: " + email);

            } else {
                createRequest.setEmail(user.getEmail());
            }

            ApiFuture<UserRecord> userRecordFuture = firebaseAuth.createUserAsync(createRequest);
            UserRecord userRecord = userRecordFuture.get();
            logger.info("\uD83E\uDDE1\uD83E\uDDE1 userRecord from Firebase : " + userRecord.getEmail());
            if (userRecord.getUid() != null) {
                String uid = userRecord.getUid();
                user.setUserId(uid);
                createUserQRCode(user);
                user.setPassword(null);

                userRepository.insert(user);
                //
                user.setPassword(storedPassword);
                String message = "Dear " + user.getName() +
                        "      ,\n\nYou have been registered with KasieTransie and the team is happy to send you the first time login password. '\n" +
                        "      \nPlease login on the web with your email and the attached password but use your cellphone number to sign in on the phone.\n" +
                        "      \n\nThank you for working with GeoMonitor. \nWelcome aboard!!\nBest Regards,\nThe KasieTransie Team\ninfo@geomonitorapp.io\n\n";

                logger.info("\uD83E\uDDE1\uD83E\uDDE1 sending email  .... ");
                mailService.sendHtmlEmail(user.getEmail(), message, "Welcome to KasieTransie");
                logger.info("\uD83E\uDDE1\uD83E\uDDE1 KasieTransie user created. ");
            } else {
                throw new Exception("userRecord.getUid() == null. We have a problem with Firebase, Jack!");
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            throw e;
        }

        return user;
    }

    public List<User> importUsersFromJSON(File file, String associationId) throws Exception {
        List<Association> orgs = associationRepository.findByAssociationId(associationId);
        List<User> resultUsers = new ArrayList<>();
        if (!orgs.isEmpty()) {
            List<User> users = FileToUsers.getUsersFromJSONFile(file);
            for (User user : users) {
                user.setAssociationId(associationId);
                user.setAssociationName(orgs.get(0).getAssociationName());
                User u = createUser(user);
                resultUsers.add(u);
            }

        }
        logger.info("Users imported from file: " + resultUsers.size());
        return resultUsers;
    }

    public List<User> importUsersFromCSV(File file, String associationId) throws Exception {
        List<Association> orgs = associationRepository.findByAssociationId(associationId);
        List<User> resultUsers = new ArrayList<>();
        if (!orgs.isEmpty()) {
            List<User> users = FileToUsers.getUsersFromCSVFile(file);
            for (User user : users) {
                user.setAssociationId(associationId);
                user.setAssociationName(orgs.get(0).getAssociationName());
                User u = createUser(user);
                resultUsers.add(u);
            }
        }
        logger.info("Users imported from file: " + resultUsers.size());
        return resultUsers;
    }

    public User getUserById(String userId) {
        List<User> list = userRepository.findByUserId(userId);
        if (list.isEmpty()) {
            throw new NoSuchElementException();
        }
        return list.get(0);
    }

    public User getUserByEmail(String email) {
        try {
            List<User> list = userRepository.findByEmail(email);
            logger.info(E.RAIN+E.RAIN+" getUserByEmail found " + list.size()+ " users");
            if (list.isEmpty()) {
                logger.severe(E.RED_DOT+"User not found by email");
                throw new NoSuchElementException();
            }
            return list.get(0);
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public List<User> getAssociationUsers(String associationId) {
        return userRepository.findByAssociationId(associationId);
    }
}
