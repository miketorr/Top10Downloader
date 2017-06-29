package empty.top10downloader;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ListView listApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listApps = (ListView) findViewById(R.id.xmlListView);

        Log.d(TAG, "onCreate:starting Asynctask");
        DownloadData downloadData = new DownloadData();
        downloadData.execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml"); // execute method to start AsyncTask. // URL goes here
        Log.d(TAG, "onCreate: done");

    }


    //create inner class for async task AsyncTask<Type String/URL, displayProgress Bar, Type of result we want to get back>
    private class DownloadData extends AsyncTask<String, Void, String> {
        private static final String TAG = "DownloadData";

        //parameter passed through onPostExecute(String s) is the return value from doInBackground
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: parameter is " + s);

            ParseApplications parseApplications = new ParseApplications();
            parseApplications.parse(s);

            ArrayAdapter<FeedEntry> arrayAdapter = new ArrayAdapter<FeedEntry>(MainActivity.this, R.layout.list_item, parseApplications.getApplications());
            listApps.setAdapter(arrayAdapter);

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
