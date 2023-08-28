package com.boha.kasietransie.filters;

import com.boha.kasietransie.data.dto.User;
import com.boha.kasietransie.data.repos.UserRepository;
import com.boha.kasietransie.util.CustomResponse;
import com.boha.kasietransie.util.E;
import com.google.api.core.ApiFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.database.annotations.NotNull;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

@Component
@Profile("prod")
public class MonitorAuthenticationFilter extends OncePerRequestFilter {
    private static final String xx = E.COFFEE + E.COFFEE + E.COFFEE;
    String mm = " :: " + E.AMP + E.AMP + E.AMP + E.AMP;


    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorAuthenticationFilter.class);
    private static final Gson G = new GsonBuilder().setPrettyPrinting().create();
    private final UserRepository userRepository;
    @Value("${spring.profiles.active}")
    private String profile;

    public MonitorAuthenticationFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest httpServletRequest,
                                    @NotNull HttpServletResponse httpServletResponse,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {

        String url = httpServletRequest.getRequestURL().toString();
        if (profile.equalsIgnoreCase("test")) {
            doFilter(httpServletRequest, httpServletResponse, filterChain);
            return;
        }
        //todp - remove
        if (exclude(httpServletRequest, httpServletResponse, filterChain, url)) return;

        String m = httpServletRequest.getHeader("Authorization");
        if (m == null) {
            sendError(httpServletResponse, "Authentication token missing");
            return;
        }
        String token = m.substring(7);
        try {
            ApiFuture<FirebaseToken> future = FirebaseAuth.getInstance().verifyIdTokenAsync(token, true);
            FirebaseToken mToken = future.get();

            if (mToken != null) {
                String userId = mToken.getUid();
                List<User> user = userRepository.findByUserId(userId);
                if (!user.isEmpty()) {
                    LOGGER.info("\uD83D\uDE21 \uD83D\uDE21 \uD83D\uDE21  user unknown, possible new registration");
                    doFilter(httpServletRequest, httpServletResponse, filterChain);
                }

            } else {
                LOGGER.info("\uD83D\uDE21 \uD83D\uDE21 \uD83D\uDE21 request has been forbidden, token invalid");
                sendError(httpServletResponse, "request has been forbidden, token invalid");
            }

        } catch (Exception e) {
            String msg = "an Exception happened: \uD83C\uDF4E " + e.getMessage();
            LOGGER.info(E.RED_DOT + E.RED_DOT + E.RED_DOT + E.RED_DOT + E.RED_DOT + E.RED_DOT + E.RED_DOT + " " + msg
                    + "\n failed url: " + httpServletRequest.getRequestURL().toString()+ E.RED_DOT);
            e.printStackTrace();
            sendError(httpServletResponse, e.getMessage());
        }

    }

    private boolean exclude(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain, String url) throws IOException, ServletException {
        if (url.contains("kasietransie")) {   //this is my local machine
            LOGGER.info(E.ANGRY + E.ANGRY + "kasietransie this request is not subject to authentication: "
                    + E.HAND2 + url);
            doFilter(httpServletRequest, httpServletResponse, filterChain);
            return true;
        }
        if (url.contains("uploadFile")) {   //this is my local machine
            LOGGER.info(E.ANGRY + E.ANGRY + "this request is not subject to authentication: "
                    + E.HAND2 + url);
            doFilter(httpServletRequest, httpServletResponse, filterChain);
            return true;
        }
        //allow getCountries
        if (httpServletRequest.getRequestURI().contains("getCountries")
                || httpServletRequest.getRequestURI().contains("addCountry")) {
            LOGGER.info(mm + " contextPath: " + httpServletRequest.getContextPath()
                    + E.AMP + " requestURI: " + httpServletRequest.getRequestURI());
            LOGGER.info(mm + " allowing addCountry and getCountries without authentication, is this OK?");

            doFilter(httpServletRequest, httpServletResponse, filterChain);
            return true;
        }
        //allow registerAssociation, downloadExampleUsersFile, downloadExampleVehiclesFile
        if (httpServletRequest.getRequestURI().contains("downloadExampleUsersFile")
                || httpServletRequest.getRequestURI().contains("registerAssociation")
                || httpServletRequest.getRequestURI().contains("downloadExampleVehiclesFile")) {
            LOGGER.info(mm + " contextPath: " + httpServletRequest.getContextPath()
                    + E.AMP + " requestURI: " + httpServletRequest.getRequestURI());
            LOGGER.info(mm + " allowing downloadExampleVehiclesFile and downloadExampleUsersFile without authentication, is this OK?");

            doFilter(httpServletRequest, httpServletResponse, filterChain);
            return true;
        }
        //allow api-docs
        if (httpServletRequest.getRequestURI().contains("api-docs")) {
            LOGGER.info(mm + " contextPath: " + httpServletRequest.getContextPath()
                    + E.AMP + " requestURI: " + httpServletRequest.getRequestURI() + "\n\n");
            LOGGER.info(mm + " allowing swagger openapi call");

            doFilter(httpServletRequest, httpServletResponse, filterChain);
            return true;
        }
        //allow localhost
        if (url.contains("localhost") || url.contains("192.168.86.242")) {

            LOGGER.info(mm + " allowing call from " + url + " " + E.HEART_GREEN + E.HEART_GREEN + E.HEART_GREEN + E.HEART_GREEN);

            doFilter(httpServletRequest, httpServletResponse, filterChain);
            return true;
        }

        return false;
    }

    private static void sendError(HttpServletResponse httpServletResponse, String message) throws IOException {
        CustomResponse er = new CustomResponse(403, message, DateTime.now().toDateTimeISO().toString());
        httpServletResponse.setStatus(403);
        httpServletResponse.getWriter().write(G.toJson(er));
    }

    private void doFilter(@NotNull HttpServletRequest httpServletRequest,
                          @NotNull HttpServletResponse httpServletResponse,
                          FilterChain filterChain) throws IOException, ServletException {

        try {
//            Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
//            while (headerNames.hasMoreElements()) {
//                String key = headerNames.nextElement();
//                String value = httpServletRequest.getHeader(key);
//                logger.info(E.RED_DOT + " header name: " + key + E.LEAF+" value: " + value);
//            }
            //LOGGER.info(".... handing off to filterChain ... query: " + httpServletRequest.getQueryString() );
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            //print(httpServletRequest);
            //LOGGER.info(".... back from filterChain ...");

        } catch (IOException | ServletException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        if (httpServletResponse.getStatus() != 200) {
            LOGGER.info(reds + " \n" + httpServletRequest.getRequestURI() + " \uD83D\uDD37 Status Code: "
                    + httpServletResponse.getStatus() + "  \uD83D\uDCA6\uD83D\uDCA6  buffer size: " +
                    httpServletResponse.getOutputStream().toString());
        } else {
            LOGGER.info("\uD83D\uDD37\uD83D\uDD37\uD83D\uDD37\uD83D\uDD37"
                    + httpServletRequest.getRequestURI() + " \uD83D\uDD37 Status Code: "
                    + httpServletResponse.getStatus() + "  \uD83D\uDD37 \uD83D\uDD37 \uD83D\uDD37 "
                    + httpServletResponse.getOutputStream().toString());
        }
    }

    static final String reds = E.RED_DOT + E.RED_DOT + E.RED_DOT + E.RED_DOT + E.RED_DOT + " Bad Response Status:";

    private void print(@NotNull HttpServletRequest httpServletRequest) {
        String url = httpServletRequest.getRequestURL().toString();
        LOGGER.info(E.ANGRY + E.ANGRY + E.ANGRY + E.BELL + "print this request: " + E.BELL + " " + url);

        LOGGER.info("\uD83D\uDE21 \uD83D\uDE21 \uD83D\uDE21 \uD83D\uDE21 request header parameters ...");
        Enumeration<String> params = httpServletRequest.getParameterNames();
        while (params.hasMoreElements()) {
            String m = params.nextElement();
            LOGGER.info("\uD83D\uDE21 \uD83D\uDE21 \uD83D\uDE21 parameterName: " + m);

        }
        LOGGER.info("\uD83D\uDE21 \uD83D\uDE21 headers ...\n");
        Enumeration<String> names = httpServletRequest.getHeaderNames();
        while (names.hasMoreElements()) {
            String m = names.nextElement();
            String v = httpServletRequest.getHeader(m);
            LOGGER.info("\uD83D\uDE21 \uD83D\uDE21 \uD83D\uDE21 headerName: "
                    + E.BLUE_BIRD + " " + m + " value: " + v);
        }
        LOGGER.info("\uD83D\uDC9A \uD83D\uDC9A \uD83D\uDC9A Authorization: "
                + httpServletRequest.getHeader("Authorization") + " \uD83D\uDC9A \uD83D\uDC9A");
    }

}

