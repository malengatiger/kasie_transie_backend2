package com.boha.kasietransie;

import com.boha.kasietransie.services.FirebaseService;
import com.boha.kasietransie.services.MongoService;
import com.boha.kasietransie.util.E;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Logger;

@SpringBootApplication
@CrossOrigin(origins = "*", allowedHeaders = "*")
@EnableMongoRepositories(basePackages = "com.boha.kasietransie.data.repos")
@EnableAsync
@Configuration
class KasieTransieBackendApplication implements ApplicationListener<ApplicationReadyEvent> {
    static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    static final Logger logger = Logger.getLogger(KasieTransieBackendApplication.class.getSimpleName());
    static final String MM = "\uD83D\uDD35\uD83D\uDC26\uD83D\uDD35\uD83D\uDC26\uD83D\uDD35\uD83D\uDC26 ";
    final MongoService mongoService;
    final FirebaseService firebaseService;

    @Value("${doIndex}")
    int doIndex;

    public KasieTransieBackendApplication(MongoService mongoService, FirebaseService firebaseService) {
        this.mongoService = mongoService;
        this.firebaseService = firebaseService;
    }


    public static void main(String[] args) {

        logger.info(MM + " ................ " +
                "KasieTransieBackendApplication starting ...");
        SpringApplication.run(KasieTransieBackendApplication.class, args);
        logger.info(MM + E.RED_APPLE + E.RED_APPLE + E.RED_APPLE
                + " KasieTransieBackendApplication started OK! " + E.YELLOW + E.YELLOW);

    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) throws RuntimeException {

        ApplicationContext applicationContext = event.getApplicationContext();
        RequestMappingHandlerMapping requestMappingHandlerMapping = applicationContext
                .getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping
                .getHandlerMethods();

        logger.info(E.PEAR + E.PEAR + E.PEAR + E.PEAR +
                " Total Endpoints: " + map.size() + "\n");

        try {
            firebaseService.initializeFirebase();
            if (doIndex > 0) {
                mongoService.initializeIndexes();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try (final DatagramSocket datagramSocket = new DatagramSocket()) {
            datagramSocket.connect(InetAddress.getByName("8.8.8.8"), 12345);
            var addr = datagramSocket.getLocalAddress().getHostAddress();
            logger.info(E.PEAR + E.PEAR + E.PEAR + E.PEAR
                    + " datagramSocket: Current IP address : " + addr);

        } catch (SocketException | UnknownHostException e) {
            //
        }

        InetAddress ip;
        try {
            ip = InetAddress.getLocalHost();
            logger.info(E.PEAR + E.PEAR + E.PEAR + E.PEAR
                    + " Current IP address : " + ip.getHostAddress());

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                logger.info(E.BLUE_DOT + " WebMvcConfigurer setting CORS mapping ...");
                registry.addMapping("/**").allowedOrigins("*");
            }
        };
    }
}
