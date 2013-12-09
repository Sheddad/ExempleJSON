package com.json.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ActivitatInici extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    // JSON Noms dels Nodes. Hi ha que no es fan servir en aquest exemple
    private static final String TAG_FORECAST = "forecast";
    private static final String TAG_TXT_FORECAST = "txt_forecast";
    private static final String TAG_FORECASTDAY = "forecastday";
    private static final String TAG_PERIOD = "period";
    private static final String TAG_ICON = "icon";
    private static final String TAG_FCTTEXT_METRIC = "fcttext_metric";
    private static final String TAG_POP = "pop";
    private static final String TAG_SIMPLEFORECAST = "simpleforecast";
    private static final String TAG_HIGH = "high";
    private static final String TAG_LOW = "low";
    private static final String TAG_CELSIUS = "celsius";
    private static final String TAG_AVEHUMIDITY = "avehumidity";
    private static final String TAG_AVEWIND = "avewind";
    private static final String TAG_KPH = "kph";
    private static final String TAG_DIR = "dir";

    private String url = "http://api.wunderground.com/api/0a0bf648112054bb/forecast/lang:SP/q/Spain/manresa.json";

    TextView txtTmax [] = new TextView[2];
    TextView txtTmin [] = new TextView[2];
    TextView txtHumitat [] = new TextView[2];
    TextView txtProbPluja [] = new TextView[2];
    TextView txtVent [] = new TextView[2];

    private static final int AVUI = 0;
    private static final int DEMA = 1;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitat_inici);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // SKF. Inicialitzem els TextsViews
        txtTmax[AVUI] = (TextView) findViewById(R.id.txtTMax);
        txtTmin[AVUI] = (TextView) findViewById(R.id.txtTMin);
        txtHumitat[AVUI] = (TextView) findViewById(R.id.txtHumitat);
        txtProbPluja[AVUI] = (TextView) findViewById(R.id.txtProbPluja);
        txtVent[AVUI] = (TextView) findViewById(R.id.txtVent);

        txtTmax[DEMA] = (TextView) findViewById(R.id.txtTMaxDema);
        txtTmin[DEMA] = (TextView) findViewById(R.id.txtTMinDema);
        txtHumitat[DEMA] = (TextView) findViewById(R.id.txtHumitatDema);
        txtProbPluja[DEMA] = (TextView) findViewById(R.id.txtProbPlujaDema);
        txtVent[DEMA] = (TextView) findViewById(R.id.txtVentDema);

        // SKF. Inicialitzem la Barra de Progrés Circular

        pd = new ProgressDialog(this);
        pd.setMessage(getResources().getString(R.string.fent_cerca));
    }

    /*****************************************************************************
     * SKF. INICI DE L'EXEMPLE
     *****************************************************************************/

    /**
     * SKF Funció que executem en clickar el botó CERCAR
     * @param v
     */
    public void onClickCercar (View v) {

        // SKF. Comprovem per veure si tenim connectivitat
        if (isNetworkAvailable()) {
            //SKF. Fem la crida de forma asícrona. Primer ensenyem la barra de progrés
            pd.show();
            new cercarWeatherHttp().execute();
        } else { //SKF. No tenim conexió al dispositiu
            Toast.makeText(getBaseContext(), "ERROR: Sense conexió a INTERNET",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * SKF. Fem la Cerca del temps.
     * Aquí és on fem la crida a la url.
     * OJO!!! En Android, i des de la API ?? les crides que ocupen un temps com a servidors o
     * base de dades s'han de fer de forma asícrona (en Background) si no dona un error difícil
     * de detectar.
     * Molts exemples a interenet estan fets amb API's antigues on no es donava aquest error i no
     * tenen en compte aquest fet.
     */
    class cercarWeatherHttp extends AsyncTask<Void, Void, Boolean> {

        JSONObject jObj = null;
        // contacts JSONArray
        JSONArray forecastday = null;

        protected void onPostExecute(Boolean result) {

            // SKF. Ja podem amagar la barra de progrés
            pd.dismiss();

            if (result) {

                try {
                    //SKF. Comencem a recuperar dades del JSON
                    JSONObject forecast = jObj.getJSONObject(TAG_FORECAST);
                    // SKF. Ara agafem la part de forecast day
                    JSONObject simpleforecast = forecast
                            .getJSONObject(TAG_SIMPLEFORECAST);
                    // SKF. Agafem les dades de la part de simpleforecast
                    forecastday = simpleforecast.getJSONArray(TAG_FORECASTDAY);
                    // SKF. Només volem el periods 0, 1 (que son avui i demà)
                    for (int i = 0; i < 2; i++) {
                        JSONObject fd = forecastday.getJSONObject(i);
                        // Storing each json item in variable

                        // SKF. High Temp és a la seva vegada un JSON Object
                        JSONObject hg = fd.getJSONObject(TAG_HIGH);
                        txtTmax[i].setText(hg.getString(TAG_CELSIUS) + " ºC");

                        // SKF. Low Temp és a la seva vegada un JSON Object
                        JSONObject lw = fd.getJSONObject(TAG_LOW);
                        txtTmin[i].setText(lw.getString(TAG_CELSIUS) + " ºC");

                        //SKF. Probabilitat de Pluja
                        txtProbPluja[i].setText(fd.getString(TAG_POP) + " %");

                        //SKF. Humitat
                        txtHumitat[i].setText(fd.getString(TAG_AVEHUMIDITY) + " %");

                        // SKF. Vent
                        JSONObject aw = fd.getJSONObject(TAG_AVEWIND);
                        txtVent[i].setText(aw.getString(TAG_KPH) + " " + aw.getString(TAG_DIR));

                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(), "PROBLEMES DE CONEXIÓ 1",
                            Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(getBaseContext(), "PROBLEMES DE CONEXIÓ 2",
                        Toast.LENGTH_SHORT).show();
            }
        }

        /*
         * SKF. Conexió al WebServer
         */
        @Override
        protected Boolean doInBackground(Void... arg0) {

            // SKF. Fem la Crida al la calse JSONPArser que hem creat al paquet
            JSONParser jParser = new JSONParser();

            // SKF. Si ens retorna el Objecte JSON, recuperem dades
            try {
                // getting JSON string from URL
                jObj = jParser.getJSONFromUrl(url);
                return true;
            } catch (Exception e) { //SKF. No ens ha retornat un Objecte JSON
                Log.e("JSON Parser", "Error parsing data " + e.toString());
                return false;
            }
        }
    }

    /**
     * SKF. PER COMPROVAR QUE TENIM CONNECTIVITAT PER INTERNET
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /*****************************************************************************
     * SKF. FINAL DE LA CERCA
     *****************************************************************************/

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.activitat_inici, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_activitat_inici, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((ActivitatInici) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
