/*  Starter project for Mobile Platform Development - 1st diet 26/27
    STAGE A: downloads both Traffic Scotland feeds, parses them with XmlPullParser into Situation objects
    (ArrayLists held in the Activity), and lists every situation's type and generalPublicComment.
    Falls back to the last saved copy when offline.
*/

//
// Name                 Malcolm Smith
// Student ID           S2224512
// Programme of Study   Software Development (BSc)
//

// UPDATE THE PACKAGE NAME to include your Student Identifier
package org.me.gcu.smith_malcolm_s2224512;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View.OnClickListener;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Dialog pop-up imports
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.AdapterView;

//search
import android.widget.EditText;
import android.widget.Toast;

//fragments
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity implements OnClickListener, SearchFragment.SearchFragmentListener, SituationListFragment.SituationListListener{
    private TextView tvStatus;
    private Button startButton;
    private SearchFragment searchFragment;
    private SituationListFragment listFragment;

    //Search
    private ArrayList<Situation> shownSituations = new ArrayList<Situation>();
    private String searchRoad = "";

    //radius
    private float searchKm = 0;
    private final float campusLatitude = 55.8673f;
    private final float campusLongitude = -4.2504f;

    private final float kmPerDegreeLatitude = 111.2f;
    private final float kmPerDegreeLongitude = 62.0f;

    //DOWNLOADING FROM TRAFFIC SCOTLAND
    private String clientId = "";
    private String clientKey = "";
    final String unplannedEventsURL = "https://datex2.trafficscotland.org/rest/2.3/publications/UnplannedEvents/Content.xml";
    final String roadworksURL = "https://datex2.trafficscotland.org/rest/2.3/publications/CurrentRoadworks/Content.xml";
    final String unplannedSavedFile = "unplanned_saved.xml";
    final String roadworksSavedFile = "roadworks_saved.xml";

    // Parsed data
    private ArrayList<Situation> unplannedList = new ArrayList<Situation>();
    private ArrayList<Situation> roadworksList = new ArrayList<Situation>();
    private ArrayList<Situation> allSituations = new ArrayList<Situation>();
    private boolean unplannedLoaded = false;
    private boolean roadworksLoaded = false;
    private boolean unplannedOffline = false;
    private boolean roadworksOffline = false;

    private ArrayList<Situation> parsedList;

    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private boolean stopped = false;

    private final long refreshIntervalMillis = 3600000;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvStatus = (TextView)findViewById(R.id.tvStatus);
        startButton = (Button)findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        searchFragment = new SearchFragment();
        listFragment = new SituationListFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.searchHolder, searchFragment);
        transaction.replace(R.id.listHolder, listFragment);
        transaction.commit();

        try
        {
            Bundle metaData = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData;
            String idText = metaData.getString(getPackageName() + ".TRAFFIC_CLIENT_ID");
            String keyText = metaData.getString(getPackageName() + ".TRAFFIC_CLIENT_KEY");
            if (idText != null) { clientId = idText; }
            if (keyText != null) { clientKey = keyText; }
        }
        catch (PackageManager.NameNotFoundException e)
        {
            Log.e("MyTag", "Could not read the keys: " + e.toString());
        }

        startProgress();
        uiHandler.postDelayed(new RefreshTask(), refreshIntervalMillis);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        stopped = true;
    }
    public void onClick(View aview)
    {
        if (aview == startButton)
        {
            startProgress();
        }
    }


    @Override
    public void onRoadSearch(String typedRoad)
    {
        String typed = typedRoad.trim();
        if (typed.equals(""))
        {
            Toast.makeText(this, "Please enter a road name, e.g. A83", Toast.LENGTH_LONG).show();
        }
        else
        {
            searchRoad = typed;
            searchKm = 0;
            searchFragment.clearDistanceBox();
            applyFilter();
            if (shownSituations.size() == 0)
            {
                Toast.makeText(this, "No situations found on " + searchRoad, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDistanceSearch(String typedKm)
    {
        if (typedKm.equals(""))
        {
            Toast.makeText(this, "Please enter a distance in km, e.g. 10", Toast.LENGTH_LONG).show();
        }
        else
        {
            try
            {
                float km = Float.parseFloat(typedKm);
                if (km > 0)
                {
                    searchKm = km;
                    searchRoad = "";
                    searchFragment.clearRoadBox();
                    applyFilter();
                    if (shownSituations.size() == 0)
                    {
                        Toast.makeText(this, "No situations within " + String.valueOf(searchKm) + " km", Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Toast.makeText(this, "The distance must be more than 0", Toast.LENGTH_LONG).show();
                }
            }
            catch (NumberFormatException e)
            {
                Log.e("MyTag", "Distance is not a number: " + typedKm);
                Toast.makeText(this, "Please enter the distance as a number, e.g. 10", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onShowAll()
    {
        searchRoad = "";
        searchKm = 0;
        searchFragment.clearRoadBox();
        searchFragment.clearDistanceBox();
        applyFilter();
    }

    public void startProgress()
    {
        unplannedLoaded = false;
        roadworksLoaded = false;
        tvStatus.setText("Downloading traffic data...");
        executor.execute(new Task(unplannedEventsURL, Situation.TYPE_UNPLANNED, unplannedSavedFile));
        executor.execute(new Task(roadworksURL, Situation.TYPE_ROADWORKS, roadworksSavedFile));
    }

    private class RefreshTask implements Runnable
    {
        @Override
        public void run()
        {
            if (!stopped)
            {
                Log.d("MyTag", "Auto-update: refreshing the data");
                startProgress();
                uiHandler.postDelayed(new RefreshTask(), refreshIntervalMillis);
            }
        }
    }

    private class Task implements Runnable
    {
        private String result="";
        private String myurl;
        private int myFeedType;
        private String mySavedFile;
        BufferedReader in = null;
        String inputLine = "";
        public Task(String aurl, int aFeedType, String aSavedFile){
            myurl = aurl;
            myFeedType = aFeedType;
            mySavedFile = aSavedFile;
        }
        @Override
        public void run(){
            Log.d("MyTask","in run");
            try {
                URL url = new URL(myurl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                //ADD AUTHORISATION (string with clientIID:clientKey)
                urlConnection.setRequestProperty("Authorization",Base64.getEncoder().encodeToString((clientId + ":" + clientKey).getBytes()));
                try {
                    in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    while ((inputLine = in.readLine()) != null){
                        result = result + inputLine;
                    }
                    in.close();
                } catch (IOException e) {

                    Log.e("urlConnection", "ioexception");
                }
                finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                Log.e("URL", "ioexception");
            }

            //Clean up any leading garbage characters
            int i = result.indexOf("<?");
            boolean offline = false;
            if (i >= 0)
            {
                result = result.substring(i);
                saveCopy(mySavedFile, result);
            }
            else
            {
                String start = result;
                if (start.length() > 200) { start = start.substring(0, 200); }
                Log.e("MyTag", "No XML downloaded. Server sent: " + start);
                result = readSaved(mySavedFile);
                offline = true;
            }

            parsedList = new ArrayList<Situation>();
            if (result.length() > 0)
            {
                parseData(result, myFeedType);
            }

            final ArrayList<Situation> finished = parsedList;
            final boolean wasOffline = offline;
            uiHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    if (!stopped) { showResult(myFeedType, finished, wasOffline); }
                }
            });
        }

    }

    private void showResult(int feedType, ArrayList<Situation> list, boolean offline)
    {
        if (feedType == Situation.TYPE_UNPLANNED)
        {
            unplannedList = list;
            unplannedLoaded = true;
            unplannedOffline = offline;
        }
        else
        {
            roadworksList = list;
            roadworksLoaded = true;
            roadworksOffline = offline;
        }
        allSituations = new ArrayList<Situation>();
        for (Situation u : unplannedList)
        {
            allSituations.add(u);
        }
        for (Situation r : roadworksList)
        {
            allSituations.add(r);
        }
        applyFilter();

        String unplannedText = "...";
        String roadworksText = "...";
        if (unplannedLoaded) { unplannedText = String.valueOf(unplannedList.size()); }
        if (roadworksLoaded) { roadworksText = String.valueOf(roadworksList.size()); }
        String status = "Incidents: " + unplannedText + "   Roadworks: " + roadworksText;
        if (unplannedOffline || roadworksOffline)
        {
            status = status + "   (OFFLINE - saved copy used)";
        }
        if (unplannedLoaded && roadworksLoaded && allSituations.size() == 0)
        {
            status = "No data: could not download it and there is no saved copy.";
        }
        tvStatus.setText(status);
    }

    private void applyFilter()
    {
        shownSituations = new ArrayList<Situation>();
        for (Situation s : allSituations)
        {
            if (searchRoad.equals("") && searchKm == 0)
            {
                shownSituations.add(s);
            }
            else if (!searchRoad.equals(""))
            {
                boolean found = false;
                for (String road : s.getRoads())
                {
                    if (road.equalsIgnoreCase(searchRoad))
                    {
                        found = true;
                    }
                    else if (road.length() > searchRoad.length())
                    {
                        String start = road.substring(0, searchRoad.length());
                        String next = road.substring(searchRoad.length(), searchRoad.length() + 1);
                        if (start.equalsIgnoreCase(searchRoad) && next.equals("("))
                        {
                            found = true;
                        }
                    }
                }
                if (found)
                {
                    shownSituations.add(s);
                }
            }
            else
            {
                if (isNear(s))
                {
                    shownSituations.add(s);
                }
            }
        }
        showList();
        if (!searchRoad.equals(""))
        {
            searchFragment.setInfo("Road " + searchRoad + ": " + String.valueOf(shownSituations.size()) + " found");
        }
        else if (searchKm != 0)
        {
            searchFragment.setInfo("Within " + String.valueOf(searchKm) + " km of GCU: " + String.valueOf(shownSituations.size()) + " found");
        }
        else
        {
            searchFragment.setInfo("Showing all " + String.valueOf(shownSituations.size()) + " situations");
        }
    }

    private boolean isNear(Situation s)
    {
        float limit = searchKm * searchKm;
        boolean near = false;
        if (s.hasFromPoint())
        {
            if (distanceSquared(s.getFromLatitude(), s.getFromLongitude()) <= limit)
            {
                near = true;
            }
        }
        if (s.hasToPoint())
        {
            if (distanceSquared(s.getToLatitude(), s.getToLongitude()) <= limit)
            {
                near = true;
            }
        }
        return near;
    }

    private float distanceSquared(float latitude, float longitude)
    {
        float northSouth = (latitude - campusLatitude) * kmPerDegreeLatitude;
        float eastWest = (longitude - campusLongitude) * kmPerDegreeLongitude;
        return northSouth * northSouth + eastWest * eastWest;
    }

    private void showList()
    {
        String[] values = new String[shownSituations.size()];
        int[] icons = new int[shownSituations.size()];
        int[] colours = new int[shownSituations.size()];
        int i = 0;
        for (Situation s : shownSituations)
        {
            if (s.getFeedType() == Situation.TYPE_ROADWORKS)
            {
                values[i] = "Roadworks: " + s.getComment();
            }
            else
            {
                values[i] = "Incident (" + s.getSubType() + "): " + s.getComment();
            }
            icons[i] = R.mipmap.ic_launcher;
            colours[i] = getDelayColour(s);
            i++;
        }
        listFragment.setRows(values, icons, colours);
    }

    private int getDelayColour(Situation s)
    {
        int colour = getResources().getColor(R.color.white);
        if (s.getFeedType() == Situation.TYPE_ROADWORKS)
        {
            if (s.getDelaysType().equals("delays"))
            {
                colour = getResources().getColor(android.R.color.holo_orange_light);
            }
            else if (s.getDelaysType().equals("longDelays"))
            {
                colour = getResources().getColor(android.R.color.holo_orange_dark);
            }
            else if (s.getDelaysType().equals("veryLongDelays"))
            {
                colour = getResources().getColor(android.R.color.holo_red_light);
            }
        }
        return colour;
    }

    // Called when a row is tapped
    @Override
    public void onSituationSelected(int position)
    {
        Situation selected = null;
        int i = 0;
        for (Situation s : shownSituations)
        {
            if (i == position)
            {
                selected = s;
            }
            i++;
        }
        if (selected != null)
        {
            showDetailDialog(selected);
        }
    }

    private void showDetailDialog(Situation s)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (s.getFeedType() == Situation.TYPE_ROADWORKS)
        {
            builder.setTitle("Roadworks");
        }
        else
        {
            builder.setTitle("Incident");
        }
        builder.setMessage(buildDetailText(s));
        builder.setCancelable(false);
        builder.setPositiveButton("Close", closeButtonListener);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private DialogInterface.OnClickListener closeButtonListener = new DialogInterface.OnClickListener ()
    {
        public void onClick(DialogInterface dialog, int id)
        {
            dialog.cancel();
        }
    };

    private String buildDetailText(Situation s)
    {
        String text = s.getComment() + "\n\n";

        if (s.getFeedType() == Situation.TYPE_ROADWORKS)
        {
            text = text + "Type: Roadworks\n";
        }
        else
        {
            text = text + "Type: Incident\n";
        }
        if (!s.getSubType().equals(""))
        {
            text = text + "Kind: " + s.getSubType() + "\n";
        }
        if (!s.getDelaysType().equals(""))
        {
            text = text + "Delays: " + s.getDelaysType() + "\n";
        }
        if (s.isUrgentRoadworks())
        {
            text = text + "Urgent roadworks: yes\n";
        }
        if (s.getOperationalLanes() >= 0)
        {
            text = text + "Lanes open: " + String.valueOf(s.getOperationalLanes()) + "\n";
        }
        if (s.getLanesRestricted() >= 0)
        {
            text = text + "Lanes restricted: " + String.valueOf(s.getLanesRestricted()) + "\n";
        }
        if (!s.getDirection().equals(""))
        {
            text = text + "Direction: " + s.getDirection() + "\n";
        }
        if (!s.getProbability().equals(""))
        {
            text = text + "Probability: " + s.getProbability() + "\n";
        }

        text = text + "\n";
        if (!s.getStartTime().equals(""))
        {
            text = text + "Starts: " + tidyTime(s.getStartTime()) + "\n";
        }
        if (!s.getEndTime().equals(""))
        {
            text = text + "Ends: " + tidyTime(s.getEndTime()) + "\n";
        }
        if (!s.getVersionTime().equals(""))
        {
            text = text + "Last updated: " + tidyTime(s.getVersionTime()) + "\n";
        }

        text = text + "\n";
        String roadText = "";
        for (String road : s.getRoads())
        {
            if (!roadText.equals(""))
            {
                roadText = roadText + ", ";
            }
            roadText = roadText + road;
        }
        if (!roadText.equals(""))
        {
            text = text + "Road: " + roadText + "\n";
        }
        if (!s.getFromDescription().equals(""))
        {
            text = text + "From: " + s.getFromDescription() + "\n";
        }
        if (!s.getToDescription().equals(""))
        {
            text = text + "To: " + s.getToDescription() + "\n";
        }
        if (s.hasFromPoint())
        {
            text = text + "From point: " + String.valueOf(s.getFromLatitude()) + ", " + String.valueOf(s.getFromLongitude()) + "\n";
        }
        if (s.hasToPoint())
        {
            text = text + "To point: " + String.valueOf(s.getToLatitude()) + ", " + String.valueOf(s.getToLongitude()) + "\n";
        }
        return text;
    }
    //Time formatting from 2026-08-31T11:30:00+01:00 to 2026-08-31 11:30
    private String tidyTime(String t)
    {
        if (t.length() >= 16)
        {
            return t.substring(0, 10) + " " + t.substring(11, 16);
        }
        return t;
    }

    // ------------------------------saved copy------------------------------------

    private void saveCopy(String fileName, String text)
    {
        try
        {
            File dir = getApplication().getFilesDir();
            File f = new File(dir, fileName);
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(text.getBytes());
            fos.close();
        }
        catch (IOException e)
        {
            Log.e("MyTag", "Could not save a copy: " + e.toString());
        }
    }

    private String readSaved(String fileName)
    {
        String saved = "";
        String line;
        File dir = getApplication().getFilesDir();
        File f = new File(dir, fileName);
        if (!f.exists())
        {
            return "";
        }
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            while ((line = reader.readLine()) != null)
            {
                saved = saved + line;
            }
            reader.close();
        }
        catch (IOException e)
        {
            Log.e("MyTag", "Could not read the saved copy: " + e.toString());
            return "";
        }
        return saved;
    }

    // ------------------------------parsing------------------------------------

    private void parseData(String dataToParse, int feedType)
    {
        Situation aSituation = null;
        try
        {
            boolean insideASituation = false;
            boolean inComment = false;
            boolean inFrom = false;
            boolean inTo = false;
            boolean inLocName = false;
            String locElement = "";
            String pendingValue = null;
            String pendingType = null;
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput( new StringReader( dataToParse ) );
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT)
            {
                if(eventType == XmlPullParser.START_TAG)
                {
                    if (xpp.getName().equalsIgnoreCase("situation"))
                    {
                        insideASituation = true;
                        aSituation = new Situation();
                        aSituation.setFeedType(feedType);
                        Log.d("MyTag","New Situation found!");
                    }
                    else if (insideASituation)
                    {
                        if (xpp.getName().equalsIgnoreCase("situationRecord"))
                        {
                            if (xpp.getAttributeCount() > 0) { aSituation.setRecordType(xpp.getAttributeValue(0)); }
                            if (xpp.getAttributeCount() > 1) { aSituation.setSituationId(xpp.getAttributeValue(1)); }
                            Log.d("MyTag","Record id is " + aSituation.getSituationId());
                        }
                        else if (xpp.getName().equalsIgnoreCase("situationRecordCreationTime"))
                        {
                            String temp = xpp.nextText();
                            aSituation.setCreationTime(temp);
                        }
                        else if (xpp.getName().equalsIgnoreCase("situationRecordVersionTime"))
                        {
                            String temp = xpp.nextText();
                            aSituation.setVersionTime(temp);
                        }
                        else if (xpp.getName().equalsIgnoreCase("probabilityOfOccurrence"))
                        {
                            String temp = xpp.nextText();
                            aSituation.setProbability(temp);
                        }
                        else if (xpp.getName().equalsIgnoreCase("overallStartTime"))
                        {
                            String temp = xpp.nextText();
                            aSituation.setStartTime(temp);
                            Log.d("MyTag","Start time is " + temp);
                        }
                        else if (xpp.getName().equalsIgnoreCase("overallEndTime"))
                        {
                            String temp = xpp.nextText();
                            aSituation.setEndTime(temp);
                            Log.d("MyTag","End time is " + temp);
                        }
                        else if (xpp.getName().equalsIgnoreCase("delaysType"))
                        {
                            String temp = xpp.nextText();
                            aSituation.setDelaysType(temp);
                            Log.d("MyTag","Delays type is " + temp);
                        }
                        else if (xpp.getName().equalsIgnoreCase("numberOfOperationalLanes"))
                        {
                            String temp = xpp.nextText();
                            aSituation.setOperationalLanes(temp);
                        }
                        else if (xpp.getName().equalsIgnoreCase("numberOfLanesRestricted"))
                        {
                            String temp = xpp.nextText();
                            aSituation.setLanesRestricted(temp);
                        }
                        else if (xpp.getName().equalsIgnoreCase("urgentRoadworks"))
                        {
                            String temp = xpp.nextText();
                            aSituation.setUrgentRoadworks(temp.equalsIgnoreCase("true"));
                        }
                        else if (xpp.getName().equalsIgnoreCase("roadMaintenanceType")
                                || xpp.getName().equalsIgnoreCase("roadOrCarriagewayOrLaneManagementType")
                                || xpp.getName().equalsIgnoreCase("abnormalTrafficType"))
                        {
                            String temp = xpp.nextText();
                            aSituation.setSubType(temp);
                        }
                        else if (xpp.getName().equalsIgnoreCase("tpegDirection"))
                        {
                            String temp = xpp.nextText();
                            aSituation.setDirection(temp);
                        }
                        else if (xpp.getName().equalsIgnoreCase("generalPublicComment"))
                        {
                            inComment = true;
                        }
                        else if (xpp.getName().equalsIgnoreCase("to"))
                        {
                            inTo = true;
                            inFrom = false;
                        }
                        else if (xpp.getName().equalsIgnoreCase("from"))
                        {
                            inFrom = true;
                            inTo = false;
                        }
                        else if (xpp.getName().equalsIgnoreCase("latitude") && (inTo || inFrom))
                        {
                            String temp = xpp.nextText();
                            if (inTo) { aSituation.setToLatitude(temp); } else { aSituation.setFromLatitude(temp); }
                        }
                        else if (xpp.getName().equalsIgnoreCase("longitude") && (inTo || inFrom))
                        {
                            String temp = xpp.nextText();
                            if (inTo) { aSituation.setToLongitude(temp); } else { aSituation.setFromLongitude(temp); }
                        }
                        else if ((inTo || inFrom) && (xpp.getName().equalsIgnoreCase("name")
                                || xpp.getName().equalsIgnoreCase("ilc")
                                || xpp.getName().equalsIgnoreCase("otherName")))
                        {
                            inLocName = true;
                            locElement = xpp.getName();
                            pendingValue = null;
                            pendingType = null;
                        }
                        else if (xpp.getName().equalsIgnoreCase("value") && inComment)
                        {
                            String temp = xpp.nextText();
                            aSituation.setComment(temp);
                            Log.d("MyTag","Comment is " + temp);
                        }
                        else if (xpp.getName().equalsIgnoreCase("value") && inLocName)
                        {
                            pendingValue = xpp.nextText();
                        }
                        else if (inLocName && (xpp.getName().equalsIgnoreCase("tpegOtherPointDescriptorType")
                                || xpp.getName().equalsIgnoreCase("tpegIlcPointDescriptorType")
                                || xpp.getName().equalsIgnoreCase("tpegJunctionPointDescriptorType")))
                        {
                            pendingType = xpp.nextText();
                        }
                    }
                }
                else if(eventType == XmlPullParser.END_TAG)
                {
                    if (xpp.getName().equalsIgnoreCase("situation"))
                    {
                        parsedList.add(aSituation);
                        insideASituation = false;
                        inComment = false;
                        inFrom = false;
                        inTo = false;
                        inLocName = false;
                        Log.d("MyTag","Situation parsing completed! " + aSituation.toString());
                    }
                    else if (insideASituation)
                    {
                        if (xpp.getName().equalsIgnoreCase("generalPublicComment")) { inComment = false; }
                        else if (xpp.getName().equalsIgnoreCase("to")) { inTo = false; }
                        else if (xpp.getName().equalsIgnoreCase("from")) { inFrom = false; }
                        else if (inLocName && xpp.getName().equalsIgnoreCase(locElement))
                        {
                            storeLocationName(aSituation, inFrom, locElement, pendingType, pendingValue);
                            inLocName = false;
                        }
                    }
                }
                eventType = xpp.next();
            }
        }
        catch (XmlPullParserException ae1)
        {
            Log.e("MyTag","Parsing error" + ae1.toString());
        }
        catch (IOException ae1)
        {
            Log.e("MyTag","IO error during parsing");
        }

        Log.d("MyTag","End of document reached");
    }

    private void storeLocationName(Situation s, boolean isFrom, String element, String type, String value)
    {
        if (value == null || value.equals("")) { return; }
        if (element.equalsIgnoreCase("ilc"))
        {
            s.addRoad(value);
        }
        else if (type != null && type.equalsIgnoreCase("linkName"))
        {
            s.addRoad(value);
        }
        else if (type != null && (type.equalsIgnoreCase("intersectionName") || type.equalsIgnoreCase("nonLinkedPointName")))
        {
            if (isFrom) { s.setFromDescription(value); } else { s.setToDescription(value); }
        }
        else if (type != null && type.equalsIgnoreCase("junctionName"))
        {
            if (isFrom && s.getFromDescription().equals("")) { s.setFromDescription(value); }
            if (!isFrom && s.getToDescription().equals("")) { s.setToDescription(value); }
        }
    }

}
