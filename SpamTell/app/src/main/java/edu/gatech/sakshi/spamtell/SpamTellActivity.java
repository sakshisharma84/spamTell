package edu.gatech.sakshi.spamtell;

import android.app.Activity;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ch.boye.httpclientandroidlib.entity.mime.content.FileBody;
import ch.boye.httpclientandroidlib.entity.mime.content.StringBody;

public class SpamTellActivity extends Activity {

    final String DTAG = "SPAMTELL_ACT";
    Button mButton;
    CheckBox mCheckBox;

    Boolean mSendRecordingNow = true;
    String mRecid = "";
    RequestQueue mReporter = null;
    TelephonyManager telManager;
    private PhoneStateListener mPhoneListener = null;

    Boolean enableRecording = true;
    MediaRecorder recorder;
    String selected_song_name;
    Time today = new Time(Time.getCurrentTimezone());
    boolean recordStarted = false;

    // Data to make url
    String phnu;
    String toc;
    String doc;
    Date sDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spam_tell);

        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String murl = "http://130.207.5.67:8080/report?phnu="+ Uri.encode(phnu + "&doc=" + doc + "&toc=" + toc);
                // FIXME: fetch data from call report and add to murl
                report_spam(murl);
                if(true)
                {
                    String rurl = "http://130.207.5.67:8080/recording?recid="+Uri.encode(mRecid);
                    // FIXME: send_recording(murl) using
                    send_recording(rurl);
                }
            }
        });

        mCheckBox = (CheckBox) findViewById(R.id.checkBox);
        mCheckBox.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                // Set/reset flag to send recording  based on its state
                boolean checked = ((CheckBox) v).isChecked();
                if(checked) {
                    mSendRecordingNow = true;
                }
            }

        });

        today.setToNow();

        mPhoneListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                try {
                    switch (state) {
                        case TelephonyManager.CALL_STATE_RINGING: {
                            Log.d(DTAG, "Phone state listener: CALL_STATE_RINGING: " + state + " Incoming From: "+incomingNumber);
                            phnu = incomingNumber;
                            break;
                        }
                        case TelephonyManager.CALL_STATE_OFFHOOK: {
                            Log.d(DTAG, "Phone state listener: CALL_STATE_OFFHOOK: " + state);

                            String date = today.year + "-" + today.month + "-" + today.monthDay;
                            String time = today.format("%k:%M:%S");
                            toc = date+time;

                            sDate = new Date();
                            setup_recording();
                            break;
                        }
                        case TelephonyManager.CALL_STATE_IDLE: {
                            Log.d(DTAG, "Phone state listener: CALL_STATE_IDLE: " + state);
                            doc = String.valueOf( (new Date()).getTime() - sDate.getTime());
                            teardown_recorder();
                            break;
                        }
                        default: {
                        }
                    }
                } catch (Exception ex) {
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };

        telManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telManager.listen(mPhoneListener,PhoneStateListener.LISTEN_CALL_STATE);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_spam_tell, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
