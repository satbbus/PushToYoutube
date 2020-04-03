package com.sbandi.youtube;

/**
 * Sample Java code for youtube.videos.insert
 * See instructions for running these code samples locally:
 * https://developers.google.com/explorer-help/guides/code_samples#java
 */

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PushToYoutubeMain {
    private static final String CLIENT_SECRETS= "client_secret.json";
    private static final Collection<String> SCOPES =
            Arrays.asList("https://www.googleapis.com/auth/youtube.upload");

    private static final String CONFIG_FILE = "config.properties";

    private static final String APPLICATION_NAME = "Push To Youtube";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Create an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize(final NetHttpTransport httpTransport) throws IOException {
        // Load client secrets.
        System.out.println(CLIENT_SECRETS);
        InputStream in = PushToYoutubeMain.class.getClassLoader().getResourceAsStream(CLIENT_SECRETS);
        if(null != in) {
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                        .build();
        Credential credential =
                new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        return credential;}
        else {
            System.out.println("input steam reader is null");
            throw new IOException("input steam reader is null");
        }
    }

    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */
    public static YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = authorize(httpTransport);
        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Call function to create API service object. Define and
     * execute API request. Print API response.
     *
     * @throws GeneralSecurityException, IOException, GoogleJsonResponseException
     */
    public static void main(String[] args)
            throws GeneralSecurityException, IOException, GoogleJsonResponseException {
        YouTube youtubeService = getService();

        // Define the Video object, which will be uploaded as the request body.
        Video video = new Video();

        // Add the snippet object property to the Video object.
        VideoSnippet snippet = new VideoSnippet();
        snippet.setCategoryId("22");
        snippet.setDescription("Description of uploaded video.");
        snippet.setTitle("Test video upload.");
        video.setSnippet(snippet);

        // Add the status object property to the Video object.
        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus("private");
        video.setStatus(status);

        //  For this request to work, you must replace "YOUR_FILE"
        //       with a pointer to the actual file you are uploading.
        //       The maximum file size for this operation is 128GB.

        //FetchConfigProperties fetchConfigProperties = new FetchConfigProperties(CONFIG_FILE);
       // FileInputStream fis = (FileInputStream) PushToYoutubeMain.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
        Properties cnfProperties = new Properties();
        try {

            File jarPath=new File(PushToYoutubeMain.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            String propertiesPath=jarPath.getAbsolutePath();
            System.out.println(" propertiesPath-"+propertiesPath);
            cnfProperties.load(new FileInputStream(propertiesPath+"/config.properties"));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
       // FileInputStream fis = new FileInputStream(CONFIG_FILE);
       // Properties cnfProperties = new Properties();
        //cnfProperties.load(fis);
        //fis.close();


        //String  dir = "C:\\Users\\312207\\Documents\\2020\\YT\\";
        String  dir = cnfProperties.getProperty("source_dir");

        List<String> fileList = Stream.of(new File(dir).listFiles()).filter(file -> !file.isDirectory())
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());

        File mediaFile;
        ListIterator<String> li = fileList.listIterator();
        while(li.hasNext()){
            mediaFile = new File(li.next());
            InputStreamContent mediaContent =
                new InputStreamContent("video/*",
                        new BufferedInputStream(new FileInputStream(mediaFile)));
        mediaContent.setLength(mediaFile.length());

        // Define and execute the API request
        YouTube.Videos.Insert request = youtubeService.videos()
                .insert("snippet,status", video, mediaContent);
        Video response = request.execute();
        }
    }
}

