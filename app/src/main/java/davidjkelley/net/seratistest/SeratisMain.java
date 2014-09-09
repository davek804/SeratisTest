package davidjkelley.net.seratistest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public class SeratisMain extends Activity {

    JSONArray providersJsonArray;
    JSONArray patientsJsonArray;
    JSONArray joinJSONArray;

    //Use these lengths twice in very different locations.
    int providersLength;
    int patientsLength;
    int joinLength;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seratis_main);

        processJSON();
        setupGUIElements();
    }

    public void processJSON() {
        /*
        I did not implement the JSON as endpoints from REST - setting up an HTTP client as well as parsing the
        JSON response (from an nonexistent web-service, but instead simply some string fields) didn't seem like it would
        serve a large purpose. Perhaps the Asynchronous aspect of requesting and then receiving the constituent JSON would
        have been a valid exercise, but having whiteboarded that, I'm already aware that Seratis would not be implementing the
        solution I would have used, so we can delve deeper into this later!
         */
        String providersNonJson = "{'providers':[{'id': 1,'name':'Perry Cox'},{'id': 2,'name': 'John Dorian'},{'id': 3,'name':'Christopher Turk'}]}";
        String patientsNonJson = "{'patients': [{'id': 1,'name': 'Eric Cartman'},{'id': 2,'name': 'Stan Marsh'}]}";
        String joinNonJson = "{'joinmap':[{'id':1,'providerId':'1','patientIds':'1,2'},{'id':2,'providerId':'2','patientIds':'1'},{'id':3,'providerId':'3','patientIds':''}]}";
        try {
            JSONObject patientsJSON = patientsJSON = new JSONObject(patientsNonJson);
            JSONObject providersJSON = new JSONObject(providersNonJson);
            JSONObject joinJSONObject = new JSONObject(joinNonJson);

            /*Without these little adaptations of the JSOn in arrays, I found that dealing with the objects was onerous at best
            */
            providersJsonArray = providersJSON.getJSONArray("providers");
            patientsJsonArray = patientsJSON.getJSONArray("patients");
            joinJSONArray = joinJSONObject.getJSONArray("joinmap");

            /*
            I was torn on using these variables as fields... they were unnecessary in the 'Provider' implementation, but proved very useful in my implementation
            of the 'Patient' solution, so I was ultimately happy to leave them as fields.
             */
            providersLength = providersJsonArray.length();
            patientsLength = patientsJsonArray.length();
            joinLength = joinJSONArray.length();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setupGUIElements() {
        //These arrays only need to reside in this method... they will not be used outside of the ArrayAdapters.
        ArrayList<String> providers = new ArrayList<String>();
        int count = 0;
        while (count < providersLength) {
            try {
                providers.add(providersJsonArray.getJSONObject(count).getString("name"));
                count++;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        ArrayList<String> patients = new ArrayList<String>();
        count = 0;
        while (count < patientsLength) {
            try {
                patients.add(patientsJsonArray.getJSONObject(count).getString("name"));
                count++;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //Yes, these will be passed to the listener, but the main activity doesn't really need them as members.
        ListView patientListView;
        ListView providerListView;

        patientListView = (ListView) findViewById(R.id.patientList);
        providerListView = (ListView) findViewById(R.id.providerList);

        //Make some pretty headers that are for usability.
        View patientHeader = View.inflate(this, R.layout.patient_header, null);
        View providerHeader = View.inflate(this, R.layout.provider_header, null);
        TextView patientTitle = (TextView) findViewById(R.id.patientTitle);
        TextView providerTitle = (TextView) findViewById(R.id.providerTitle);
        patientListView.addHeaderView(patientHeader);
        providerListView.addHeaderView(providerHeader);
        //Set ListView tags to be used later for dynamism.
        patientListView.setTag("Patient");
        providerListView.setTag("Provider");

        providerListView.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, providers));
        patientListView.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, patients));

        providerListView.setOnItemClickListener(new ListListener(providerListView, this));
        patientListView.setOnItemClickListener(new ListListener(patientListView, this));
    }


    public class ListListener implements AdapterView.OnItemClickListener {

        ListView dynamicListView;
        SeratisMain activity;

        public ListListener(ListView lv, SeratisMain activity) {
            dynamicListView = lv;
            this.activity = activity;
        }

        public void onItemClick(AdapterView<?> parent, final View view,
                                int position, long id) {
            AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
            int jsonAppropriateListId = position - 1; //Have to move the counter back by one to match the JSON and lists
            String message = "";
            if (position > 0) {//Simple test to not do any work on the Header Row.

                if (dynamicListView.getTag().toString().equals("Provider")) {
                //Provider Work
                    try {
                        alertDialog.setTitle(dynamicListView.getTag().toString() + " " +
                                providersJsonArray.getJSONObject(jsonAppropriateListId).get("name").toString());

                        for (int j = 0; j < joinJSONArray.length(); j++) {
                            int compareId = Integer.parseInt(joinJSONArray.getJSONObject(j).get("providerId").toString());
                            if (position == compareId) {
                                ArrayList<String> namesList = new ArrayList<String>();
                                String namesAsIds = joinJSONArray.getJSONObject(j).get("patientIds").toString();
                                String[] tempNamesArray = namesAsIds.split(",");
                                int tempNamesArrayLocation = 0;
                                for (String s : tempNamesArray) {
                                    if (j <= tempNamesArray.length) {
                                        int nameId = Integer.parseInt(patientsJsonArray.getJSONObject(tempNamesArrayLocation).get("id").toString());
                                        if (Integer.parseInt(s) == nameId) {
                                            message += patientsJsonArray.getJSONObject(tempNamesArrayLocation).get("name").toString() + "\n";
                                        }
                                    }
                                    tempNamesArrayLocation++;
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                //Patient Work
                    try {
                        alertDialog.setTitle(dynamicListView.getTag().toString() + " " +
                                patientsJsonArray.getJSONObject(jsonAppropriateListId).getString("name"));
                        //Do the work to produce many:many list for patients.
                        HashMap<String, String> uniqueList = new HashMap<String, String>();
                        ArrayList<String> uniquerList = new ArrayList<String>();
                        String[] thisCountsPatientsList = new String[joinLength];
                        String[] splitPatientsForThisJoinEntry;
                        for (int j = 0; j < joinLength; j++) {
                            thisCountsPatientsList[j] = joinJSONArray.getJSONObject(j).getString("patientIds");
                            splitPatientsForThisJoinEntry = thisCountsPatientsList[j].split(",");
                            for (String s : splitPatientsForThisJoinEntry) {//For each item in the current joinList, check to see if its ID matches the ID of the selected ListItem (patient)
                                if (s.length() > 0) {
                                    int superSeriouslyImportantNumber = Integer.parseInt(s);
                                    if (superSeriouslyImportantNumber == position) {
                                        //uniqueList.put("id", joinJSONArray.getJSONObject(superSeriouslyImportantNumber).getString("providerId"));
                                        uniquerList.add(providersJsonArray.getJSONObject(j).getString("name"));
                                    }
                                }
                            }
                        }
                        for (String s : uniquerList) {
                            message += s + "\n";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //If something has been set for the message, ie. providers or patients
                if (message.length() > 0) {
                    alertDialog.setMessage(message);
                } else {//Otherwise put a nice message in there...
                    alertDialog.setMessage("None");
                }
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertDialog.show();
            } else {//Just catch the scenario where the user clicks on a header row (bad user!) and warn them.
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, "No info for header..", duration);
                toast.show();
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.seratis_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}