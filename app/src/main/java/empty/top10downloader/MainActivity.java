package empty.top10downloader;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String FEED_LIMIT = "FeedLimit";
    private static final String FEED_URL = "FeedURL";
    String urlCopy = "Not a URL";
    private ListView listApps;
    private String feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/%d/xml"; // %d integer value that will be replaced
    private int feedLimit = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listApps = (ListView) findViewById(R.id.xmlListView);

        //when creating again(rotating screen). When creating for first time, bundle will be null
        if (savedInstanceState != null) {
            feedUrl = savedInstanceState.getString(FEED_URL);
            feedLimit = savedInstanceState.getInt(FEED_LIMIT);

        }

        downloadUrl(String.format(feedUrl, feedLimit)); // (String to use, integer value to replace %d)


    }

    //called when its time to inflate the activity\s menu. create the menus object from xml file
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feeds_menu, menu);
        //for when app is rotated (destroys and recreates activity- we can just refer to the feedLimit to recheck the limit
        if (feedLimit == 10) {
            menu.findItem(R.id.mnu10).setChecked(true);
        } else {
            menu.findItem(R.id.mnu25).setChecked(true);
        }
        return true; // to tell android we actually created/inflated a menu
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

          switch (id) {
            case R.id.mnuFree:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
                break;
            case R.id.mnuPaid:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml";
                break;
            case R.id.mnuSongs:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml";
                break;
            case R.id.mnu10:
            case R.id.mnu25:
                if (!item.isChecked()) {
                    item.setChecked(true);
                    feedLimit = 35 - feedLimit;
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + " setting feedLimit to " + feedLimit);
                } else {
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + " feedLimit unchanged");
                }
                break;
            case R.id.mnuRefresh:
                urlCopy = "not the same url"; // allows the download method to run since URLS are not the same
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        downloadUrl(String.format(feedUrl, feedLimit));
        return true;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(FEED_URL, feedUrl);
        outState.putInt(FEED_LIMIT, feedLimit);

        super.onSaveInstanceState(outState);
    }


    private void downloadUrl(String feedUrl) {

        if (!feedUrl.equalsIgnoreCase(urlCopy)) {
            Log.d(TAG, "downloadUrl:starting Asynctask");
            DownloadData downloadData = new DownloadData();
            downloadData.execute(feedUrl); // execute method to start AsyncTask. // URL goes here
            Log.d(TAG, "downloadUrl: done");
            urlCopy = feedUrl;
        } else {
            Log.d(TAG, "downloadUrl: URL is the same");
        }

    }


    //create inner class for async task AsyncTask<Type String/URL, displayProgress Bar, Type of result we want to get back>
    private class DownloadData extends AsyncTask<String, Void, String> {
        private static final String TAG = "DownloadData";

        //parameter passed through onPostExecute(String s) is the return value from doInBackground
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Log.d(TAG, "onPostExecute: parameter is " + s);

            ParseApplications parseApplications = new ParseApplications();
            parseApplications.parse(s);

//            ArrayAdapter<FeedEntry> arrayAdapter = new ArrayAdapter<FeedEntry>(MainActivity.this, R.layout.list_item, parseApplications.getApplications());
//            listApps.setAdapter(arrayAdapter); //link list view to adapter

            //<FeedEntry> generics
            FeedAdapter<FeedEntry> feedAdapter = new FeedAdapter<>(MainActivity.this, R.layout.list_record, parseApplications.getApplications());
            listApps.setAdapter(feedAdapter);
        }


        //can pass multiple types as an array etc strings using "..."
        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: starts with " + strings[0]);
            String rssFeed = downloadXML(strings[0]);
            if (rssFeed == null) {
                Log.e(TAG, "doInBackground: Error downloading");
            }
            return rssFeed;
        }


        private String downloadXML(String urlPath) {
            //using string builder because we\re going to be appending to strings a lot from the stream
            StringBuilder xmlResult = new StringBuilder();

            //use a try block because there can be a lot of problems from pulling info from external source.. etc donlowading drops/ bad connections
            try {
                URL url = new URL(urlPath); //unhandled exception - no guaranteed url is valid, catch with the catch block
                HttpURLConnection connection = (HttpURLConnection) url.openConnection(); //unhandled exception - io exception
                int response = connection.getResponseCode(); // io exception
                Log.d(TAG, "downloadXML: The response code was " + response);
//                InputStream inputStream = connection.getInputStream(); //io exception
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
//                BufferedReader reader = new BufferedReader(inputStreamReader); //buffered inputsream reader. buffered reader used to read the xml
                //closing the buffered reader closes the input stream reader, which closes the input stream
                //use this instead of writing the three lines of code above
                //buffered reader reads characters not strings
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                int charsRead;
                char[] inputBuffer = new char[500];
                while (true) {
                    charsRead = reader.read(inputBuffer);
                    if (charsRead < 0) { //end of stream of data
                        break;
                    }
                    if (charsRead > 0) {
                        xmlResult.append(String.copyValueOf(inputBuffer, 0, charsRead));
                    }
                }
                reader.close(); //closing the buffered reader also closes the other IO objects
                return xmlResult.toString();
            } catch (MalformedURLException e) {
                Log.e(TAG, "downloadXML: Invalid URL " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "downloadXML: IO Exception reading data: " + e.getMessage());
            } catch (SecurityException e) {
                Log.e(TAG, "downloadXML: Security Exception. Needs permission? " + e.getMessage());
                //e.printStackTrace(); // print the stack trace for the actually exception in this catch block
            }
            return null; // if we catch an exception
        }

    }


}
