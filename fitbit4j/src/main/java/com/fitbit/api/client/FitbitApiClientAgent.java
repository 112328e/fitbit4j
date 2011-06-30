package com.fitbit.api.client;

import com.fitbit.api.APIUtil;
import com.fitbit.api.FitbitAPIException;
import com.fitbit.api.client.http.*;
import com.fitbit.api.common.model.activities.Activities;
import com.fitbit.api.common.model.activities.Activity;
import com.fitbit.api.common.model.activities.ActivityReference;
import com.fitbit.api.common.model.activities.LoggedActivityReference;
import com.fitbit.api.common.model.body.Body;
import com.fitbit.api.common.model.devices.Device;
import com.fitbit.api.common.model.devices.DeviceType;
import com.fitbit.api.common.model.foods.*;
import com.fitbit.api.common.model.sleep.Sleep;
import com.fitbit.api.common.model.sleep.SleepLog;
import com.fitbit.api.common.model.timeseries.*;
import com.fitbit.api.common.model.units.VolumeUnits;
import com.fitbit.api.common.model.user.Account;
import com.fitbit.api.common.model.user.UserInfo;
import com.fitbit.api.common.service.FitbitApiService;
import com.fitbit.api.model.*;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;


@SuppressWarnings({"NonPrivateFieldAccessedInSynchronizedContext"})
public class FitbitApiClientAgent extends FitbitAPIClientSupport implements Serializable {
    private static final FitbitApiCredentialsCache DEFAULT_CREDENTIALS_CACHE = new FitbitApiCredentialsCacheMapImpl();

    private static final String DEFAULT_API_BASE_URL = "api.fitbit.com";
    private static final String DEFAULT_WEB_BASE_URL = "http://www.fitbit.com";
    private static final long serialVersionUID = -1486360080128882436L;
    protected static final String SUBSCRIBER_ID_HEADER_NAME = "X-Fitbit-Subscriber-Id";

    private SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
    private String apiBaseUrl = DEFAULT_API_BASE_URL;
    private APIVersion apiVersion = APIVersion.BETA_1;

    private FitbitApiCredentialsCache credentialsCache;


    /**
     * Default constructor. Creates FitbitApiClientAgent with default API hosts and credentials cache.
     */
    public FitbitApiClientAgent() {
        this(DEFAULT_API_BASE_URL, DEFAULT_WEB_BASE_URL, (FitbitApiCredentialsCache) null);
    }

    /**
     * Creates FitbitApiClientAgent with custom API hosts and credentials cache.
     *
     * @param apiBaseUrl e.g. api.fitbit.com
     * @param webBaseUrl e.g. http://www.fitbit.com
     * @param credentialsCache Credentials cache
     *
     * @see <a href="http://wiki.fitbit.com/display/API/API-Client-Reference-App">Fitbit API: API-Client-Reference-App</a>
     */
    public FitbitApiClientAgent(String apiBaseUrl, String webBaseUrl, FitbitApiCredentialsCache credentialsCache) {
        this("https://" + apiBaseUrl + "/oauth/request_token", webBaseUrl + "/oauth/authorize", "https://" + apiBaseUrl + "/oauth/access_token");
        this.apiBaseUrl = apiBaseUrl;
        if (null == credentialsCache) {
            this.credentialsCache = DEFAULT_CREDENTIALS_CACHE;
        } else {
            this.credentialsCache = credentialsCache;
        }
    }

    /**
     * @param requestTokenURL e.g. https://api.fitbit.com/oauth/request_token
     * @param authorizationURL e.g. http://www.fitbit.com/oauth/authorize
     * @param accessTokenURL https://api.fitbit.com/oauth/access_token
     *
     * @see <a href="http://wiki.fitbit.com/display/API/API-Client-Reference-App">Fitbit API: API-Client-Reference-App</a>
     */
    public FitbitApiClientAgent(String requestTokenURL, String authorizationURL, String accessTokenURL) {
        super();
        init(requestTokenURL, authorizationURL, accessTokenURL);
    }

    private void init(String requestTokenURL, String authorizationURL, String accessTokenURL) {
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        http.setRequestTokenURL(requestTokenURL);
        http.setAuthorizationURL(authorizationURL);
        http.setAccessTokenURL(accessTokenURL);
    }

    /**
     * Returns the base API URL
     *
     * @return the base API URL
     */
    public String getApiBaseUrl() {
        return "http://" + apiBaseUrl;
    }

    /**
     * Returns the base API SSL URL
     *
     * @return the secured base API URL
     */
    public String getApiBaseSecuredUrl() {
        return "https://" + apiBaseUrl;
    }

    /**
     * Returns currently used API version
     *
     * @return API version
     */
    public APIVersion getApiVersion() {
        return apiVersion;
    }

    /**
     * Retrieves a request token
     *
     * @return retrieved request token@since Fitbit 1.0
     *
     * @throws FitbitAPIException when Fitbit API service or network is unavailable
     * @see <a href="http://wiki.fitbit.com/display/API/OAuth-Authentication-API">Fitbit API: OAuth-Authentication-API<</a>
     * @see <a href="http://oauth.net/core/1.0/#auth_step1">OAuth Core 1.0 - 6.1.  Obtaining an Unauthorized Request Token</a>
     */
    public TempCredentials getOAuthTempToken() throws FitbitAPIException {
        return http.getOAuthRequestToken();
    }

    /**
     * Retrieves a request token, providing custom callback url
     *
     * @return retrieved request token@since Fitbit 1.0
     *
     * @throws FitbitAPIException when Fitbit API service or network is unavailable
     * @see <a href="http://wiki.fitbit.com/display/API/OAuth-Authentication-API">Fitbit API: OAuth-Authentication-API</a>
     * @see <a href="http://oauth.net/core/1.0/#auth_step1">OAuth Core 1.0 - 6.1.  Obtaining an Unauthorized Request Token</a>
     */
    public TempCredentials getOAuthTempToken(String callback_url) throws FitbitAPIException {
        return http.getOauthRequestToken(callback_url);
    }

    /**
     * Retrieves an access token associated with the supplied request token.
     *
     * @param tempToken the request token
     *
     * @return access token associated with the supplied request token.
     *
     * @throws FitbitAPIException when Fitbit service or network is unavailable, or the user has not authorized
     * @see <a href="http://wiki.fitbit.com/OAuth-Authentication-API">Fitbit API: OAuth-Authentication-API<</a>
     * @see <a href="http://oauth.net/core/1.0/#auth_step2">OAuth Core 1.0 - 6.2.  Obtaining User Authorization</a>
     */
    public synchronized AccessToken getOAuthAccessToken(TempCredentials tempToken) throws FitbitAPIException {
        return http.getOAuthAccessToken(tempToken);
    }

    /**
     * Retrieves an access token associated with the supplied request token and retrieved pin, sets userId.
     *
     * @param tempToken the request token
     * @param pin pin
     *
     * @return access token associsted with the supplied request token.
     *
     * @throws FitbitAPIException when Fitbit service or network is unavailable, or the user has not authorized
     * @see <a href="http://wiki.fitbit.com/OAuth-Authenticaion-API">Fitbit API: OAuth-Authentication-API</a>
     * @see <a href="http://oauth.net/core/1.0/#auth_step2">OAuth Core 1.0 - 6.2.  Obtaining User Authorization</a>
     */
    public synchronized AccessToken getOAuthAccessToken(TempCredentials tempToken, String pin) throws FitbitAPIException {
        AccessToken accessToken = http.getOAuthAccessToken(tempToken, pin);
        setUserId(accessToken.getEncodedUserId());
        return accessToken;
    }

    /**
     * Retrieves an access token associated with the supplied request token, retrieved tokenSecret and oauth_verifier or pin
     *
     * @param token request token
     * @param tokenSecret request token secret
     * @param oauth_verifier oauth_verifier or pin
     *
     * @return access token associsted with the supplied request token.
     *
     * @throws FitbitAPIException when Fitbit service or network is unavailable, or the user has not authorized
     * @see <a href="http://wiki.fitbit.com/OAuth-Authenticaion-API">Fitbit API: OAuth-Authentication-API</a>
     * @see <a href="http://oauth.net/core/1.0/#auth_step2">OAuth Core 1.0 - 6.2.  Obtaining User Authorization</a>
     */
    public synchronized AccessToken getOAuthAccessToken(String token, String tokenSecret, String oauth_verifier) throws FitbitAPIException {
        return http.getOAuthAccessToken(token, tokenSecret, oauth_verifier);
    }

