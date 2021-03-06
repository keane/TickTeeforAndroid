package com.huhukun.tickteeforandroid.providers;

import android.util.Log;

import com.huhukun.tickteeforandroid.App_Constants;
import com.huhukun.tickteeforandroid.exception.DeviceConnectionException;
import com.huhukun.tickteeforandroid.exception.NetworkSystemException;
import com.huhukun.tickteeforandroid.exception.WebServiceFailedException;
import com.huhukun.tickteeforandroid.TickTeeAndroid;
import com.huhukun.tickteeforandroid.model.Project;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.huhukun.tickteeforandroid.providers.WebApiConstants.PARAM_ALERT_TYPE;
import static com.huhukun.tickteeforandroid.providers.WebApiConstants.PARAM_CREATED_AT;
import static com.huhukun.tickteeforandroid.providers.WebApiConstants.PARAM_CURRENT_PROGRESS;
import static com.huhukun.tickteeforandroid.providers.WebApiConstants.PARAM_DESCRIPTION;
import static com.huhukun.tickteeforandroid.providers.WebApiConstants.PARAM_END_AT;
import static com.huhukun.tickteeforandroid.providers.WebApiConstants.PARAM_EXPECTED_PROGRESS;
import static com.huhukun.tickteeforandroid.providers.WebApiConstants.PARAM_INIT_PROGRESS;
import static com.huhukun.tickteeforandroid.providers.WebApiConstants.PARAM_IS_CONSUMED;
import static com.huhukun.tickteeforandroid.providers.WebApiConstants.PARAM_IS_DECIMAL_UNIT;
import static com.huhukun.tickteeforandroid.providers.WebApiConstants.PARAM_NAME;
import static com.huhukun.tickteeforandroid.providers.WebApiConstants.PARAM_PROJECT;
import static com.huhukun.tickteeforandroid.providers.WebApiConstants.PARAM_SCHEDULE;
import static com.huhukun.tickteeforandroid.providers.WebApiConstants.PARAM_START_AT;
import static com.huhukun.tickteeforandroid.providers.WebApiConstants.PARAM_TARGET;
import static com.huhukun.tickteeforandroid.providers.WebApiConstants.PARAM_UNIT;
import static com.huhukun.tickteeforandroid.providers.WebApiConstants.PARAM_UPDATED_AT;

public class InsertCommand extends RESTCommand {

    private static final String TAG = App_Constants.APP_TAG +"InsertCommand";

    private long requestId;
    private JSONObject projectJson;

    public InsertCommand( long requestId, String name, String description, String startAt,
                          String endAt, String expectedProgress, String currentProgress,
                          String createdAt, String updatedAt, String target, String unit,
                          String alert, boolean isDecimal, String initProgress, boolean isConsumed, String schedule )
            throws JSONException {
        this.requestId = requestId;
        JSONObject json = new JSONObject();
        json.put(PARAM_NAME, name);
        json.put(PARAM_DESCRIPTION, description);
        json.put(PARAM_START_AT, startAt == null? JSONObject.NULL : startAt);
        json.put(PARAM_END_AT, endAt == null? JSONObject.NULL: endAt);
        json.put(PARAM_EXPECTED_PROGRESS, expectedProgress);
        json.put(PARAM_CURRENT_PROGRESS,currentProgress);
        json.put(PARAM_CREATED_AT, createdAt);
        json.put(PARAM_UPDATED_AT, updatedAt);
        json.put(PARAM_TARGET, target);
        json.put(PARAM_UNIT, unit);
        json.put(PARAM_ALERT_TYPE, alert);
        json.put(PARAM_IS_DECIMAL_UNIT, isDecimal);
        json.put(PARAM_INIT_PROGRESS, initProgress);
        json.put(PARAM_IS_CONSUMED, isConsumed);
        json.put(PARAM_SCHEDULE, schedule);
        projectJson = new JSONObject();
        projectJson.put(PARAM_PROJECT, json);
        Log.e(TAG, projectJson.toString());
    }

    @Override
    protected int handleRequest( String authToken )
            throws DeviceConnectionException,
            NetworkSystemException,
            WebServiceFailedException
    {

        HttpResponse resp;
        JSONObject jsonObject;
        int statusCode;
        String respText;
        Project detail;

        final List<NameValuePair> httpHeaders = new ArrayList<NameValuePair>();





        String email = TickTeeAndroid.appSetting.getString(App_Constants.PREF_EMAIL, null);
        String token = TickTeeAndroid.appSetting.getString(App_Constants.PREF_TOKEN, null);

        httpHeaders.add(new BasicNameValuePair(WebApiConstants.HEADER_ACCESS_EMAIL_PARAM, email));
        httpHeaders.add(new BasicNameValuePair(WebApiConstants.HEADER_ACCESS_TOKEN_PARAM, token));

        try {
            final HttpPost post;

            post = NetworkUtils.BUILDER(WebApiConstants.PROJECTS_URL)
                    .setHeader(httpHeaders).toPost(projectJson.toString());


            createHttpClient();


            resp = mHttpClient.execute(post);
        } catch (IOException e) {
            String msg = "PUT method failed: Cannot connect to network.";
            Log.i(TAG, msg, e);
            throw new DeviceConnectionException(msg, e);
        }

        statusCode = resp.getStatusLine().getStatusCode();

        if ( Log.isLoggable( TAG, Log.INFO ) ) {
            Log.i( TAG, "HTTP statusCode[" + statusCode + "]" );
        }
        if ( statusCode == HttpStatus.SC_CREATED ) {

            try {
                respText = EntityUtils.toString(resp.getEntity());
            } catch ( IOException e ) {
                String msg = "POST method failed: Invalid response.";
                Log.e(TAG, msg, e);
                throw new WebServiceFailedException(msg, e);
            }

            try {
                jsonObject = new JSONObject(respText);
                Log.d(TAG, respText);
                detail = new Project(jsonObject);
                detail.setRequestId(requestId);
                detail.setHttpResult(statusCode);
//                detail.setTransDate(
//                        MyDateUtils.stringToDateForWS(
//                                jsonObject.getString(WebApiConstants.PARAM_DATE_UPDATED)));
                detail.setTransDate(new Date());
                Processor.getInstance().update(detail);
            } catch (JSONException e) {
                String msg =
                        "PUT method failed: Cannot parse data returned from web service.";
                Log.e(TAG, msg);
                throw new WebServiceFailedException(msg);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return statusCode;
    }

    /**
     * Delegate error handling to the Processor.
     */
    public boolean handleError( int httpResult, boolean allowRetry )
    {
        return Processor.getInstance()
                .requestFailure( this.requestId, httpResult, allowRetry );
    }

}