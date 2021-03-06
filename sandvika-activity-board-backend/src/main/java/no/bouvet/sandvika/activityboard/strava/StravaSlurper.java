package no.bouvet.sandvika.activityboard.strava;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javastrava.api.v3.auth.TokenManager;
import javastrava.api.v3.auth.model.Token;
import javastrava.api.v3.auth.ref.AuthorisationScope;
import javastrava.api.v3.model.StravaActivity;
import javastrava.api.v3.rest.API;
import javastrava.api.v3.rest.ClubAPI;
import javastrava.api.v3.service.exception.BadRequestException;
import javastrava.api.v3.service.exception.UnauthorizedException;
import no.bouvet.sandvika.activityboard.domain.Activity;
import no.bouvet.sandvika.activityboard.domain.Athlete;
import no.bouvet.sandvika.activityboard.points.PointsCalculator;
import no.bouvet.sandvika.activityboard.repository.ActivityRepository;
import no.bouvet.sandvika.activityboard.repository.AthleteRepository;
import no.bouvet.sandvika.activityboard.utils.DateUtil;

@Component
public class StravaSlurper
{
    private static final int STRAVA_CLUB_ID = 259508;
    public static String USERNAME = "sondrewe@gmail.com";
    public static String PASSWORD = "passordForSandvika";
    public static Integer STRAVA_APPLICATION_ID = 3034;
    public static String STRAVA_CLIENT_SECRET = "506d1d0ed30af56b74a458a26419dd6ead8e910d";
    private static Logger log = LoggerFactory.getLogger(StravaSlurper.class);
    @Autowired
    ActivityRepository activityRepository;

    @Autowired
    AthleteRepository athleteRepository;

    @Scheduled(fixedRate = 1000 * 60 * 10)
    public void updateActivities()
    {
        log.info("Updating activities");
        ClubAPI api = getApi();
        List<Activity> activities = new ArrayList<>();
        Arrays.asList(api.listRecentClubActivities(STRAVA_CLUB_ID, 1, 200)).forEach(stravaActivity -> activities.add(createActivity(stravaActivity)));
        activityRepository.save(activities);
    }

    private Activity createActivity(StravaActivity stravaActivity)
    {
        Activity activity = new Activity();
        activity.setAthletefirstName(stravaActivity.getAthlete().getFirstname());
        activity.setAthleteLastName(stravaActivity.getAthlete().getLastname());
        activity.setType(stravaActivity.getType().getValue());
        activity.setId(stravaActivity.getId().intValue());
        activity.setName(stravaActivity.getName());
        if (stravaActivity.getCalories() != null)
        {
            activity.setCalories(stravaActivity.getCalories());
        }
        // Må vente på ny versjon som tar med SufferScore
        //if (stravaActivity.getSufferScore() != null)
        //{
        //    activity.setSufferScore(stravaActivity.getSufferScore());
        //}
        activity.setElapsedTimeInSeconds(stravaActivity.getElapsedTime());
        if (stravaActivity.getAchievementCount() != null)
        {
            activity.setAchievementCount(stravaActivity.getAchievementCount());
        }
        if (stravaActivity.getTotalElevationGain() != null)
        {
            activity.setTotalElevationGaininMeters(stravaActivity.getTotalElevationGain());
        }
        activity.setMovingTimeInSeconds(stravaActivity.getMovingTime());
        activity.setDistanceInMeters(stravaActivity.getDistance());
        activity.setStartDateLocal(DateUtil.getDateFromLocalDateTime(stravaActivity.getStartDateLocal()));
        activity.setPoints(PointsCalculator.getPointsForActivity(activity, getHandicapForActivity(activity)));
        activity.setHandicap(getHandicapForActivity(activity));
        log.info("Created activity: " + activity.toString());
        return activity;
    }

    private ClubAPI getApi()
    {
        return API.instance(ClubAPI.class, getValidToken());
    }

    private Token getValidToken(final AuthorisationScope... scopes)
    {
        Token token = TokenManager.instance().retrieveTokenWithExactScope(USERNAME, scopes);
        if (token == null)
        {
            try
            {
                token = StravaUtils.getStravaAccessToken(USERNAME, PASSWORD, scopes);
                TokenManager.instance().storeToken(token);
            } catch (BadRequestException | UnauthorizedException e)
            {
                return null;
            }
        }
        return token;
    }

    private double getHandicapForActivity(Activity activity)
    {
        Athlete athlete = athleteRepository.findByLastName(activity.getAthleteLastName());
        if (athlete == null || athlete.getHandicapList().isEmpty())
        {
            return 1;
        } else
        {
            return athlete.getHandicapForDate(activity.getStartDateLocal());
        }
    }


}