/*    public class MultipartRequest extends Request<String> {

        private MultipartEntity entity = new MultipartEntity();

        private static final String FILE_PART_NAME = "file";
        private static final String STRING_PART_NAME = "text";

        private final Response.Listener<String> mListener;
        private final File mFilePart;
        private final String mStringPart;

        public MultipartRequest(String url, Response.ErrorListener errorListener, Response.Listener<String> listener, File file, String stringPart)
        {
            super(Method.POST, url, errorListener);

            mListener = listener;
            mFilePart = file;
            mStringPart = stringPart;
            buildMultipartEntity();
        }

        private void buildMultipartEntity()
        {
            entity.addPart(FILE_PART_NAME, new FileBody(mFilePart));
            try
            {
                entity.addPart(STRING_PART_NAME, new StringBody(mStringPart));
            }
            catch (UnsupportedEncodingException e)
            {
                VolleyLog.e("UnsupportedEncodingException");
            }
        }

        @Override
        public String getBodyContentType()
        {
            return entity.getContentType().getValue();
        }

        @Override
        public byte[] getBody() throws AuthFailureError
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try
            {
                entity.writeTo(bos);
            }
            catch (IOException e)
            {
                VolleyLog.e("IOException writing to ByteArrayOutputStream");
            }
            return bos.toByteArray();
        }

        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response)
        {
            return Response.success("Uploaded", getCacheEntry());
        }

        @Override
        protected void deliverResponse(String response)
        {
            mListener.onResponse(response);
        }
     }*/
    public void send_recording(String murl) {

        if(mReporter == null)
            mReporter = Volley.newRequestQueue(this);
        if(mRecid != "") {
            Log.d(DTAG,"Sending recording URL(post): "+murl);
            StringRequest sr = new StringRequest(Request.Method.POST, murl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // Display the first 500 characters of the response string.
                    Log.d(DTAG, "Post response is: " + response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(DTAG, "Post didn't work! " +error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    String rec = null;
                    //FIXME: Read from the file and stream the file here
                    try {
                        rec = getAudioFileFromSdcard();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    //params.put("recid", mRecid);
                    params.put("recording", rec);
                    Log.d(DTAG,"Created post body: "+mRecid);
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/x-www-form-urlencoded");
                    return params;
                }
            };
            mReporter.add(sr);
            mRecid = null;
        }
    }

    public void report_spam(String murl) {
        String ret = null;

        if(mReporter == null)
            mReporter = Volley.newRequestQueue(this);

        Log.d(DTAG,"ReportingSpam URL: "+murl);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, murl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d(DTAG, "Get Response is: " + response.toString());
                        // set recid here
                        mRecid = response.toString();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(DTAG,"Get didn't work - " + error);
            }
        });
        // Add the request to the RequestQueue.
        mReporter.add(stringRequest);
    }

    private void setup_recording()
    {
        if (enableRecording == true) {
            try {
                recorder = new MediaRecorder();
                recorder.reset();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                String date = today.monthDay + "_" + (today.month + 1) + "_" + today.year;
                String time = today.format("%k_%M_%S");

                Log.d(DTAG, "Creating a file - 2: " + "Out" + date + "_" + time);
                File file = createDirIfNotExists("Out" + "_" + date + "_" + time);
                Log.d(DTAG, "File absolute path - 2: " + file.getAbsolutePath());

                recorder.setOutputFile(file.getAbsolutePath());
                recorder.prepare();
                recorder.start();
                recordStarted = true;
                Log.d(DTAG, "Recording started - 2");
            } catch (Exception ex) {
                Log.d(DTAG, "Exception in recording - 2");
                ex.printStackTrace();
            }
        }
    }

    private File createDirIfNotExists(String path) {
        selected_song_name = path;
        File folder = new File(Environment.getExternalStorageDirectory()
                + "/PhoneCallRecording");

        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                Log.d(DTAG, "Error creating folder");
            }
        }

        File file = new File(folder, path + ".3GPP");
        try {
            if (!file.exists()) {
                if (file.createNewFile()) {
                    Log.d(DTAG, "Error creating file");
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return file;

    }
    private String getAudioFileFromSdcard() throws FileNotFoundException {
        byte[] inarry = null;
        try {
            File sdcard = new File(Environment.getExternalStorageDirectory()+ "/PhoneCallRecording");
            File file = new File(sdcard, selected_song_name + ".3GPP");
            FileInputStream fileInputStream = null;
            byte[] bFile = new byte[(int) file.length()];
            // convert file into array of bytes
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();
            inarry = bFile;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Base64.encodeToString(inarry, Base64.DEFAULT);
    }
    private void teardown_recorder(){
        if (enableRecording == true && recordStarted == true) {
            recorder.stop();
            recorder.reset();
            recorder.release();
            recorder = null;
            recordStarted = false;
        }
    }
}
