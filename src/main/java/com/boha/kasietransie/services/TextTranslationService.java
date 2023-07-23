package com.boha.kasietransie.services;

// Imports the Google Cloud Translation library.

import com.boha.kasietransie.data.TranslationBag;
import com.boha.kasietransie.data.TranslationInput;
import com.boha.kasietransie.data.repos.TranslationBagRepository;
import com.boha.kasietransie.util.E;
import com.google.cloud.translate.v3.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


@Service
public class TextTranslationService {
    static final Logger LOGGER = LoggerFactory.getLogger(TextTranslationService.class);
    static final Gson G = new GsonBuilder().setPrettyPrinting().create();
    @Value("${projectId}")
    private String projectId;

    TranslationServiceClient translationServiceClient;
    final TranslationBagRepository translationBagRepository;
    final MongoTemplate mongoTemplate;

    public TextTranslationService(TranslationBagRepository translationBagRepository, MongoTemplate mongoTemplate) {
        this.translationBagRepository = translationBagRepository;
        this.mongoTemplate = mongoTemplate;
    }

    private void initialize() throws Exception {
        try {
            translationServiceClient = TranslationServiceClient.create();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String translateText(TranslationBag bag) throws Exception {

        initialize();

        LocationName parent = LocationName.of(projectId, "global");

        TranslateTextRequest request =
                TranslateTextRequest.newBuilder()
                        .setParent(parent.toString())
                        .setMimeType("text/plain")
                        .setTargetLanguageCode(bag.getTarget())
                        .addContents(bag.getStringToTranslate())
                        .build();

        TranslateTextResponse response = translationServiceClient.translateText(request);

        // Display the translation for each input text provided
        String translatedText = null;
        for (Translation translation : response.getTranslationsList()) {
            translatedText = translation.getTranslatedText();
        }

        return translatedText;

    }

    List<TranslationBag> allBags = new ArrayList<>();

    public String generateTranslations(boolean setBaseStrings) throws Exception {
        setLanguageCodes();
        if (setBaseStrings) {
            setStrings();
        }
        allBags.clear();
        DateTime start = DateTime.now();
        LOGGER.info(E.PINK + E.PINK + E.PINK + " Number of Languages: " + languageCodes.size());
        LOGGER.info(E.PINK + E.PINK + E.PINK + " Number of Strings: " + hashMap.size());
        int cnt = 0;
        for (String languageCode : languageCodes) {
            List<TranslationBag> translationBags = new ArrayList<>();

            for (Map.Entry<String, String> entry : hashMap.entrySet()) {
                TranslationBag bag = getBag(languageCode, entry.getValue(), entry.getKey());
                translationBags.add(bag);
            }

            for (TranslationBag bag : translationBags) {
                String text = translateText(bag);
                bag.setTranslatedText(text);
                cnt++;
                LOGGER.info("%s%sTranslationBag #%d %s%s".formatted(E.AMP, E.AMP,
                        cnt, E.RED_APPLE, G.toJson(bag)));
                try {
                    LOGGER.info(" ..... sleeping for 200 milliseconds ....");
                    Thread.sleep(200);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
            //
            allBags.addAll(translationBags);
            writeFile(translationBags,languageCode);
        }

        String dart = createDartFile(true);
        LOGGER.info(E.PINK + E.PINK + E.PINK + " Number of Translations done: " + cnt);

        DateTime end = DateTime.now();
        long ms = end.getMillis() - start.getMillis();
        double delta = Double.parseDouble(String.valueOf(ms)) / Double.parseDouble("1000");

        LOGGER.info(E.PINK + E.PINK + E.PINK + " Number of TranslationBags: " + bags.size() +
                " elapsed time: " + delta + " seconds");
        try {
            translationServiceClient.shutdownNow();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return E.LEAF+"Translated " + allBags.size()
                + " strings and saved them to mongo\n\n" + dart;
    }

    public String generateInputStrings(List<TranslationInput> input) throws Exception {
        try {
            hashMap.clear();
            for (TranslationInput ti : input) {
                hashMap.put(ti.getKey(),ti.getText());
            }
            String res = generateTranslations(false);
            String res1 = createDartFile(true);
            return res + "\n" + res1;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    private void writeFile(List<TranslationBag> newBags, String languageCode) {
        JSONObject object = new JSONObject();
        List<TranslationBag> oldBags = translationBagRepository.findByTarget(languageCode);
        LOGGER.info(" oldBags from db: " + oldBags.size());

        oldBags.addAll(newBags);

        for (TranslationBag bag : oldBags) {
            object.put(bag.getKey(), bag.getTranslatedText());

        }
        LOGGER.info(" oldBags after new translations: " + oldBags.size());

        String mJson = G.toJson(object);

        File tmpDir = new File("translations");
        if (!tmpDir.isDirectory()) {
            try {
                Files.createDirectory(tmpDir.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Path path
                = Paths.get("translations/" + languageCode + ".json");
        try {
            LOGGER.info(E.PINK + E.PINK+" writing translationBags to file: " + oldBags.size());
            Files.writeString(path, mJson,
                    StandardCharsets.UTF_8);

            LOGGER.info(E.PINK + E.PINK+" writing translationBags to db: " + newBags.size());
            translationBagRepository.saveAll(newBags);
            LOGGER.info(E.PINK + E.PINK + E.PINK + " Locale Translations saved for: " + languageCode);
        } catch (IOException ex) {
            LOGGER.error("Invalid Path");
        }


    }

    public String createDartFile(boolean fromDB) {
        //
        if (fromDB) {
            allBags = translationBagRepository.findAll();
        }

        LOGGER.info(E.RED_APPLE+ " Process translated strings for Dart: " + allBags.size());

        HashMap<String,TranslationBag> hash = new HashMap<>();
        for (TranslationBag bag : allBags) {
            hash.put(bag.getKey(),bag);
        }
        List<TranslationBag> mList = new ArrayList<>(hash.values().stream().toList());
        Collections.sort(mList);

        LOGGER.info(E.RED_APPLE+ " Process filtered strings: " + mList.size());

        StringBuilder sb = new StringBuilder();
        for (TranslationBag bag : mList) {
            String bagLine = "\t hashMap['" + bag.getKey() + "'] = '" + bag.getKey() + "';";
            sb.append(bagLine).append("\n");
        }

        LOGGER.info(E.RED_APPLE+E.RED_APPLE+E.RED_APPLE+ " RESULT for Dart: " + sb.toString());
        File tmpDir = new File("translations");
        if (!tmpDir.isDirectory()) {
            try {
                Files.createDirectory(tmpDir.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Path path
                = Paths.get("translations/myKeys.txt");
        try {
            Files.writeString(path, sb.toString(),
                    StandardCharsets.UTF_8);
            LOGGER.info(" writing translation keys to file: " + path.toString());
            LOGGER.info(E.PINK + E.PINK + E.PINK + " Translations saved for myKeys: ");
        } catch (IOException ex) {
            LOGGER.error("Invalid Path");
        }

        return sb.toString();
    }

    private TranslationBag getBag(String languageCode, String stringToTranslate, String key) {
        TranslationBag bag = new TranslationBag();
        bag.setStringToTranslate(stringToTranslate);
        bag.setSource("en");
        bag.setTarget(languageCode);
        bag.setFormat("text");
        bag.setCreated(DateTime.now().toDateTimeISO().toString());
        bag.setKey(key);
        return bag;
    }

    List<String> languageCodes = new ArrayList<>();
    HashMap<String, String> hashMap = new HashMap<>();
    List<TranslationBag> bags = new ArrayList<>();

    //DO NOT REMOVE!!!!
    private void addBigStrings() {

        String m1 = "Geo is a powerful tool to monitor the construction and maintenance of " +
                "infrastructure and facilities, especially in areas where youth unemployment is high " +
                "and corruption may be an issue. With Geo, they can track progress and quality in real-time, " +
                "ensure that projects are being completed on time and within budget, and " +
                "identify and address potential issues before they become major problems. \n" +
                "By leveraging Geo\s multimedia monitoring capabilities, the agency can also " +
                "improve transparency and accountability, which can help combat corruption and " +
                "build trust with the community. \nUltimately, Geo can help the agency maximize the impact " +
                "of its investments in infrastructure and other facilities, " +
                "while also creating opportunities for unemployed youth to participate in monitoring " +
                "and contributing to the development of their communities.";

        hashMap.put("infrastructure", m1);

        String m2 = "Given the prevalence of smartphones and digital media use among young people, " +
                "Geo could be a valuable tool for engaging and employing youth in community development projects. " +
                "Field worker positions could be specifically targeted towards youth who are comfortable " +
                "using smartphones and digital media, and who may have been previously unemployed. " +
                "Furthermore, involvement with Geo could provide youth with valuable job skills " +
                "and work experience, which could increase their employability in the future. \n" +
                "In addition, by engaging with youth and involving them in community development projects, " +
                "Geo could help to foster a sense of pride and ownership in their communities, " +
                "which could lead to increased engagement and participation in the future.";

        hashMap.put("youth", m2);

        String m3 = "Government agencies and officials can benefit from Geo in several ways. " +
                "First, Geo can help in the efficient and effective management of infrastructure projects " +
                "by providing real-time multimedia monitoring and reporting, which can help identify and " +
                "address any issues that may arise during the project. Second, Geo can help to combat corruption " +
                "by producing an objective and verifiable record of project progress and expenses, " +
                "which can help to prevent or detect fraudulent activities. \n" +
                "Third, Geo can provide valuable insights into the needs and priorities of communities, " +
                "which can help government agencies to better target their resources and efforts to where " +
                "they are needed most. Finally, by engaging and " +
                "empowering young people in the process of monitoring and reporting on infrastructure projects, " +
                "Geo can help to build a sense of ownership and responsibility among youth, while also providing them " +
                "with valuable skills and experience that can help to improve their employment prospects.";

        hashMap.put("govt", m3);

        String m4 = "Community groups can get involved with Geo by using the platform to " +
                "monitor and manage their community-based projects, such as community clean-up campaigns, " +
                "volunteer initiatives, or fundraising events. The platform can also be used to track the " +
                "progress of these projects and ensure that resources are being allocated effectively. " +
                "Additionally, community groups can use Geo to report incidents or issues in their community, " +
                "such as crime or environmental hazards, and track the response of relevant authorities. \n" +
                "This can help to improve community safety and promote greater collaboration between community members " +
                "and local government agencies. Finally, community groups can also use Geo to coordinate volunteer " +
                "efforts during emergencies, such as natural disasters, and ensure that resources are being distributed to those in need.";

        hashMap.put("community", m4);

    }

    private void setStrings() {
        hashMap.put("loading", "Loading Association data");
        hashMap.put("vehicleQRCode", "Vehicle QR Code");
        hashMap.put("carQRCode", "Vehicle QR Code");
        hashMap.put("qrCode", "QR Code");
        hashMap.put("timeElapsed", "Time Elapsed");
        hashMap.put("dataLoader", "Data Loader");
        hashMap.put("sendDispatch", "Send Dispatch");
        hashMap.put("dispatchError", "Error dispatching taxi. Please try again");
        hashMap.put("noRoutesFound", "No routes found nearby");
        hashMap.put("noLandmarks", "No Landmarks found nearby");
        hashMap.put("newPlace", "New Place");
        hashMap.put("enterPlace", "Enter the name of the place");
        hashMap.put("savePlace", "Save Place");
        hashMap.put("placeMaker", "Place Maker");
        hashMap.put("routeLandmarks", "Route Landmarks");
        hashMap.put("landmarkIsPart", "This Landmark is part of the route.");
        hashMap.put("newLandmark", "New Landmark");
        hashMap.put("saveLandmark","Save Landmark");
        hashMap.put("taxiRouteMapper","Taxi Route Mapper");
        hashMap.put("points","Points");
        hashMap.put("index","Index");
        hashMap.put("routeViewer","Route Viewer");
        hashMap.put("taxiRouteViewer","Taxi Route Viewer");
        hashMap.put("routeName","Route Name");
        hashMap.put("saveRoute","Save Route");
        hashMap.put("routeColour","Route Colour");
        hashMap.put("enterRouteName","Enter Route Name");
        hashMap.put("routePointsMapped","Route Points Mapped");
        hashMap.put("routeDetails","Route Details");
        hashMap.put("taxiRoutes","Taxi Routes");
        hashMap.put("routeMaps","Route Maps");
        hashMap.put("welcome","Welcome");
        hashMap.put("errorSignIn","Error with sign in");
        hashMap.put("noLocation","Location not available");
        hashMap.put("errorServer","Server not available");
        hashMap.put("errorNetwork","Network not available");
        hashMap.put("errorScanner","Problem with Scanner");

//        hashMap.put("waitingForGPS", "Waiting for GPS location ...");
//        hashMap.put("emailSignIn", "Email Sign In");
//        hashMap.put("phoneSignIn", "Cellphone Sign In");
//
//        hashMap.put("register", "Register Taxi Association");
//        hashMap.put("routeMaps", "Route Maps");
//
//        hashMap.put("signedIn", "You have been signed in. Welcome!");
//        hashMap.put("emailAuthFailed", "Authentication failed, please check your email and password");
//        hashMap.put("problem", "We may have a slight problem here. Let us solve it together!");
//        hashMap.put("emailAuth", "Email Authentication");
//        hashMap.put("emailAddress", "Email Address");
//        hashMap.put("selectRoute", "Select Route");
//        hashMap.put("password", "Password");
//        hashMap.put("enterEmail", "Enter your Email address");
//        hashMap.put("enterPassword", "Enter your password");
//        hashMap.put("sendCreds", "Send Sign In Credentials");
//        hashMap.put("enterCode", "Please put in the code that was sent to you in the SMS");
//        hashMap.put("serverUnreachable", "Server cannot be reached");
//        hashMap.put("duplicateAss", "Duplicate association name");
//        hashMap.put("unableToSignIn", "Unable to Sign in. Have you registered an association?");
//        hashMap.put("phoneAuth", "Phone Authentication");
//        hashMap.put("enterPhone", "Enter Phone Number");
//        hashMap.put("phoneNumber", "Phone Number");
//        hashMap.put("verify", "Verify Phone Number");
//        hashMap.put("sendCode", "Send Code");
//        hashMap.put("associations", "Associations");
//        hashMap.put("dashboard", "Dashboard");
//        hashMap.put("owner", "Owner");
//        hashMap.put("ownerUnknown", "Owner Unknown");
//        hashMap.put("arrivals", "Arrivals");
//        hashMap.put("departures", "Departures");
//        hashMap.put("heartbeats", "Heartbeats");
//        hashMap.put("dispatches", "Dispatches");
//        hashMap.put("vehicleQRCode'", "Vehicle QR Code'");
//        hashMap.put("associationVehicles", "Association Vehicles");
//        hashMap.put("selectVehicle", "Select the vehicle for the app");
//        hashMap.put("initializingResources", "Initializing data resources ...");
//        hashMap.put("thisMayTake", "This may take a few minutes or so ...");
//        hashMap.put("done", "Done, please proceed");
//        hashMap.put("hereWeCome", "KasieTransie, here we come!");
//        hashMap.put("selectAss", "Select the association for the taxi");
//        hashMap.put("signInWithPhone", "Sign in with your phone");
//        hashMap.put("selectSignInKind", "Please select the kind of sign in");
//        hashMap.put("signInWithEmail", "Sign in with your email address");
//        hashMap.put("registerAssoc", "Register Your Taxi Association");
//        hashMap.put("errorGettingData", "Error getting data");
//        hashMap.put("taxiMarshal", "Taxi Marshal");
//        hashMap.put("marshal", "Marshal");
//        hashMap.put("driver", "Driver");
//        hashMap.put("taxiDriver", "Taxi Driver");
//
//        hashMap.put("dispatchWithScan", "Dispatch with Scan");
//        hashMap.put("manualDispatch", "Manual Dispatch");
//        hashMap.put("vehicles", "Vehicles");
//        hashMap.put("routes", "Routes");
//        hashMap.put("landmarks", "Landmarks");
//        hashMap.put("dispatch", "Dispatch");
//        hashMap.put("pleaseSelectRoute", "Please select Route");
//        hashMap.put("scannerWaiting", "Scanner waiting for Route selection");
//        hashMap.put("Cancel", "Cancel");
//        hashMap.put("yes", "Yes");
//        hashMap.put("no", "No");
//        hashMap.put("working", "Working ... hang on a few seconds ...");
//        hashMap.put("passengers", "Passengers");
//        hashMap.put("thanks", "Thank You'");
//        hashMap.put("taxiRoutes", "Taxi Routes");
//        hashMap.put("routesMenu", "Routes Menu");
//
//        hashMap.put("addCity", "Add Place/Town/City");
//        hashMap.put("createNewPlace", "Create a new place that wil be used in your routes");
//        hashMap.put("addNewRoute", "Add New Route");
//        hashMap.put("createNewRoute", "Create a new route");
//        hashMap.put("calculateRouteDistance", "Calculate Route Distance");
//        hashMap.put("calculateDistancesBetween", "Calculate distances between landmarks in the route");
//
//        hashMap.put("refreshRouteData", "Refresh Route Data");
//        hashMap.put("fetch", "Fetch refreshed route data from the Mother Ship");
//        hashMap.put("viewRouteMap", "View Route Map");
//        hashMap.put("routeLandmarks", "Route Landmarks");
//        hashMap.put("sendRouteMessage", "Send Route Update Message");
//        hashMap.put("createOrUpdate", "Create or update the taxi Route'");
//        hashMap.put("selectStartEnd", "Tap below to select your start and end of the route");
//        hashMap.put("routeStart", "Route Start");
//        hashMap.put("routeEnd", "Route End");
//        hashMap.put("pleaseEnterRouteName", "Please enter name of the taxi Route");
//        hashMap.put("routeName", "Route Name");
//        hashMap.put("enterRouteName", "Enter Route Name");
//
//        hashMap.put("route`color", "Route Colour");
//        hashMap.put("saveRoute", "Save Route");
//        hashMap.put("routeDetails", "Route Details");
//        hashMap.put("routePointsMapped", "Route Points Mapped");
//        hashMap.put("routeEditor", "Route Editor");
//        hashMap.put("startOfRoute", "Start of Route");
//        hashMap.put("endOfRoute", "End of Route");

    }

    private void setLanguageCodes() {
        languageCodes.add("en");
        languageCodes.add("af");

        languageCodes.add("fr");
        languageCodes.add("es");

        languageCodes.add("pt");
        languageCodes.add("de");

        languageCodes.add("sn");
        languageCodes.add("yo");

        languageCodes.add("zu");
        languageCodes.add("ts");

        languageCodes.add("ig");
        languageCodes.add("st");

        languageCodes.add("sw");
        languageCodes.add("xh");

        languageCodes.add("zh");

    }
}
