package com.boha.kasietransie.services;

import com.boha.kasietransie.data.dto.Commuter;
import com.boha.kasietransie.data.dto.CommuterRequest;
import com.boha.kasietransie.data.dto.CommuterResponse;
import com.boha.kasietransie.data.repos.CommuterRepository;
import com.boha.kasietransie.data.repos.CommuterRequestRepository;
import com.boha.kasietransie.data.repos.CommuterResponseRepository;
import com.boha.kasietransie.util.E;
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
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class CommuterService {
    final CommuterRepository commuterRepository;
    final CommuterRequestRepository commuterRequestRepository;
    final CommuterResponseRepository commuterResponseRepository;
    final CloudStorageUploaderService cloudStorageUploaderService;
    private final MailService mailService;
    final MessagingService messagingService;


    public CommuterService(CommuterRepository commuterRepository, CommuterRequestRepository commuterRequestRepository, CommuterResponseRepository commuterResponseRepository, CloudStorageUploaderService cloudStorageUploaderService, MailService mailService, MessagingService messagingService) {
        this.commuterRepository = commuterRepository;
        this.commuterRequestRepository = commuterRequestRepository;
        this.commuterResponseRepository = commuterResponseRepository;
        this.cloudStorageUploaderService = cloudStorageUploaderService;
        this.mailService = mailService;
        this.messagingService = messagingService;
    }

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger logger = Logger.getLogger(CommuterService.class.getSimpleName());

    public Commuter createCommuter(Commuter commuter) throws Exception {
        logger.info("\uD83E\uDDE1\uD83E\uDDE1 create commuter : " + gson.toJson(commuter));
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        logger.info("\uD83E\uDDE1\uD83E\uDDE1 createRequest  .... ");
        String storedPassword = UUID.randomUUID().toString();
        boolean validEmail = false;
        try {
            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest();
            if (commuter.getCellphone() != null) {
                createRequest.setPhoneNumber(commuter.getCellphone());
            }
            if (commuter.getName() != null) {
                createRequest.setDisplayName(commuter.getName());
            }
            createRequest.setPassword(storedPassword);
            if (commuter.getEmail() == null) {
                String name = commuter.getName();
                String mName = name.replace(" ", "").toLowerCase(Locale.getDefault());
                String email = mName + System.currentTimeMillis() + "@kasietransie.com";
                commuter.setEmail(email);
                createRequest.setEmail(email);
                logger.info("\uD83E\uDDE1\uD83E\uDDE1 createUserAsync  .... email: " + email);

            } else {
                validEmail = true;
                createRequest.setEmail(commuter.getEmail());
            }

            ApiFuture<UserRecord> userRecordFuture = firebaseAuth.createUserAsync(createRequest);
            UserRecord userRecord = userRecordFuture.get();
            logger.info("\uD83E\uDDE1\uD83E\uDDE1 userRecord from Firebase : "
                    + userRecord.getEmail() + " uid: " + userRecord.getUid());
            if (userRecord.getUid() != null) {
                String uid = userRecord.getUid();
                commuter.setCommuterId(uid);
                createCommuterQRCode(commuter);
                commuterRepository.insert(commuter);
                logger.info(E.CROISSANT+E.CROISSANT+E.CROISSANT+
                        E.CROISSANT+" commuter should be in database");
                commuter.setPassword(storedPassword);
                //
                if (validEmail) {
                    String message = "Dear " + commuter.getName() +
                            "      ,\n\nYou have been registered with KasieTransie and the team is happy to send you the first time login password. '\n" +
                            "      \nPlease login on the web with your email and the attached password but use your cellphone number to sign in on the phone.\n" +
                            "      \n\nThank you for working with KasieTransie. \nWelcome aboard!!\nBest Regards,\nThe KasieTransie Team\ninfo@geomonitorapp.io\n\n";

                    logger.info("\uD83E\uDDE1\uD83E\uDDE1 sending email  .... ");
                    mailService.sendHtmlEmail(commuter.getEmail(), message, "Welcome to KasieTransie");
                }
                logger.info("\uD83E\uDDE1\uD83E\uDDE1 KasieTransie commuter created. "
                        + gson.toJson(commuter));
            } else {
                throw new Exception("userRecord.getUid() == null. We have a problem with Firebase, Jack!");
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            throw e;
        }

        return commuter;
    }

    public void createCommuterQRCode(Commuter commuter) {
        try {
            String barcodeText = gson.toJson(commuter);
            QRCodeWriter barcodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix =
                    barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, 800, 800);

            BufferedImage img = MatrixToImageWriter.toBufferedImage(bitMatrix);

            String reg = commuter.getCommuterId().replace(" ", "");

            File tmpDir = new File("qrcodes");
            if (!tmpDir.isDirectory()) {
                try {
                    Files.createDirectory(tmpDir.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Path path = Path.of("qrcodes/qrcode_" + reg
                    + "_" + System.currentTimeMillis() + ".png");

            String p = "qrcodes/qrcode_" + reg
                    + "_" + System.currentTimeMillis() + ".png";
            File file = new File(p);
            ImageIO.write(img, "png", file);
            logger.info(E.COFFEE + "File created and qrCode ready for uploading");
            String url = cloudStorageUploaderService.uploadFile(file.getName(), file);
            commuter.setQrCodeUrl(url);

            boolean delete = Files.deleteIfExists(path);
            logger.info(E.LEAF + E.LEAF + E.LEAF +
                    " QRCode generated, url: " + url + " for commuter: "
                    + E.RED_APPLE + " - temp file deleted: " + delete);
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Commuter addCommuter(Commuter commuter) throws Exception {
        Commuter comm = createCommuter(commuter);
        createCommuterQRCode(comm);
        return comm;
    }

    public CommuterRequest addCommuterRequest(CommuterRequest commuterRequest) {
        CommuterRequest c = commuterRequestRepository.insert(commuterRequest);
        messagingService.sendMessage(commuterRequest);
        return c;
    }

    public CommuterResponse addCommuterResponse(CommuterResponse commuterResponse) {
        return commuterResponseRepository.insert(commuterResponse);
    }

}