    /**
     * Sets the access token
     *
     * @param accessToken access token
     *
     * @see <a href="http://wiki.fitbit.com/OAuth-Authenticaion-API">Fitbit API: OAuth-Authentication-API</a>
     * @see <a href="http://oauth.net/core/1.0/#auth_step2">OAuth Core 1.0 - 6.2.  Obtaining User Authorization</a>
     */
    public void setOAuthAccessToken(AccessToken accessToken) {
        http.setOAuthAccessToken(accessToken);
    }

    /**
     * Sets the access token and secret
     *
     * @param token access token
     * @param tokenSecret access token secret
     */
    public void setOAuthAccessToken(String token, String tokenSecret) {
        setOAuthAccessToken(new AccessToken(token, tokenSecret));
    }

    /**
     * Sets the access token and secret
     *
     * @param token access token
     * @param tokenSecret access token secret
     * @param encodedUserId userId
     */
    public void setOAuthAccessToken(String token, String tokenSecret, String encodedUserId) {
        setOAuthAccessToken(new AccessToken(token, tokenSecret));
    }

    /**
     * Sets the OAuth consumer credentials
     *
     * @param consumerKey consumer key
     * @param consumerKey consumer secret
     */
    public synchronized void setOAuthConsumer(String consumerKey, String consumerSecret) {
        http.setOAuthConsumer(consumerKey, consumerSecret);
    }

    /**
     * Sets id of a default subscriber for subscription requests
     *
     * @param subscriberId default subscriber id
     *
     * @see <a href="http://wiki.fitbit.com/display/API/Subscriptions-API#Subscriptions-API-Configureyouraccountwithasubscriberendpoint">Fitbit API: Subscriptions-API</a>
     */
    protected void setSubscriberId(String subscriberId) {
        if (null != subscriberId) {
            http.setRequestHeader(SUBSCRIBER_ID_HEADER_NAME, subscriberId);
        }
    }

    /**
     * Retrieves credentials cache
     *
     * @return credentials cache
     */
    public FitbitApiCredentialsCache getCredentialsCache() {
        return credentialsCache;
    }

    /**
     * Get a summary and list of a user's activities and activity log entries for a given day
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     * @param date date to retrieve data dor
     *
     * @return activities for a given day
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Activities">Fitbit API: API-Get-Activities</a>
     */
    public Activities getActivities(LocalUserDetail localUser, FitbitUser fitbitUser, LocalDate date) throws FitbitAPIException {
        // Example: GET /1/user/228TQ4/activities/date/2010-02-25.json
        Response res = getCollectionResponseForDate(localUser, fitbitUser, APICollectionType.activities, date);
        throwExceptionIfError(res);
        return Activities.constructActivities(res);
    }

    /**
     * Get a list of a user's favorite activities. The activity id contained in the record retrieved can be used to log the activity
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     *
     * @return list of user's favorite activities
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Favorite-Activities">Fitbit API: API-Get-Favorite-Activities</a>
     */
    public List<ActivityReference> getFavoriteActivities(LocalUserDetail localUser, FitbitUser fitbitUser) throws FitbitAPIException {
        // Example: GET /1/user/228TQ4/activities/favorite.json
        Response res = getCollectionResponseForProperty(localUser, fitbitUser, APICollectionType.activities, ApiCollectionProperty.favorite);
        throwExceptionIfError(res);
        return ActivityReference.constructActivityReferenceList(res);
    }

    /**
     * Get a list of a user's recent activities. The activity id contained in the record retrieved can be used to log the activity
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     *
     * @return list of user's recent activities
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Recent-Activities">Fitbit API: API-Get-Recent-Activities</a>
     */
    public List<LoggedActivityReference> getRecentActivities(LocalUserDetail localUser, FitbitUser fitbitUser) throws FitbitAPIException {
        // Example: GET /1/user/228TQ4/activities/recent.json
        Response res = getCollectionResponseForProperty(localUser, fitbitUser, APICollectionType.activities, ApiCollectionProperty.recent);
        throwExceptionIfError(res);
        return LoggedActivityReference.constructLoggedActivityReferenceList(res);
    }

    /**
     * Get a list of a user's frequent activities. The activity id contained in the record retrieved can be used to log the activity
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     *
     * @return list of user's frequent activities
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Frequent-Activities">Fitbit API: API-Get-Frequent-Activities</a>
     */
    public List<LoggedActivityReference> getFrequentActivities(LocalUserDetail localUser, FitbitUser fitbitUser) throws FitbitAPIException {
        // Example: GET /1/user/228TQ4/activities/recent.json
        Response res = getCollectionResponseForProperty(localUser, fitbitUser, APICollectionType.activities, ApiCollectionProperty.frequent);
        throwExceptionIfError(res);
        return LoggedActivityReference.constructLoggedActivityReferenceList(res);
    }

    /**
     * Create log entry for an activity
     *
     * @param localUser authorized user
     * @param activityId Activity id
     * @param steps Start time
     * @param durationMillis Duration
     * @param distance Distance
     * @param date Log entry date
     * @param startTime Start time
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Activity">Fitbit API: API-Log-Activity</a>
     */
    public void logActivity(LocalUserDetail localUser,
                            long activityId,
                            int steps,
                            int durationMillis,
                            float distance,
                            LocalDate date,
                            LocalDate startTime) throws FitbitAPIException {

        List<PostParameter> params = new ArrayList<PostParameter>(5);
        params.add(new PostParameter("activityId", activityId));
        params.add(new PostParameter("steps", steps));
        params.add(new PostParameter("durationMillis", durationMillis));
        params.add(new PostParameter("distance", distance));
        params.add(new PostParameter("date", DateTimeFormat.forPattern("yyyy-MM-dd").print(date)));
        params.add(new PostParameter("startTime", FitbitApiService.LOCAL_TIME_HOURS_MINUTES_FORMATTER.print(startTime)));

        logActivity(localUser, params);
    }

    /**
     * Create log entry for an activity
     *
     * @param localUser authorized user
     * @param params POST request parameters
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Activity">Fitbit API: API-Log-Activity</a>
     */
    public void logActivity(LocalUserDetail localUser, List<PostParameter> params) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: POST /1/user/-/activities.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/activities", APIFormat.JSON);

        Response res;
        try {
            res = httpPost(url, params.toArray(new PostParameter[params.size()]), true);
        } catch (Exception e) {
            throw new FitbitAPIException("Error creating activity: " + e, e);
        }

