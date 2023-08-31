package com.boha.kasietransie.util;

import com.boha.kasietransie.services.DispatchService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zipper {
    private static final Logger logger = LoggerFactory.getLogger(Zipper.class);

    static Random random = new Random(System.currentTimeMillis());
    public static File getZippedFile(String json) throws IOException {
        long start = System.currentTimeMillis();

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setGroupingUsed(true);
        decimalFormat.setGroupingSize(3);

        logger.info(E.RED_DOT + E.RED_DOT + " Before zip: " + decimalFormat.format(json.length()) + " bytes in json");

        File dir = new File("zipDirectory");
        if (!dir.exists()) {
            boolean ok = dir.mkdir();
            logger.info( " Zip directory created: path: " + dir.getAbsolutePath() + " created: " + ok);
        }
        File zippedFile = new File(dir, DateTime.now().getMillis()
                +"_"+ random.nextInt(1000) + ".zip");
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zippedFile));
        ZipEntry e = new ZipEntry("bag");
        out.putNextEntry(e);

        byte[] data = json.getBytes();
        out.write(data, 0, data.length);
        out.closeEntry();

        out.close();
        long end = System.currentTimeMillis();
        long ms = (end - start);
        double elapsed = Double.parseDouble("" + ms) / Double.parseDouble("1000");

        logger.info(E.RED_DOT + E.RED_DOT + " After zip: "
                + decimalFormat.format(zippedFile.length()) + " bytes in file, elapsed: "
                + E.RED_APPLE + " " + elapsed + " seconds");
        return zippedFile;
    }

}
