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
import java.util.HashSet;
import java.util.Set;


public class SeratisMain extends Activity {

    ArrayList<String> providers = new ArrayList<String>();
    ArrayList<String> patients = new ArrayList<String>();
    String providersNonJson = "{'providers':[{'id': 1,'name':'Perry Cox'},{'id': 2,'name': 'John Dorian'},{'id': 3,'name':'Christopher Turk'}]}";
    String patientsNonJson = "{'patients': [{'id': 1,'name': 'Eric Cartman'},{'id': 2,'name': 'Stan Marsh'}]}";
    String joinNonJson = "{'joinmap':[{'id':1,'providerId':'1','patientIds':'1,2'},{'id':2,'providerId':'2','patientIds':'1'},{'id':3,'providerId':'3','patientIds':''}]}";
    JSONObject patientsJSON;
    JSONObject providersJSON;
    JSONObject joinJSONObject;
    JSONArray joinJSONArray;
    private ListView patientListView;
    private ListView providerListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seratis_main);


        constructLists();
        makeNiceLists();
    }

    private void makeNiceLists() {
        patientListView = (ListView) findViewById(R.id.patientList);
        providerListView = (ListView) findViewById(R.id.providerList);

        View patientHeader = View.inflate(this, R.layout.patient_header, null);
        View providerHeader = View.inflate(this, R.layout.provider_header, null);

        TextView patientTitle = (TextView) findViewById(R.id.patientTitle);
        TextView providerTitle = (TextView) findViewById(R.id.providerTitle);

        patientListView.addHeaderView(patientHeader);
        patientListView.setTag("Patient");
        providerListView.addHeaderView(providerHeader);
        providerListView.setTag("Provider");

        try {
            int n = 0;
            while (n < providersJSON.getJSONArray("providers").length()) {
                providers.add(providersJSON.getJSONArray("providers").getJSONObject(n).get("name").toString());
                n++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            int n = 0;
            while (n < patientsJSON.getJSONArray("patients").length()) {
                patients.add(patientsJSON.getJSONArray("patients").getJSONObject(n).get("name").toString());
                n++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        providerListView.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, providers));
        patientListView.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, patients));

        providerListView.setOnItemClickListener(new ListListener(providerListView, this));
        patientListView.setOnItemClickListener(new ListListener(patientListView, this));
    }

    public void constructLists() {
        try {
            patientsJSON = new JSONObject(patientsNonJson);
            providersJSON = new JSONObject(providersNonJson);
            joinJSONObject = new JSONObject(joinNonJson);
            joinJSONArray = new JSONArray(joinJSONObject.getString("joinmap"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.seratis_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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

            int leftListId = position-1; //Have to move the counter back by one to match the JSON and lists
            String message = "";
            if (position > 0) {//Simple test to not do any work on the Header Row.
                AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                if (dynamicListView.getTag().toString().equals("Provider")) {
                    //Provider View

                    try {
                        alertDialog.setTitle(dynamicListView.getTag().toString() + " " +
                               activity.providersJSON.getJSONArray("providers").getJSONObject(leftListId).get("name").toString());

                        for (int j = 0; j < activity.joinJSONArray.length(); j++) {
                            int compareId = Integer.parseInt(activity.joinJSONArray.getJSONObject(j).get("providerId").toString());
                            if (position == compareId) {
                                ArrayList<String> namesList = new ArrayList<String>();
                                String namesAsIds = activity.joinJSONArray.getJSONObject(j).get("patientIds").toString();
                                String[] tempNamesArray = namesAsIds.split(",");
                                int tempNamesArrayLocation = 0;
                                for (String s : tempNamesArray) {

                                    if (j <= tempNamesArray.length) {
                                        int nameId = Integer.parseInt(activity.patientsJSON.getJSONArray("patients").getJSONObject(tempNamesArrayLocation).get("id").toString());
                                    if (Integer.parseInt(s) == nameId) {
                                        message += activity.patientsJSON.getJSONArray("patients").getJSONObject(tempNamesArrayLocation).get("name").toString() + "\n";

                                    }
                                }
                                    tempNamesArrayLocation++;
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    //Patient View

                    try {
                        alertDialog.setTitle(dynamicListView.getTag().toString() + " " +
                                activity.patientsJSON.getJSONArray("patients").getJSONObject(leftListId).get("name").toString());

                        ArrayList<String> finalIds = new ArrayList<String>();
                        Set<String> uniqueIds = null;
                        for (int j = 0; j < activity.joinJSONArray.length(); j++) {
                            String patientNamesAsIds = activity.joinJSONArray.getJSONObject(j).get("patientIds").toString();
                            String[] tempPatientNamesArray = patientNamesAsIds.split(",");
                            int spCounter = 0;
                            for (String sp : tempPatientNamesArray) {
                                if (position == j) {
                                    int nameId = Integer.parseInt(activity.providersJSON.getJSONArray("providers").getJSONObject(spCounter).get("id").toString());
                                    if (sp.length() > 0 && Integer.parseInt(sp) == nameId) {
                                        //message += activity.providersJSON.getJSONArray("providers").getJSONObject(spCounter).get("name").toString() + "\n";
                                        finalIds.add(activity.providersJSON.getJSONArray("providers").getJSONObject(spCounter).get("id").toString());
                                    }

                                }
                                spCounter++;
                            }
                           uniqueIds = new HashSet<String>(finalIds);
                        }
                        for (String s : uniqueIds) {
                            message += activity.providersJSON.getJSONArray("providers").getJSONObject(Integer.parseInt(s)).get("name").toString() + "\n";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }



                alertDialog.setMessage(message);
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Add your code for the button here.
                    }
                });
                alertDialog.show();
            } else {
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, "No info for header..", duration);
                toast.show();
            }
        }
    }
}