        if (res.getStatusCode() != HttpServletResponse.SC_CREATED) {
            throw new FitbitAPIException("Error creating activity: " + res.getStatusCode());
        }
    }

    /**
     * Delete user's activity log entry with the given id
     *
     * @param localUser authorized user
     * @param activityLogId Activity log entry id
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Delete-Activity-Log">Fitbit API: API-Delete-Activity-Log</a>
     */
    public void deleteActivityLog(LocalUserDetail localUser, String activityLogId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: DELETE /1/user/-/activities/123.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/activities/" + activityLogId, APIFormat.JSON);
        try {
            httpDelete(url, true);
        } catch (Exception e) {
            throw new FitbitAPIException("Error deleting activity log entry: " + e, e);
        }
    }

    /**
     * Get the details of a specific activity in Fitbit activities database. If activity has levels, also get list of activity level details.
     *
     * @param localUser authorized user
     * @param activityId Activity id
     *
     * @return activity description
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Activity">Fitbit API: API-Get-Activity</a>
     */
    public Activity getActivity(LocalUserDetail localUser, long activityId) throws FitbitAPIException {
        return getActivity(localUser, String.valueOf(activityId));
    }

    /**
     * Get the details of a specific activity in Fitbit activities database. If activity has levels, also get list of activity level details.
     *
     * @param localUser authorized user
     * @param activityId Activity id
     *
     * @return activity description
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Activity">Fitbit API: API-Get-Activity</a>
     */
    public Activity getActivity(LocalUserDetail localUser, String activityId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/activities/90009.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/activities/" + activityId, APIFormat.JSON);
        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return Activity.constructActivity(res.asJSONObject());
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving activity: " + e, e);
        }
    }

    /**
     * Adds the activity with the given id to user's list of favorite activities.
     *
     * @param localUser authorized user
     * @param activityId Activity id
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Add-Favorite-Activity">Fitbit API: API-Add-Favorite-Activity</a>
     */
    public void addFavoriteActivity(LocalUserDetail localUser, String activityId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: POST /1/user/-/activities/log/favorite/123.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/activities/log/favorite/" + activityId, APIFormat.JSON);
        try {
            httpPost(url, null, true);
        } catch (Exception e) {
            throw new FitbitAPIException("Error adding favorite activity: " + e, e);
        }
    }

    /**
     * Delete the activity with the given id from user's list of favorite activities.
     *
     * @param localUser authorized user
     * @param activityId Activity id
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Delete-Favorite-Activity">Fitbit API: API-Delete-Favorite-Activity</a>
     */
    public void deleteFavoriteActivity(LocalUserDetail localUser, String activityId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: DELETE /1/user/-/activities/log/favorite/123.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/activities/log/favorite/" + activityId, APIFormat.JSON);
        try {
            httpDelete(url, true);
        } catch (Exception e) {
            throw new FitbitAPIException("Error deleting favorite activity: " + e, e);
        }

    }

    /**
     * Create new private food for a user
     *
     * @param localUser authorized user
     * @param name Food name
     * @param description Food description
     * @param defaultFoodMeasurementUnitId Default measurement unit for a food
     * @param defaultServingSize Default size of a serving
     * @param caloriesPerServingSize Calories in default serving
     * @param formType Form type
     *
     * @return new food object
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Create-Food">Fitbit API: API-Create-Food</a>
     */
    public Food createFood(LocalUserDetail localUser, String name, String description, long defaultFoodMeasurementUnitId,
                           float defaultServingSize, int caloriesPerServingSize, FoodFormType formType) throws FitbitAPIException {
        NutritionalValuesEntry nutritionalValuesEntry = new NutritionalValuesEntry();
        nutritionalValuesEntry.setCalories(caloriesPerServingSize);
        return createFood(localUser, name, description, defaultFoodMeasurementUnitId, defaultServingSize, formType, nutritionalValuesEntry);
    }

    /**
     * Create new private food for a user
     *
     * @param localUser authorized user
     * @param name Food name
     * @param description Food description
     * @param defaultFoodMeasurementUnitId Default measurement unit for a food
     * @param defaultServingSize Default size of a serving
     * @param formType Form type
     * @param nutritionalValuesEntry Set of nutritional values for a default serving
     *
     * @return new food object
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Create-Food">Fitbit API: API-Create-Food</a>
     */
    public Food createFood(LocalUserDetail localUser, String name, String description, long defaultFoodMeasurementUnitId,
                           float defaultServingSize, FoodFormType formType,
                           NutritionalValuesEntry nutritionalValuesEntry) throws FitbitAPIException {
        setAccessToken(localUser);
        List<PostParameter> params = new ArrayList<PostParameter>();
        params.add(new PostParameter("name", name));
        params.add(new PostParameter("description", description));
        params.add(new PostParameter("defaultFoodMeasurementUnitId", defaultFoodMeasurementUnitId));
        params.add(new PostParameter("defaultServingSize", defaultServingSize));
        params.add(new PostParameter("formType", formType.toString()));

        params.add(new PostParameter("calories", nutritionalValuesEntry.getCalories()));
        params.add(new PostParameter("caloriesFromFat", nutritionalValuesEntry.getCaloriesFromFat()));
        params.add(new PostParameter("totalFat", nutritionalValuesEntry.getTotalFat()));
        params.add(new PostParameter("transFat", nutritionalValuesEntry.getTransFat()));
        params.add(new PostParameter("saturatedFat", nutritionalValuesEntry.getSaturatedFat()));
        params.add(new PostParameter("cholesterol", nutritionalValuesEntry.getCholesterol()));
        params.add(new PostParameter("sodium", nutritionalValuesEntry.getSodium()));
        params.add(new PostParameter("potassium", nutritionalValuesEntry.getPotassium()));
        params.add(new PostParameter("totalCarbohydrate", nutritionalValuesEntry.getTotalCarbohydrate()));
        params.add(new PostParameter("dietaryFiber", nutritionalValuesEntry.getDietaryFiber()));
        params.add(new PostParameter("sugars", nutritionalValuesEntry.getSugars()));
        params.add(new PostParameter("protein", nutritionalValuesEntry.getProtein()));
        params.add(new PostParameter("vitaminA", nutritionalValuesEntry.getVitaminA()));
        params.add(new PostParameter("vitaminC", nutritionalValuesEntry.getVitaminC()));
        params.add(new PostParameter("iron", nutritionalValuesEntry.getIron()));
        params.add(new PostParameter("calcium", nutritionalValuesEntry.getCalcium()));
        params.add(new PostParameter("thiamin", nutritionalValuesEntry.getThiamin()));
        params.add(new PostParameter("riboflavin", nutritionalValuesEntry.getRiboflavin()));
        params.add(new PostParameter("vitaminB6", nutritionalValuesEntry.getVitaminB6()));
        params.add(new PostParameter("vitaminB12", nutritionalValuesEntry.getVitaminB12()));
        params.add(new PostParameter("vitaminE", nutritionalValuesEntry.getVitaminE()));
        params.add(new PostParameter("folicAcid", nutritionalValuesEntry.getFolicAcid()));
        params.add(new PostParameter("niacin", nutritionalValuesEntry.getNiacin()));
        params.add(new PostParameter("magnesium", nutritionalValuesEntry.getMagnesium()));
        params.add(new PostParameter("phosphorus", nutritionalValuesEntry.getPhosphorus()));
        params.add(new PostParameter("iodine", nutritionalValuesEntry.getIodine()));
        params.add(new PostParameter("zinc", nutritionalValuesEntry.getZinc()));
        params.add(new PostParameter("copper", nutritionalValuesEntry.getCopper()));
        params.add(new PostParameter("biotin", nutritionalValuesEntry.getBiotin()));
        params.add(new PostParameter("pantothenicAcid", nutritionalValuesEntry.getPantothenicAcid()));
        params.add(new PostParameter("vitaminD", nutritionalValuesEntry.getVitaminD()));

        // Example: POST /1/food/create.json
        String url = APIUtil.contextualizeUrl(getApiBaseSecuredUrl(), getApiVersion(), "/foods", APIFormat.JSON);

        Response response = httpPost(url, params.toArray(new PostParameter[params.size()]), true);

        try {
            return new Food(response.asJSONObject().getJSONObject("food"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error parsing json response to Food object: ", e);
        }
    }

    /**
     * Get a summary and list of a user's food log entries for a given day
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     * @param date date to retrieve data dor
     *
     * @return food records for a given day
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Foods">Fitbit API: API-Get-Foods</a>
     */
    public Foods getFoods(LocalUserDetail localUser, FitbitUser fitbitUser, LocalDate date) throws FitbitAPIException {
        // Example: GET /1/user/228TQ4/foods/log/date/2010-02-25.json
        Response res = getCollectionResponseForDate(localUser, fitbitUser, APICollectionType.foods, date);
        return Foods.constructFoods(res);
    }

    public List<LoggedFood> getLoggedFoods(LocalUserDetail localUser, FitbitUser fitbitUser, ApiCollectionProperty property) throws FitbitAPIException {
        // Example: GET /1/user/228TQ4/foods/log/recent.json
        Response res = getCollectionResponseForProperty(localUser, fitbitUser, APICollectionType.foods, property);
        return LoggedFood.constructLoggedFoodReferenceList(res);
    }

    /**
     * Get a list of a user's favorite foods. A favorite food provides a quick way to log the food
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     *
     * @return list of user's favorite foods
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Favorite-Foods">Fitbit API: API-Get-Favorite-Foods</a>
     */
    public List<FavoriteFood> getFavoriteFoods(LocalUserDetail localUser, FitbitUser fitbitUser) throws FitbitAPIException {
        // Example: GET /1/user/228TQ4/foods/log/favorite.json
        Response res = getCollectionResponseForProperty(localUser, fitbitUser, APICollectionType.foods, ApiCollectionProperty.favorite);
        return FavoriteFood.constructFavoriteFoodList(res);
    }

    /**
     * Get a list of a user's recent foods
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     *
     * @return list of user's recent foods
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Recent-Foods">Fitbit API: API-Get-Recent-Foods</a>
     */
    public List<LoggedFood> getRecentFoods(LocalUserDetail localUser, FitbitUser fitbitUser) throws FitbitAPIException {
        // Example: GET /1/user/228TQ4/foods/log/recent.json
        return getLoggedFoods(localUser, fitbitUser, ApiCollectionProperty.recent);
    }

    /**
     * Get a list of a user's frequent foods
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     *
     * @return list of user's frequent foods
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Frequent-Foods">Fitbit API: API-Get-Frequent-Foods</a>
     */
    public List<LoggedFood> getFrequentFoods(LocalUserDetail localUser, FitbitUser fitbitUser) throws FitbitAPIException {
        // Example: GET /1/user/228TQ4/foods/log/frequent.json
        return getLoggedFoods(localUser, fitbitUser, ApiCollectionProperty.frequent);
    }

    /**
     * Given a search query, get a list of public foods from Fitbit foods database and private foods the user created
     *
     * @param localUser authorized user
     * @param query search query
     *
     * @return list of food search results
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Search-Foods">Fitbit API: API-Search-Foods</a>
     */
    public List<Food> searchFoods(LocalUserDetail localUser, String query) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/228TQ4/foods/search.json?query=apple
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/foods/search", APIFormat.JSON);
        List<PostParameter> params = new ArrayList<PostParameter>(1);
        params.add(new PostParameter("query", query));
        Response res = httpGet(url, params.toArray(new PostParameter[params.size()]), true);
        return Food.constructFoodList(res);
    }

    /**
     * Get list of all valid Fitbit food units
     *
     * @return list of valid food units
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Food-Units">Fitbit API: API-Get-Food-Units</a>
     */
    public List<FoodUnit> getFoodUnits() throws FitbitAPIException {
        clearAccessToken();
        // Example: GET http://api.fitbit.com/1/foods/units.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/foods/units", APIFormat.JSON);
        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        return FoodUnit.constructFoodUnitList(res);
    }

    /**
     * Create log entry for a food
     *
     * @param localUser authorized user
     * @param foodId Food id
     * @param mealTypeId Meal type id
     * @param unitId Unit id
     * @param amount Amount consumed
     * @param date Log entry date
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Food">Fitbit API: API-Log-Food</a>
     */
    public void logFood(LocalUserDetail localUser, long foodId, int mealTypeId, int unitId, String amount, LocalDate date) throws FitbitAPIException {
        List<PostParameter> params = new ArrayList<PostParameter>(5);
        params.add(new PostParameter("foodId", String.valueOf(foodId)));
        params.add(new PostParameter("mealTypeId", mealTypeId));
        params.add(new PostParameter("unitId", String.valueOf(unitId)));
        params.add(new PostParameter("amount", amount));
        params.add(new PostParameter("date", DateTimeFormat.forPattern("yyyy-MM-dd").print(date)));

        logFood(localUser, params);
    }

    /**
     * Create log entry for a food
     *
     * @param localUser authorized user
     * @param params POST request parameters
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Food">Fitbit API: API-Log-Food</a>
     */
    public void logFood(LocalUserDetail localUser, List<PostParameter> params) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: POST /1/user/-/food/log.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/foods/log", APIFormat.JSON);
        try {
            httpPost(url, params.toArray(new PostParameter[params.size()]), true);
        } catch (Exception e) {
            throw new FitbitAPIException("Error creating food log entry: " + e, e);
        }
    }

    /**
     * Delete the user's food log entry with the given id
     *
     * @param localUser authorized user
     * @param foodLogId Food log entry id
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Delete-Food-Log">Fitbit API: API-Delete-Food-Log</a>
     */
    public void deleteFoodLog(LocalUserDetail localUser, String foodLogId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: DELETE /1/user/-/food/log/123.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/foods/log/" + foodLogId, APIFormat.JSON);
        try {
            httpDelete(url, true);
        } catch (Exception e) {
            throw new FitbitAPIException("Error deleting food log entry: " + e, e);
        }
    }

    /**
     * Add the food with the given id to user's list of favorite foods
     *
     * @param localUser authorized user
     * @param foodId Food id
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Add-Favorite-Food">Fitbit API: API-Add-Favorite-Food</a>
     */
    public void addFavoriteFood(LocalUserDetail localUser, String foodId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: POST /1/user/-/food/log/favorite/123.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/foods/log/favorite/" + foodId, APIFormat.JSON);
        try {
            httpPost(url, null, true);
        } catch (Exception e) {
            throw new FitbitAPIException("Error adding favorite food: " + e, e);
        }
    }

    /**
     * Delete the food with the given id from user's list of favorite foods
     *
     * @param localUser authorized user
     * @param foodId Food id
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Delete-Favorite-Food">Fitbit API: API-Delete-Favorite-Food</a>
     */
    public void deleteFavoriteFood(LocalUserDetail localUser, String foodId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: DELETE /1/user/-/food/log/favorite/123.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/foods/log/favorite/" + foodId, APIFormat.JSON);
        try {
            httpDelete(url, true);
        } catch (Exception e) {
            throw new FitbitAPIException("Error deleting favorite food: " + e, e);
        }

    }

    /**
     * Get a list of meals created by user
     *
     * @param localUser authorized user
     *
     * @return list of meals
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Meals">Fitbit API: API-Get-Meals</a>
     */
    public List<Meal> getMeals(LocalUserDetail localUser) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/228TQ4/meals.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/meals", APIFormat.JSON);
        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return Meal.constructMeals(res);
        } catch (JSONException e) {
            throw new FitbitAPIException(e.getMessage() + ": " + res.asString(), e);
        }
    }

    /**
     * Retrieves the list of Fitbit devices for a user
     *
     * @param localUser authorized user
     *
     * @return list of devices
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Devices">Fitbit API: API-Get-Devices</a>
     */
    public List<Device> getDevices(LocalUserDetail localUser) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/-/devices.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/devices", APIFormat.JSON);
        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        return Device.constructDeviceList(res);
    }

    /**
     * Retrieve the attributes of user's Fitbit device
     *
     * @param localUser authorized user
     *
     * @return device
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Device">Fitbit API: API-Get-Device</a>
     */
    public Device getDevice(LocalUserDetail localUser, String deviceId, DeviceType type) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/-/devices/1234.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/devices/" + type.name().toLowerCase() + '/' + deviceId, APIFormat.JSON);
        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return new Device(res.asJSONObject().getJSONObject("device"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving device: " + e, e);
        }
    }

    public Response getCollectionResponseForDate(LocalUserDetail localUser, FitbitUser fitbitUser, APICollectionType type, LocalDate date) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/228TQ4/foods/log/date/2010-02-25.json
        String url = APIUtil.constructFullUrl(getApiBaseUrl(), getApiVersion(), fitbitUser, type, date, APIFormat.JSON);
        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        return res;
    }

    public Response getCollectionResponseForProperty(LocalUserDetail localUser, FitbitUser fitbitUser, APICollectionType type, ApiCollectionProperty property) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/228TQ4/foods/log/recent.json
        String url = APIUtil.constructFullUrl(getApiBaseUrl(), getApiVersion(), fitbitUser, type, property, APIFormat.JSON);
        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        return res;
    }

    public Object getCollectionForDate(LocalUserDetail localUser, FitbitUser fitbitUser, APICollectionType type, LocalDate date) throws FitbitAPIException {
        switch (type) {
            case activities:
                return getActivities(localUser, fitbitUser, date);
            case foods:
                return getFoods(localUser, fitbitUser, date);
            case meals:
                return getMeals(localUser);
            default:
                return null;
        }
    }

    /**
     * Retrieve a user's body measurements for a given day
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     * @param date day to retrieve data for
     *
     * @return body measurements for a give date
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Body-Measurements">Fitbit API: API-Get-Body-Measurements</a>
     */
    public double getWeight(LocalUserDetail localUser, FitbitUser fitbitUser, LocalDate date) throws FitbitAPIException {
        return getBody(localUser, fitbitUser, date).getWeight();
    }

    /**
     * Retrieve a user's body measurements for a given day
     *
     * @param localUser authorized user
     * @param date day to retrieve data for
     *
     * @return body measurements for a give date
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Body-Measurements">Fitbit API: API-Get-Body-Measurements</a>
     */
    public double getWeight(LocalUserDetail localUser, String date) throws FitbitAPIException {
        return getBody(localUser, date).getWeight();
    }

    /**
     * Get a summary of a user's body measurements for a given day
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     * @param date day to retrieve data for
     *
     * @return body measurements for a give date
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Body-Measurements">Fitbit API: API-Get-Body-Measurements</a>
     */
    public Body getBody(LocalUserDetail localUser, FitbitUser fitbitUser, LocalDate date) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/228TQ4/body/2010-02-25.json
        String url = APIUtil.constructFullUrl(getApiBaseUrl(), getApiVersion(), fitbitUser, APICollectionType.body, date, APIFormat.JSON);

        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return new Body(res.asJSONObject());
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving body: " + e, e);
        }
    }

    /**
     * Get a summary of a user's body measurements for a given day
     *
     * @param localUser authorized user
     * @param date day to retrieve data for
     *
     * @return body measurements for a give date
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Body-Measurements">Fitbit API: API-Get-Body-Measurements</a>
     */
    public Body getBody(LocalUserDetail localUser, String date) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/-/body/date/2010-02-25.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/body/date/" + date, APIFormat.JSON);

        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return new Body(res.asJSONObject());
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving body: " + e, e);
        }
    }

    /**
     * Log weight
     *
     * @param localUser authorized user
     * @param weight Weight
     * @param date Log entry date
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Weight">Fitbit API: API-Log-Weight</a>
     */
    public void logWeight(LocalUserDetail localUser, float weight, LocalDate date) throws FitbitAPIException {
        List<PostParameter> params = new ArrayList<PostParameter>(2);
        params.add(new PostParameter("weight", weight));
        params.add(new PostParameter("date", DateTimeFormat.forPattern("yyyy-MM-dd").print(date)));

        logWeight(localUser, params);
    }

    /**
     * Log weight
     *
     * @param localUser authorized user
     * @param params POST request parameters
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Weight">Fitbit API: API-Log-Weight</a>
     */
    public void logWeight(LocalUserDetail localUser, List<PostParameter> params) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: POST /1/user/-/body/weight.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/body/weight", APIFormat.JSON);

        try {
            httpPost(url, params.toArray(new PostParameter[params.size()]), true);
        } catch (FitbitAPIException e) {
            throw new FitbitAPIException("Error logging weight: " + e, e);
        }
    }

    /**
     * Create log entry for a water in custom volume units
     *
     * @param localUser authorized user
     * @param amount Amount consumed
     * @param date Log entry date
     * @param volumeUnit Custom volume unit
     *
     * @return new water log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Water">Fitbit API: API-Log-Water</a>
     */
    public WaterLog logWater(LocalUserDetail localUser, float amount, VolumeUnits volumeUnit, LocalDate date) throws FitbitAPIException {
        List<PostParameter> params = new ArrayList<PostParameter>(2);
        params.add(new PostParameter("amount", amount));
        params.add(new PostParameter("date", DateTimeFormat.forPattern("yyyy-MM-dd").print(date)));
        if (volumeUnit != null) {
            params.add(new PostParameter("unit", volumeUnit.getText()));
        }

        return logWater(localUser, params);
    }

    /**
     * Create log entry for a water
     *
     * @param localUser authorized user
     * @param amount Amount consumed
     * @param date Log entry date
     *
     * @return new water log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Water">Fitbit API: API-Log-Water</a>
     */
    public WaterLog logWater(LocalUserDetail localUser, float amount, LocalDate date) throws FitbitAPIException {
        return logWater(localUser, amount, null, date);
    }

    public WaterLog logWater(LocalUserDetail localUser, List<PostParameter> params) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: POST /1/user/-/foods/log/water.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/foods/log/water", APIFormat.JSON);

        try {
            Response res = httpPost(url, params.toArray(new PostParameter[params.size()]), true);
            return new WaterLog(res.asJSONObject().getJSONObject("waterLog"));
        } catch (FitbitAPIException e) {
            throw new FitbitAPIException("Error logging water: " + e, e);
        } catch (JSONException e) {
            throw new FitbitAPIException("Error logging water: " + e, e);
        }
    }

    /**
     * Get a summary and list of a user's water log entries for a given day
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     * @param date date to retrieve data for
     *
     * @return water for a given day
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Water">Fitbit API: API-Get-Water</a>
     */
    public Water getLoggedWater(LocalUserDetail localUser, FitbitUser fitbitUser, LocalDate date) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/228TQ4/foods/log/water/date/2010-02-25.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/" + fitbitUser.getId() + "/foods/log/water/date/" + DateTimeFormat.forPattern("yyyy-MM-dd").print(date), APIFormat.JSON);

        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return new Water(res.asJSONObject());
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving water: " + e, e);
        }
    }

    /**
     * Delete user's water log entry with the given id
     *
     * @param localUser authorized user
     * @param logWaterId Water log entry id
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Delete-Water-Log">Fitbit API: API-Delete-Water-Log</a>
     */
    public void deleteWater(LocalUserDetail localUser, String logWaterId) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: DELETE /1/user/-/foods/log/water/123.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/foods/log/water/" + logWaterId, APIFormat.JSON);
        try {
            httpDelete(url, true);
        } catch (Exception e) {
            throw new FitbitAPIException("Error deleting water: " + e, e);
        }

    }

    /**
     * Get a user's profile
     *
     * @param localUser authorized user
     *
     * @return profile of a user
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-User-Info">Fitbit API: API-Get-User-Info</a>
     */
    public UserInfo getUserInfo(LocalUserDetail localUser) throws FitbitAPIException {
        return getUserInfo(localUser, FitbitUser.CURRENT_AUTHORIZED_USER);
    }

    /**
     * Get a user's profile
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     *
     * @return profile of a user
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-User-Info">Fitbit API: API-Get-User-Info</a>
     */
    public UserInfo getUserInfo(LocalUserDetail localUser, FitbitUser fitbitUser) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: GET /1/user/-/profile.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/" + fitbitUser.getId() + "/profile", APIFormat.JSON);

        try {
            Response response = httpGet(url, true);
            throwExceptionIfError(response);
            return new UserInfo(response.asJSONObject());
        } catch (FitbitAPIException e) {
            throw new FitbitAPIException("Error getting user info: " + e, e);
        } catch (JSONException e) {
            throw new FitbitAPIException("Error getting user info: " + e, e);
        }
    }

    /**
     * Update user's profile
     *
     * @param localUser authorized user
     * @param params list of values to update
     *
     * @return updated profile of a user
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Update-User-Info">Fitbit API: API-Update-User-Info</a>
     */
    public UserInfo updateUserInfo(LocalUserDetail localUser, List<PostParameter> params) throws FitbitAPIException {
        setAccessToken(localUser);
        // Example: POST /1/user/-/profile.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/profile", APIFormat.JSON);

        try {
            Response response = httpPost(url, params.toArray(new PostParameter[params.size()]), true);
            throwExceptionIfError(response);
            return new UserInfo(response.asJSONObject());
        } catch (FitbitAPIException e) {
            throw new FitbitAPIException("Error updating profile: " + e, e);
        } catch (JSONException e) {
            throw new FitbitAPIException("Error updating profile: " + e, e);
        }
    }

    /**
     * Register new Fitbit user account
     * <p/>
     * <b>Info: This method is not generally available in public API, though this permission could be given to partners that need this functionality</b>
     *
     * @param email Email
     * @param password password
     * @param timezone timezone string
     *
     * @return Account
     *
     * @throws com.fitbit.api.FitbitAPIException fitbit api Exception
     */
    public Account registerAccount(String email, String password, String timezone) throws FitbitAPIException {
        return registerAccount(email, password, timezone, false);
    }

    /**
     * Register new Fitbit user account
     * <p/>
     * <b>Info: This method is not generally available in public API, though this permission could be given to partners that need this functionality</b>
     *
     * @param email Email
     * @param password password
     * @param timezone timezone string
     * @param emailSubscribe Subscribe to email newsletter
     *
     * @return Account
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     */
    public Account registerAccount(String email, String password, String timezone, boolean emailSubscribe) throws FitbitAPIException {

        List<PostParameter> params = new ArrayList<PostParameter>();
        params.add(new PostParameter("email", email));
        params.add(new PostParameter("password", password));
        params.add(new PostParameter("timezone", timezone));
        params.add(new PostParameter("emailSubscribe", String.valueOf(emailSubscribe)));

        // POST /1/user/-/account/register.json
        String url = APIUtil.contextualizeUrl(getApiBaseSecuredUrl(), getApiVersion(), "/account/register", APIFormat.JSON);

        Response response = httpPost(url, params.toArray(new PostParameter[params.size()]), true);

        try {
            return new Account(response.asJSONObject().getJSONObject("account"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error parsing json response to Account object: ", e);
        }
    }

    /**
     * Invite another user to be a friend given his userId
     *
     * @param localUser     authorized user
     * @param invitedUserId invited user id
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Create-Invite">Fitbit API: API-Create-Invite</a>
     */
    public void inviteByUserId(LocalUserDetail localUser, String invitedUserId) throws FitbitAPIException {
        setAccessToken(localUser);

        List<PostParameter> params = new ArrayList<PostParameter>();
        params.add(new PostParameter("invitedUserId", invitedUserId));

        // POST /1/user/-/friends/invitations.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/friends/invitations", APIFormat.JSON);

        httpPost(url, params.toArray(new PostParameter[params.size()]), true);
    }

    /**
     * Invite another user to be a friend given his email
     *
     * @param localUser        authorized user
     * @param invitedUserEmail invited user's email
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Create-Invite">Fitbit API: API-Create-Invite</a>
     */
    public void inviteByEmail(LocalUserDetail localUser, String invitedUserEmail) throws FitbitAPIException {
        setAccessToken(localUser);

        List<PostParameter> params = new ArrayList<PostParameter>();
        params.add(new PostParameter("invitedUserEmail", invitedUserEmail));

        // POST /1/user/-/friends/invitations.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/friends/invitations", APIFormat.JSON);

        httpPost(url, params.toArray(new PostParameter[params.size()]), true);
    }

    /**
     * Accept friend invitation from another user
     *
     * @param localUser  authorized user
     * @param fitbitUser inviting user
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Accept-Invite">Fitbit API: API-Accept-Invite</a>
     */
    public void acceptInvitationFromUser(LocalUserDetail localUser, FitbitUser fitbitUser) throws FitbitAPIException {
        setAccessToken(localUser);

        List<PostParameter> params = new ArrayList<PostParameter>();
        params.add(new PostParameter("accept", String.valueOf(true)));

        // POST /1/user/-/friends/invitations/228KP9.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/friends/invitations/" + fitbitUser.getId(), APIFormat.JSON);

        httpPost(url, params.toArray(new PostParameter[params.size()]), true);
    }

    /**
     * Reject friend invitation from another user
     *
     * @param localUser  authorized user
     * @param fitbitUser inviting user
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Accept-Invite">Fitbit API: API-Accept-Invite</a>
     */
    public void rejectInvitationFromUser(LocalUserDetail localUser, FitbitUser fitbitUser) throws FitbitAPIException {
        setAccessToken(localUser);

        List<PostParameter> params = new ArrayList<PostParameter>();
        params.add(new PostParameter("accept", String.valueOf(false)));

        // POST /1/user/-/friends/invitations/228KP9.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/friends/invitations/" + fitbitUser.getId(), APIFormat.JSON);

        httpPost(url, params.toArray(new PostParameter[params.size()]), true);
    }


    /**
     * Get a summary and list of a user's sleep log entries for a given day
     *
     * @param localUser authorized user
     * @param fitbitUser user to retrieve data from
     * @param date date to retrieve data for
     *
     * @return sleep for a given day
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Sleep">Fitbit API: API-Get-Sleep</a>
     */
    public Sleep getSleep(LocalUserDetail localUser, FitbitUser fitbitUser, LocalDate date) throws FitbitAPIException {
        // Example: GET /1/user/228TQ4/sleep/date/2010-02-25.json
        Response res = getCollectionResponseForDate(localUser, fitbitUser, APICollectionType.sleep, date);
        throwExceptionIfError(res);
        return Sleep.constructSleep(res);
    }

    /**
     * Create log entry for a sleep
     *
     * @param localUser authorized user
     * @param date Log entry date
     * @param startTime Start time
     * @param duration Duration
     *
     * @return new sleep log entry
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Log-Sleep">Fitbit API: API-Log-Sleep</a>
     */
    public SleepLog logSleep(LocalUserDetail localUser, LocalDate date, LocalTime startTime, long duration) throws FitbitAPIException {
        setAccessToken(localUser);

        List<PostParameter> params = new ArrayList<PostParameter>();
        params.add(new PostParameter("date", FitbitApiService.LOCAL_DATE_FORMATTER.print(date)));
        params.add(new PostParameter("startTime", FitbitApiService.LOCAL_TIME_HOURS_MINUTES_FORMATTER.print(startTime)));
        params.add(new PostParameter("duration", duration));

        // POST /1/user/-/sleep.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/sleep", APIFormat.JSON);

        Response response = httpPost(url, params.toArray(new PostParameter[params.size()]), true);

        try {
            return new SleepLog(response.asJSONObject().getJSONObject("sleep"));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error parsing json response to SleepLog object: ", e);
        }
    }

    /**
     * Delete user's sleep log entry with the given id
     *
     * @param localUser authorized user
     * @param sleepLogId Sleep log entry id
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Delete-Sleep-Log">Fitbit API: API-Delete-Sleep-Log</a>
     */
    public void deleteSleepLog(LocalUserDetail localUser, Long sleepLogId) throws FitbitAPIException {
        setAccessToken(localUser);

        // POST /1/user/-/sleep/345275.json
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/sleep/" + sleepLogId, APIFormat.JSON);

        httpDelete(url, true);
    }


    /**
     * Get Rate Limiting Quota left for the IP
     *
     * @return quota
     */
    public ApiRateLimitStatus getIpRateLimitStatus() throws FitbitAPIException {
        return getRateLimitStatus(ApiQuotaType.IP_ADDRESS);
    }

    /**
     * Get Rate Limiting Quota left for the Client+Owner
     *
     * @param localUser authorized user
     *
     * @return quota
     */
    public ApiRateLimitStatus getClientAndUserRateLimitStatus(LocalUserDetail localUser) throws FitbitAPIException {
        setAccessToken(localUser);
        return getRateLimitStatus(ApiQuotaType.CLIENT_AND_OWNER);
    }

    public ApiRateLimitStatus getRateLimitStatus(ApiQuotaType quotaType) throws FitbitAPIException {
        // Example: GET /1/account/clientAndUserRateLimitStatus.json OR /1/account/ipRateLimitStatus.json
        String relativePath = "/account/" + (quotaType == ApiQuotaType.CLIENT_AND_OWNER ? "clientAndUser" : "ip") + "RateLimitStatus";
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), APIVersion.BETA_1, relativePath, APIFormat.JSON);
        return new ApiRateLimitStatus(httpGet(url, true));
    }

    /**
     * Adds a subscription to all user's collections
     *
     * @param subscriberId ID of a subscriber for this subscription, defined on <a href="https://dev.fitbit.com/apps">dev.fitbit.com</a>
     * @param localUser authorized user
     * @param fitbitUser user to subscribe to
     *
     * @return details of a new subscription
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/Subscriptions-API#Subscriptions-API-Addasubscription">Fitbit API: Subscriptions-API</a>
     */
    public SubscriptionDetail subscribe(String subscriberId, LocalUserDetail localUser, FitbitUser fitbitUser) throws FitbitAPIException {
        return nullSafeSubscribe(subscriberId, localUser, fitbitUser, null, null);
    }

    /**
     * Adds a subscription to user's collection
     *
     * @param subscriberId ID of a subscriber for this subscription, defined on <a href="https://dev.fitbit.com/apps">dev.fitbit.com</a>
     * @param localUser authorized user
     * @param fitbitUser user to subscribe to
     * @param collectionType type of a collection to subscribe to
     *
     * @return details of a new subscription
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/Subscriptions-API#Subscriptions-API-Addasubscription">Fitbit API: Subscriptions-API</a>
     */
    public SubscriptionDetail subscribe(String subscriberId, LocalUserDetail localUser, FitbitUser fitbitUser, APICollectionType collectionType) throws FitbitAPIException {
        return nullSafeSubscribe(subscriberId, localUser, fitbitUser, collectionType, null);
    }

    /**
     * Adds a subscription with given id to all user's collections
     *
     * @param subscriberId ID of a subscriber for this subscription, defined on <a href="https://dev.fitbit.com/apps">dev.fitbit.com</a>
     * @param localUser authorized user
     * @param fitbitUser user to subscribe to
     * @param subscriptionId The ID of the subscription that makes sense to your application
     *
     * @return details of a new subscription
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/Subscriptions-API#Subscriptions-API-Addasubscription">Fitbit API: Subscriptions-API</a>
     */
    public SubscriptionDetail subscribe(String subscriberId, LocalUserDetail localUser, FitbitUser fitbitUser, String subscriptionId) throws FitbitAPIException {
        return nullSafeSubscribe(subscriberId, localUser, fitbitUser, null, subscriptionId);
    }

    /**
     * Adds a subscription with given id to user's collection
     *
     * @param subscriberId ID of a subscriber for this subscription, defined on <a href="https://dev.fitbit.com/apps">dev.fitbit.com</a>
     * @param localUser authorized user
     * @param fitbitUser user to subscribe to
     * @param collectionType type of a collection to subscribe to
     * @param subscriptionId The ID of the subscription that makes sense to your application
     *
     * @return details of a new subscription
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/Subscriptions-API#Subscriptions-API-Addasubscription">Fitbit API: Subscriptions-API</a>
     */
    public SubscriptionDetail subscribe(String subscriberId, LocalUserDetail localUser, FitbitUser fitbitUser, APICollectionType collectionType, String subscriptionId) throws FitbitAPIException {
        return nullSafeSubscribe(subscriberId, localUser, fitbitUser, collectionType, subscriptionId);
    }

    /**
     * Removes a subscription with given id from all user's collections
     *
     * @param subscriberId ID of a subscriber for this subscription, defined on <a href="https://dev.fitbit.com/apps">dev.fitbit.com</a>
     * @param localUser authorized user
     * @param fitbitUser user to subscribe to
     * @param subscriptionId The ID of the subscription
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/Subscriptions-API#Subscriptions-API-Removeasubscription">Fitbit API: Subscriptions-API</a>
     */
    public void unsubscribe(String subscriberId, LocalUserDetail localUser, FitbitUser fitbitUser, String subscriptionId) throws FitbitAPIException {
        nullSafeUnsubscribe(subscriberId, localUser, fitbitUser, null, subscriptionId);
    }

    /**
     * Removes a subscription with given id from user's collection
     *
     * @param subscriberId ID of a subscriber for this subscription, defined on <a href="https://dev.fitbit.com/apps">dev.fitbit.com</a>
     * @param localUser authorized user
     * @param fitbitUser user to subscribe to
     * @param collectionType type of a collection to unsubscribe from
     * @param subscriptionId The ID of the subscription
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/Subscriptions-API#Subscriptions-API-Removeasubscription">Fitbit API: Subscriptions-API</a>
     */
    public void unsubscribe(String subscriberId, LocalUserDetail localUser, FitbitUser fitbitUser, APICollectionType collectionType, String subscriptionId) throws FitbitAPIException {
        nullSafeUnsubscribe(subscriberId, localUser, fitbitUser, collectionType, subscriptionId);
    }

    /* ********************************************************************* */

    protected SubscriptionDetail nullSafeSubscribe(String subscriberId, LocalUserDetail localUser, FitbitUser fitbitUser, APICollectionType collectionType, String subscriptionId) throws FitbitAPIException {
        setAccessToken(localUser);

        String url =
                APIUtil.constructFullSubscriptionUrl(
                        getApiBaseUrl(),
                        getApiVersion(),
                        fitbitUser,
                        collectionType,
                        null == subscriptionId ? APIUtil.UNSPECIFIED_SUBSCRIPTION_ID : subscriptionId,
                        APIFormat.JSON
                );
        setSubscriberId(subscriberId);

        try {
            return new SubscriptionDetail(httpPost(url, null, true).asJSONObject());
        } catch (FitbitAPIException e) {
            throw e;
        } catch (Exception e) {
            throw new FitbitAPIException("Could not create subscription: " + e, e);
        }
    }

    protected void nullSafeUnsubscribe(String subscriberId, LocalUserDetail localUser, FitbitUser fitbitUser, APICollectionType collectionType, String subscriptionId) throws FitbitAPIException {
        setAccessToken(localUser);

        String url =
                APIUtil.constructFullSubscriptionUrl(
                        getApiBaseUrl(),
                        getApiVersion(),
                        fitbitUser,
                        collectionType,
                        subscriptionId,
                        APIFormat.JSON
                );
        setSubscriberId(subscriberId);

        httpDelete(url, true);
    }

    public List<ApiSubscription> getSubscriptions(LocalUserDetail localUser) throws FitbitAPIException {
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/apiSubscriptions", APIFormat.JSON);
        return getSubscriptions(localUser, url);
    }

    public List<ApiSubscription> getSubscriptions(LocalUserDetail localUser, APICollectionType collectionType) throws FitbitAPIException {
        String url = APIUtil.contextualizeUrl(getApiBaseUrl(), getApiVersion(), "/user/-/" + collectionType +"/apiSubscriptions", APIFormat.JSON);
        return getSubscriptions(localUser, url);
    }

    private List<ApiSubscription> getSubscriptions(LocalUserDetail localUser, String url) throws FitbitAPIException {
        setAccessToken(localUser);

        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            JSONObject jsonObject = res.asJSONObject();
            JSONArray jsonArray = jsonObject.getJSONArray("apiSubscriptions");
            List<ApiSubscription> result = new ArrayList<ApiSubscription>(jsonArray.length());
            for(int i = 0; i < jsonArray.length(); i++) {
                ApiSubscription apiSubscription = new ApiSubscription(jsonArray.getJSONObject(i));
                result.add(apiSubscription);
            }
            return result;
        } catch (JSONException e) {
            throw new FitbitAPIException("Error retrieving water: " + e, e);
        }
    }

    /**
     * Get time series in the specified range for a given resource of a user (as an unauthorized)
     *
     * @param user user to fetch data from
     * @param resourceType type of a resource
     * @param startDate End date of a time range
     * @param period Depth of a time range
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Time-Series">Fitbit API: API-Get-Time-Series</a>
     */
    public List<Data> getTimeSeries(FitbitUser user, TimeSeriesResourceType resourceType, LocalDate startDate, TimePeriod period) throws FitbitAPIException {
        return getTimeSeries(null, user, resourceType, startDate.toString(), period.getShortForm());
    }

    /**
     * Get time series in the specified range for a given resource of a user (as an unauthorized)
     *
     * @param user user to fetch data from
     * @param resourceType type of a resource
     * @param startDate End date of a time range
     * @param period Depth of a time range
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Time-Series">Fitbit API: API-Get-Time-Series</a>
     */
    public List<Data> getTimeSeries(FitbitUser user, TimeSeriesResourceType resourceType, String startDate, TimePeriod period) throws FitbitAPIException {
        return getTimeSeries(null, user, resourceType, startDate, period.getShortForm());
    }

    /**
     * Get time series in the specified range for a given resource of a user (as an unauthorized)
     *
     * @param user user to fetch data from
     * @param resourceType type of a resource
     * @param startDate Start date of a time range
     * @param endDate End date of a time range
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Time-Series">Fitbit API: API-Get-Time-Series</a>
     */
    public List<Data> getTimeSeries(FitbitUser user, TimeSeriesResourceType resourceType, LocalDate startDate, LocalDate endDate) throws FitbitAPIException {
        return getTimeSeries(null, user, resourceType, startDate.toString(), endDate.toString());
    }

    /**
     * Get time series in the specified range for a given resource of a user
     *
     * @param localUser authorized user
     * @param user user to fetch data from
     * @param resourceType type of a resource
     * @param startDate End date of a time range
     * @param period Depth of a time range
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Time-Series">Fitbit API: API-Get-Time-Series</a>
     */
    public List<Data> getTimeSeries(LocalUserDetail localUser, FitbitUser user, TimeSeriesResourceType resourceType, LocalDate startDate, TimePeriod period) throws FitbitAPIException {
        return getTimeSeries(localUser, user, resourceType, startDate.toString(), period.getShortForm());
    }

    /**
     * Get time series in the specified range for a given resource of a user
     *
     * @param localUser authorized user
     * @param user user to fetch data from
     * @param resourceType type of a resource
     * @param startDate End date of a time range
     * @param period Depth of a time range
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Time-Series">Fitbit API: API-Get-Time-Series</a>
     */
    public List<Data> getTimeSeries(LocalUserDetail localUser, FitbitUser user, TimeSeriesResourceType resourceType, String startDate, TimePeriod period) throws FitbitAPIException {
        return getTimeSeries(localUser, user, resourceType, startDate, period.getShortForm());
    }

    /**
     * Get time series in the specified range for a given resource of a user
     *
     * @param localUser authorized user
     * @param user user to fetch data from
     * @param resourceType type of a resource
     * @param startDate Start date of a time range
     * @param endDate End date of a time range
     *
     * @throws com.fitbit.api.FitbitAPIException Fitbit API Exception
     * @see <a href="http://wiki.fitbit.com/display/API/API-Get-Time-Series">Fitbit API: API-Get-Time-Series</a>
     */
    public List<Data> getTimeSeries(LocalUserDetail localUser, FitbitUser user, TimeSeriesResourceType resourceType, LocalDate startDate, LocalDate endDate) throws FitbitAPIException {
        return getTimeSeries(localUser, user, resourceType, startDate.toString(), endDate.toString());
    }

    public List<Data> getTimeSeries(LocalUserDetail localUser, FitbitUser user, TimeSeriesResourceType resourceType, String startDate, String periodOrEndDate) throws FitbitAPIException {
        if (localUser != null) {
            setAccessToken(localUser);
        } else {
            clearAccessToken();
        }

        String url = APIUtil.constructTimeSeriesUrl(getApiBaseUrl(), getApiVersion(), user, resourceType, startDate, periodOrEndDate, APIFormat.JSON);
        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            return Data.jsonArrayToDataList(res.asJSONObject().getJSONArray(resourceType.getResourcePath().substring(1).replace('/', '-')));
        } catch (JSONException e) {
            throw new FitbitAPIException("Error parsing json response to data list : ", e);
        }
    }

    public IntradaySummary getIntraDayTimeSeries(LocalUserDetail localUser, FitbitUser user, TimeSeriesResourceType resourceType, LocalDate date) throws FitbitAPIException {
        return getIntraDayTimeSeries(localUser, user, resourceType, date.toString());
    }

    public IntradaySummary getIntraDayTimeSeries(LocalUserDetail localUser, FitbitUser user, TimeSeriesResourceType resourceType, String date) throws FitbitAPIException {
        if (localUser != null) {
            setAccessToken(localUser);
        } else {
            clearAccessToken();
        }

        String url = APIUtil.constructTimeSeriesUrl(getApiBaseUrl(), getApiVersion(), user, resourceType, date, TimePeriod.INTRADAY.getShortForm(), APIFormat.JSON);
        Response res = httpGet(url, true);
        throwExceptionIfError(res);
        try {
            List<Data> dataList = Data.jsonArrayToDataList(res.asJSONObject().getJSONArray(resourceType.getResourcePath().substring(1).replace('/', '-')));
            IntradayDataset intradayDataset = new IntradayDataset(res.asJSONObject().getJSONObject(resourceType.getResourcePath().substring(1).replace('/', '-') + "-intraday"));
            return new IntradaySummary(dataList.get(0), intradayDataset);
        } catch (JSONException e) {
            throw new FitbitAPIException("Error parsing json response to IntradaySummary : ", e);
        }
    }

    /* ********************************************************************* */

    protected void setAccessToken(LocalUserDetail localUser) {
        // Get the access token for the user:
        APIResourceCredentials resourceCredentials = credentialsCache.getResourceCredentials(localUser);
        // Set the access token in the client:
        setOAuthAccessToken(resourceCredentials.getAccessToken(), resourceCredentials.getAccessTokenSecret(), resourceCredentials.getLocalUserId());
    }

    protected void clearAccessToken() {
        // Set the access token in the client to null:
        setOAuthAccessToken(null);
    }

    /**
     * Issues an HTTP GET request.
     *
     * @param url the request url
     * @param authenticate if true, the request will be sent with BASIC authentication header
     *
     * @return the response
     *
     * @throws FitbitAPIException when Fitbit service or network is unavailable
     */

    protected Response httpGet(String url, boolean authenticate) throws FitbitAPIException {
        return httpGet(url, null, authenticate);
    }

    /**
     * Issues an HTTP GET request.
     *
     * @param url the request url
     * @param authenticate if true, the request will be sent with BASIC authentication header
     * @param name1 the name of the first parameter
     * @param value1 the value of the first parameter
     *
     * @return the response
     *
     * @throws FitbitAPIException when Fitbit service or network is unavailable
     */

    protected Response httpGet(String url, String name1, String value1, boolean authenticate) throws FitbitAPIException {
        return httpGet(url, new PostParameter[]{new PostParameter(name1, value1)}, authenticate);
    }

    /**
     * Issues an HTTP GET request.
     *
     * @param url the request url
     * @param name1 the name of the first parameter
     * @param value1 the value of the first parameter
     * @param name2 the name of the second parameter
     * @param value2 the value of the second parameter
     * @param authenticate if true, the request will be sent with BASIC authentication header
     *
     * @return the response
     *
     * @throws FitbitAPIException when Fitbit service or network is unavailable
     */
    protected Response httpGet(String url, String name1, String value1, String name2, String value2, boolean authenticate) throws FitbitAPIException {
        return httpGet(url, new PostParameter[]{new PostParameter(name1, value1), new PostParameter(name2, value2)}, authenticate);
    }

    /**
     * Issues an HTTP GET request.
     *
     * @param url the request url
     * @param params the request parameters
     * @param authenticate if true, the request will be sent with BASIC authentication header
     *
     * @return the response
     *
     * @throws FitbitAPIException when Fitbit service or network is unavailable
     */
    protected Response httpGet(String url, PostParameter[] params, boolean authenticate) throws FitbitAPIException {
        return http.get(appendParamsToUrl(url, params), authenticate);
    }

    protected Response httpPost(String url, PostParameter[] params, boolean authenticate) throws FitbitAPIException {
        return http.post(url, params, authenticate);
    }

    protected Response httpDelete(String url, boolean authenticate) throws FitbitAPIException {
        return httpDelete(url, null, authenticate);
    }

    protected Response httpDelete(String url, PostParameter[] params, boolean authenticate) throws FitbitAPIException {
        // We use Sun's HttpURLConnection, which does not like request entities
        // submitted on HTTP DELETE
        return http.delete(appendParamsToUrl(url, params), authenticate);
    }

    protected static String appendParamsToUrl(String url, PostParameter[] params) {
        if (null != params && params.length > 0) {
            return url + '?' + HttpClient.encodeParameters(params);
        }
        return url;
    }

    public static void throwExceptionIfError(Response res) throws FitbitAPIException {
        if (res.isError()) {
            throw new FitbitAPIException(getErrorMessage(res));
        }
    }

    public static String getErrorMessage(Response res) throws FitbitAPIException {
        return res.isError() ? res.asString() : "";
    }

    /**
     * Set unit system for future API calls
     *
     * @param locale requested unit system
     *
     * @see <a href="http://wiki.fitbit.com/display/API/API-Unit-System">Fitbit API: API-Unit-System</a>
     */
    public void setLocale(Locale locale) {
        if (locale == null) {
            http.removeRequestHeader("Accept-Language");
        } else {
            http.setRequestHeader("Accept-Language", locale.toString());
        }
    }
}
