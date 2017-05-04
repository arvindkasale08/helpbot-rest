package com.appdirect.jira.service;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.appdirect.jira.props.OAuth;
import com.appdirect.jira.vo.Meeting;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

/**
 * Created by abidkhan on 04/05/17.
 */
@Service
@Slf4j
public class GoogleCalendarService {
    @Autowired
    private OAuth oAuth;
    @Autowired
    @Qualifier("googleNetHttpTransport")
    HttpTransport httpTransport;
    @Autowired
    @Qualifier("jsonFactory")
    JsonFactory jsonFactory;


    public List<Meeting> findMeetings(final Credential credential){
        List<Meeting> meetings = new ArrayList<Meeting>();
        try{
            Calendar calendarService = getCalendarService(credential);
            DateTime now = new DateTime(System.currentTimeMillis());
            Events events = calendarService.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            if (events.isEmpty()) {
                log.info("No meeting present");
                return meetings;
            }

            log.info("Building meeting detail");
            for (Event event : events.getItems()) {
                log.info("Meeting summary {}", event.getSummary());
                meetings.add(Meeting.builder().summary(event.getSummary()).startTime(event.getStart().getDateTime().getValue()).build());
            }
            log.info("Meetings count {}", meetings.size());
        }catch (Exception  ex){
            log.error("Failed to get meeting for credential {} due to {}",credential.toString(), ex);
        }

        return meetings;
    }
    /**
     * @param credential
     * @return
     */
    private com.google.api.services.calendar.Calendar getCalendarService(final Credential credential) {
        return new com.google.api.services.calendar.Calendar.Builder(
                httpTransport, jsonFactory, credential)
                .setApplicationName(oAuth.getGoogleAppName())
                .build();
    }
}
