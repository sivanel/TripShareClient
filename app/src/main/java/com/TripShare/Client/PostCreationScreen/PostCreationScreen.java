package com.TripShare.Client.PostCreationScreen;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.TripShare.Client.Common.*;
import com.TripShare.Client.CommunicationWithServer.SendPostToAddToDB;
import com.TripShare.Client.CommunicationWithServer.UpdateRouteInDB;
import com.TripShare.Client.HomeScreen.HomeScreen;
import com.TripShare.Client.ProfileScreen.ProfileScreen;
import com.TripShare.Client.R;
import com.TripShare.Client.RoutesScreen.RoutesScreen;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import org.angmarch.views.NiceSpinner;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PostCreationScreen extends ActivityWithNavigationDrawer
    implements OnMapReadyCallback , GoogleMap.OnMarkerClickListener, AddPhotoDialog.AttachImageToCoordinateListener, AddNoteDialog.AttachNoteToCoordinateListener
{
    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(20);

    private Post m_postToAdd;
    private String m_postThumbnailString;
    private NiceSpinner m_spinner;
    private SpinnerAdapter m_adapter;
    private GoogleMap m_map;
    private MapFragment m_mapFragment;
    private PolylineOptions m_polyline;
    private List<PatternItem> m_pattern = Arrays.asList(DOT, GAP);
    private List<Marker> m_markers;
    private Bitmap m_markerIcon;
    private int m_currentCoordinateIndex;
    private AlertDialog m_progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_creation_screen);
        setActivityTitle("Create a new post");
        initializeDrawerLayout();
        initializeViews();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    private void addItemToSpinner(Route i_route)
    {
        m_adapter.add(new SpinnerItem(i_route));
    }

    private void addAllItemsToView()
    {
        ArrayList<Route> routes = ApplicationManager.getUserRoutes();

        for (int i = 0; i < routes.size(); i++) {
            addItemToSpinner(routes.get(i));
        }
    }

    @Override
    public void attachNoteToRelevantCoordinate(String i_noteToAttach)
    {
        // Attaching the image to the coordinate.
        SpinnerItem itemSelected = m_adapter.getItems().get(m_spinner.getSelectedIndex());
        itemSelected.getRoute().getRouteCoordinates().get(m_currentCoordinateIndex).setNote(i_noteToAttach);
        Marker relevantMarker =  m_markers.get(m_currentCoordinateIndex);
        relevantMarker.setTitle(i_noteToAttach);
        addNoteIconNearMarker();
    }

    @Override
    public void attachImageToRelevantCoordinate(Bitmap i_imageToAttach)
    {
        //converting the image to string
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        i_imageToAttach.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        String imageString = Base64.encodeToString(byteArray, Base64.DEFAULT);

        // Attaching the image to the coordinate.
        SpinnerItem itemSelected = m_adapter.getItems().get(m_spinner.getSelectedIndex());
        itemSelected.getRoute().getRouteCoordinates().get(m_currentCoordinateIndex).setImageString(imageString);

        showImageNearCoordinate(i_imageToAttach);
    }

    private void addImageMarkerAndConvertStringToBitmap(String i_imageString, LatLng i_coordinateToAttachTo)
    {
        byte[] decodedString = Base64.decode(i_imageString, Base64.DEFAULT);
        Bitmap imageToShow = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        Bitmap smallerMarker = Bitmap.createScaledBitmap(imageToShow, 100, 100, false);
        m_map.addMarker(new MarkerOptions().position(i_coordinateToAttachTo).icon(BitmapDescriptorFactory.fromBitmap(smallerMarker))).setAnchor(1f, 1f);
    }

    private void addNoteIconNearMarker()
    {
        LatLng coordinateToAttachTo = m_markers.get(m_currentCoordinateIndex).getPosition();
        Bitmap iconBitmap = getBitmapFromVectorDrawable(getApplicationContext(), R.drawable.ic_note);
        Bitmap smallerMarker = Bitmap.createScaledBitmap(iconBitmap, 50, 50, false);
        m_map.addMarker(new MarkerOptions().position(coordinateToAttachTo).icon(BitmapDescriptorFactory.fromBitmap(smallerMarker))).setAnchor(1f, 0.3f);
    }

    private static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId)
    {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private void initializeViews()
    {
        initializeSpinnerWithRoutes();
        m_markerIcon = getBitmapFromVectorDrawable(getApplicationContext(), R.drawable.ic_marker_on_map); //converting image from vector to bitmap

        m_mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        m_mapFragment.getMapAsync(this);

        addAllItemsToView();
        updateRelevantViewWithSource();
    }

    private void initializeSpinnerWithRoutes()
    {
        m_adapter = new SpinnerAdapter(PostCreationScreen.this);
        m_spinner = (NiceSpinner) findViewById(R.id.spinner);
        m_spinner.setDropDownListPaddingBottom(10);

        m_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                m_map.clear();
                initializeMap();

                String spinnerItemString = ((TextView)view).getText().toString();
                if (!((spinnerItemString.equals("Choose Route..."))))
                    showRouteOnMap(spinnerItemString);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void initializeMap()
    {
        m_polyline = new PolylineOptions();
        m_map.getUiSettings().setZoomControlsEnabled(true);
        m_polyline.pattern(m_pattern);
        m_markers = new ArrayList<>();
    }

    @Override
    public void onMapReady(GoogleMap map)
    {
        m_map = map;
        m_map.setOnMarkerClickListener(this);
        initializeMap();


        // Here we check if we had gotten any additional information while getting to this screen, if yes, we select the relevant route and display it.
        Intent intent = getIntent();
        int routeIndexToSelect = intent.getIntExtra("RouteIndexToSelect", -1);

        if (routeIndexToSelect != -1)
        {
            m_spinner.setSelectedIndex(routeIndexToSelect+1); // index must be index+1 since at index=0 we have the default choice which was not available in the previous screen
            m_map.clear();
            initializeMap();

            String spinnerItemString = m_adapter.getItem(routeIndexToSelect+1);
            if (!((spinnerItemString.equals("Choose Route..."))))
                showRouteOnMap(spinnerItemString);
        }
    }

    public void OnAddRouteButtonClick(View view)
    {
        Intent routeCreationScreen = new Intent(PostCreationScreen.this, RoutesScreen.class);
        startActivity(routeCreationScreen);
    }

    @Override
    public boolean onMarkerClick(final Marker marker)
    {
        // Getting the pressed coordinate index
        m_currentCoordinateIndex = m_markers.indexOf(marker);
        DialogFragment dialog = new ChooseAdditionDialog();
        dialog.show(getSupportFragmentManager(), "Choose Addition");
        return true;
    }

    public void OnPostButtonClick(View view)
    {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                TextView postTitle = findViewById(R.id.post_title_editText);
                TextView postDescription = findViewById(R.id.post_description_editText);
                CheckBox postIsPrivate = findViewById(R.id.post_isPrivatePostCheckbox);

                long routeID = m_adapter.getItems().get(m_spinner.getSelectedIndex()).getRoute().getRouteID();
                SpinnerItem itemSelected = m_adapter.getItems().get(m_spinner.getSelectedIndex());
                Route updatedRoute = itemSelected.getRoute();
                setThumbnailForPost(updatedRoute);

                User loggedInUser = ApplicationManager.getLoggedInUser();
                m_postToAdd = new Post(loggedInUser.getID(), postTitle.getText().toString(), postDescription.getText().toString());
                m_postToAdd.setRouteID(routeID);
                m_postToAdd.setUserFirstName(loggedInUser.getStringUserName());
                m_postToAdd.setUserLastName(loggedInUser.getLastName());
                m_postToAdd.setIsPrivatePost(postIsPrivate.isChecked());

                if (m_postThumbnailString != null)
                {
                    m_postToAdd.setThumbnailString(m_postThumbnailString);
                }
            }
        });

        askUserToCheckRelevantTags(); //NOTE: the upcoming code had to be moved inside OnDismiss of the dialog in order to sync

    }

    private void askUserToCheckRelevantTags()
    {
            final DialogFragment editTagsDialog = new TagSelectionDialog();

            editTagsDialog.show(getSupportFragmentManager(), "");
            getSupportFragmentManager().executePendingTransactions();
            editTagsDialog.getDialog().setOnDismissListener((new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    Toast.makeText(getApplicationContext(), "Posting... This could take a moment.",
                            Toast.LENGTH_LONG).show();
                    m_postToAdd.setTags(((TagSelectionDialog) editTagsDialog).getSelectedTags());

                    // THE REST OF THE CODE IS HERE //
                    // Update Relevant Route.
                    SpinnerItem itemSelected = m_adapter.getItems().get(m_spinner.getSelectedIndex());
                    Route updatedRoute = itemSelected.getRoute();
                    new UpdateRouteInDB(updatedRoute).execute();

                    // Send Post to Server and save in DB.
                    new SendPostToAddToDB(m_postToAdd).execute();

                    //change screen to profile after you've posted a post
                    Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }));
    }

    private void setThumbnailForPost(Route i_route)
    {
        List<Coordinate> coordinates = i_route.getRouteCoordinates();

        for(int i=0; i < coordinates.size(); i++)
        {
            String imageString = coordinates.get(i).getAddition().getImageString();
            if(imageString != null) {
                m_postThumbnailString = imageString;
                break;
            }
        }
    }

    private void showRouteOnMap(String i_spinnerItemString)
    {
        SpinnerItem clickedItem = (SpinnerItem) m_adapter.getItemByName(i_spinnerItemString);

        List<Coordinate> routeCoordinates = clickedItem.getRoute().getRouteCoordinates();
        LatLng currLatLng;

        for (Coordinate coord : routeCoordinates)
        {
            currLatLng = new LatLng(Double.valueOf(coord.getLatitude()), Double.valueOf(coord.getLongitude()));
            Marker marker;

            marker = m_map.addMarker(new MarkerOptions()
                    .position(currLatLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(m_markerIcon)));
            marker.setAnchor(0.5f, 0.5f);
            m_map.addPolyline(m_polyline
                    .add(currLatLng)
                    .width(24)
                    .color(Color.rgb(100, 186, 105)));
            m_markers.add(marker);

            String coordinateString = coord.getImageString();
            if(coordinateString != null && !coordinateString.isEmpty())
            {
                addImageMarkerAndConvertStringToBitmap(coordinateString, marker.getPosition());
            }

            if(coord.getNote() != null)
            {
                marker.setTitle(coord.getNote());
            }
        }

        zoomRoute();
    }

    private void zoomRoute()
    {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        //the include method will calculate the min and max bound.
        for(Marker marker: m_markers)
            builder.include(marker.getPosition());

        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.20); // offset from edges of the map 20% of screen

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

        m_map.animateCamera(cameraUpdate);
    }

    private void showImageNearCoordinate(Bitmap i_imageToAttach)
    {
        LatLng coordinateToAttachTo = m_markers.get(m_currentCoordinateIndex).getPosition();
        Bitmap smallerMarker = Bitmap.createScaledBitmap(i_imageToAttach, 100, 100, false);
        m_map.addMarker(new MarkerOptions().position(coordinateToAttachTo).icon(BitmapDescriptorFactory.fromBitmap(smallerMarker))).setAnchor(1f, 1f);
    }

    private void settingTheDefaultValueForTheSpinner()
    {
        Route defaultValue = new Route(ApplicationManager.getLoggedInUser().getID());
        defaultValue.setRouteName("Choose Route...");
        m_adapter.getItems().add(0, new SpinnerItem(defaultValue));
        m_spinner.setAdapter(m_adapter);
        m_spinner.setSelectedIndex(0);
    }

    private void updateRelevantViewWithSource()
    {
        m_spinner.setAdapter(m_adapter);
        settingTheDefaultValueForTheSpinner();
        m_adapter.notifyDataSetChanged();
    }
}