package com.aos.io2017;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.androidquery.AQuery;
import com.aos.io2017.entities.User;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.List;

public class Main extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {


    private List<User> users = new ArrayList<User>();

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private GoogleApiClient mGoogleApiClient;

    private DatabaseReference databaseReference;

    private AQuery aquery;

    private ListView list;

    private FirebaseRemoteConfig firebaseRemoteConfig;
    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setHomeButtonEnabled(true);

        //Remote Config para la carga del titulo

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(true)
                .build();

        firebaseRemoteConfig.setConfigSettings(configSettings);

        firebaseRemoteConfig.fetch(1).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                firebaseRemoteConfig.activateFetched();
                getSupportActionBar().setTitle(firebaseRemoteConfig.getString("app_title"));
            }
        });

        //Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);


        ((Button) findViewById(R.id.close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });

        //inicamos la autenticacion para el cierre

        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //start firebase database connection
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("users");

        user = FirebaseAuth.getInstance().getCurrentUser();

        aquery = new AQuery(this);
        aquery.id(R.id.profile_image).image(user.getPhotoUrl().toString());

        ((TextView) findViewById(R.id.profile_name)).setText(user.getDisplayName());

        //cargamos los usuarios

        //cargamos el grupo
        User group = new User();
        group.setId("0");
        group.setName("GDG Santa Cruz");
        group.setEmail("GDG Santa Cruz");
        group.setPhoto("https://lh6.googleusercontent.com/-fe6PusUqlWI/AAAAAAAAAAI/AAAAAAAAA4E/cMmA69H3GuU/photo.jpg");
        users.add(group);

        UserAdapter adapter = new UserAdapter(Main.this, users);
        list = (ListView) findViewById(R.id.users);
        list.setAdapter(adapter);

        ChildEventListener listener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String lastKey) {
                User message = dataSnapshot.getValue(User.class);
                if (!(message.getId().equals(user.getUid()))) {
                    users.add(message);
                }
                ((UserAdapter) list.getAdapter()).notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        databaseReference.addChildEventListener(listener);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                User user = (User) list.getAdapter().getItem(i);
                Intent intent = new Intent(Main.this, Chat.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });

        EditText filter = (EditText) findViewById(R.id.filter);

        filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ((UserAdapter) list.getAdapter()).getFilter().filter(charSequence.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        updateUserToken();

    }

    private void updateUserToken() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if (refreshedToken != null) {
            User dUser = new User();
            dUser.setId(user.getUid());
            dUser.setEmail(user.getEmail());
            dUser.setName(user.getDisplayName());
            dUser.setPhoto(user.getPhotoUrl().toString());
            dUser.setToken(refreshedToken);
            databaseReference.child(user.getUid()).setValue(dUser);

            //Log event Analytics
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, user.getUid());
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Ingreso a la App");
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            FirebaseMessaging.getInstance().subscribeToTopic("chats");

        }
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();
        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        startActivity(new Intent(Main.this, Register.class));
                        finish();
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(Application.tag, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    public class UserAdapter extends ArrayAdapter<User> {

        private ItemFilter filter = new ItemFilter();

        public UserAdapter(Context context, List<User> messages) {
            super(context, 0, messages);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            User user = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_item, parent, false);
            }

            TextView profileName = (TextView) convertView.findViewById(R.id.profile_name);
            ImageView profileImage = (ImageView) convertView.findViewById(R.id.profile_image);
            profileName.setText(user.getName());
            aquery.id(profileImage).image(user.getPhoto());
            return convertView;
        }

        public Filter getFilter() {
            return filter;
        }

        private class ItemFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                String filterString = constraint.toString().toLowerCase();
                FilterResults results = new FilterResults();
                int count = users.size();
                final ArrayList<User> nlist = new ArrayList<User>(count);

                for (int i = 0; i < count; i++) {
                    if (users.get(i).getEmail().toLowerCase().contains(filterString) || users.get(i).getName().toLowerCase().contains(filterString)) {
                        nlist.add(users.get(i));
                    }
                }
                results.values = nlist;
                results.count = nlist.size();
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                list.setAdapter(new UserAdapter(Main.this, (ArrayList<User>) results.values));
            }
        }
    }
}
