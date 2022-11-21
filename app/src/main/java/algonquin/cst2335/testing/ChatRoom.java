package algonquin.cst2335.testing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import algonquin.cst2335.testing.databinding.ActivityChatRoomBinding;

public class ChatRoom extends AppCompatActivity {

    ActivityChatRoomBinding binding;
    ArrayList<ChatMessage> messages = new ArrayList<>();
    RecyclerView.Adapter myAdapter;
    ChatRoomViewModel chatModel;
    private RecyclerView mRecyclerView;
    private RequestQueue mRequestQueue;
    ChatMessageDAO mDAO;
    //private EventAdapter mEventAdapter;
    //private ArrayList<EventItem> mEventList;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button button = findViewById(R.id.button);
        Button button2 = findViewById(R.id.button2);
        EditText editText = findViewById(R.id.textInput);
        EditText editText2 = findViewById(R.id.textInput2);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        messages = new ArrayList<>();

        mRequestQueue = Volley.newRequestQueue(this);

        chatModel = new ViewModelProvider(this).get(ChatRoomViewModel.class);
        messages = chatModel.messages.getValue();

        MessageDatabase db = Room.databaseBuilder(getApplicationContext(), MessageDatabase.class, "database-name").build();
        mDAO = db.cmDAO();

        if(messages == null)
        {
            chatModel.messages.postValue( messages = new ArrayList<ChatMessage>());

            /*
            Executor thread = Executors.newSingleThreadExecutor();
            thread.execute(() ->
            {
                messages.addAll( mDAO.getAllMessages() ); //Once you get the data from database

                runOnUiThread( () ->  binding.recyclerView.setAdapter( myAdapter )); //You can then load the RecyclerView
            });

            */
        }

        // SharePre Object to store data
        SharedPreferences prefs = getSharedPreferences("MyCityData", Context.MODE_PRIVATE);
        String cityName = prefs.getString("City", "");
        editText.setText(cityName);
        SharedPreferences.Editor editor = prefs.edit();
        SharedPreferences prefs2 = getSharedPreferences("MyRadiusData", Context.MODE_PRIVATE);
        String radiusAmount = prefs2.getString("Radius", "");
        editText2.setText(radiusAmount);
        SharedPreferences.Editor editor2 = prefs2.edit();

        button.setOnClickListener(click ->{


            messages.clear(); // clear list
           // mEventAdapter = new EventAdapter(MainActivity.this, mEventList);
            //mRecyclerView.setAdapter(mEventAdapter);
            myAdapter.notifyDataSetChanged();



            String value = editText.getText().toString();
            editor.putString("City", value);
            editor.apply();
            String value2 = editText2.getText().toString();
            editor2.putString("Radius", value2);
            editor2.apply();


            parseJSON();

            // Toast Message
            String city = editText.getText().toString();
            String radius = editText2.getText().toString();
            Context context = getApplicationContext();
            CharSequence text = "The search result of "+city+" within radius "+radius+" is shown." ;
            int duration = Toast.LENGTH_LONG;

            Toast.makeText(context, text, duration).show();
        });

        binding.recyclerView.setAdapter(myAdapter = new RecyclerView.Adapter<MyRowHolder>() {
            @NonNull
            @Override
            public MyRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View root;
                root = getLayoutInflater().inflate(R.layout.event_list, parent, false);
                return new MyRowHolder(root);
            }


            @Override
            public void onBindViewHolder(@NonNull MyRowHolder holder, int position) {

                ChatMessage currentItem = messages.get(position);
                String obj = currentItem.getName();
                String obj2 = currentItem.getDate();
                String obj3 = currentItem.getUrl();
                holder.nameText.setText(obj);
                holder.dateText.setText(obj2);
                holder.urlText.setText(obj3);


            }

            @Override
            public int getItemCount() {
                return messages.size();
            }


        });

        button2.setOnClickListener(clk2 ->{

            chatModel.messages.postValue( messages = new ArrayList<ChatMessage>());

            Executor thread = Executors.newSingleThreadExecutor();
            thread.execute(() ->
            {
                messages.addAll( mDAO.getAllMessages() ); //Once you get the data from database

                runOnUiThread( () ->  binding.recyclerView.setAdapter( myAdapter )); //You can then load the RecyclerView
            });





        });


    }



    private void parseJSON() {
        EditText editText = findViewById(R.id.textInput);
        String city = editText.getText().toString();
        EditText editText2 = findViewById(R.id.textInput2);
        String radius = editText2.getText().toString();

        String url = "https://app.ticketmaster.com/discovery/v2/events.json?apikey=BNuUhbdNk9iNXFMROTkjwCLQCogOtzyf&city="+city+"&radius="+radius;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {



                        try {
                            JSONObject obj = response.getJSONObject("_embedded");
                            JSONArray array = obj.getJSONArray("events");

                            for(int i = 0; i < array.length(); i++){
                                //For All
                                JSONObject obj2 = array.getJSONObject(i);

                                // For localDate
                                JSONObject obj3 = obj2.getJSONObject("dates");
                                JSONObject obj4 = obj3.getJSONObject("start");

                                // For priceRanges
                                //JSONArray obj5 = obj2.getJSONArray("priceRanges");
                                //JSONObject obj6 = obj5.getJSONObject(1);


                                ChatMessage chatMessage = new ChatMessage();
                                chatMessage.setName(obj2.getString("name"));
                                chatMessage.setDate(obj4.getString("localDate"));
                                chatMessage.setUrl(obj2.getString("url"));
                                messages.add(chatMessage);


                            }

                            binding.recyclerView.setAdapter(myAdapter = new RecyclerView.Adapter<MyRowHolder>() {
                                @NonNull
                                @Override
                                public MyRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                    View root;
                                    root = getLayoutInflater().inflate(R.layout.event_list, parent, false);
                                    return new MyRowHolder(root);
                                }

                                @Override
                                public void onBindViewHolder(@NonNull MyRowHolder holder, int position) {
                                    ChatMessage currentItem = messages.get(position);
                                    String obj = currentItem.getName();
                                    String obj2 = currentItem.getDate();
                                    String obj3 = currentItem.getUrl();
                                    holder.nameText.setText(obj);
                                    holder.dateText.setText(obj2);
                                    holder.urlText.setText(obj3);
                                }

                                @Override
                                public int getItemCount() {
                                    return messages.size();
                                }
                            });


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mRequestQueue.add(request);



    }

    class MyRowHolder extends RecyclerView.ViewHolder {
        public TextView nameText;
        public TextView dateText;
        public TextView urlText;


        public MyRowHolder(@NonNull View itemView) {
            super(itemView);

            /*
            itemView.setOnClickListener(clk ->{

            int position = getAbsoluteAdapterPosition();
            AlertDialog.Builder builder = new AlertDialog.Builder( ChatRoom.this );
            builder.setMessage("Do you want to add this event to database: " + nameText.getText() )
                    .setTitle("Question:")
                    .setNegativeButton("No", (dialog, cl) -> {})
                    .setPositiveButton("Yes", (dialog, cl) -> {

                        ChatMessage thisMessage = messages.get(position);
                        Executor thread = Executors.newSingleThreadExecutor();
                        thread.execute( () -> {
                            mDAO.insertMessage(thisMessage);
                        });
                        messages.add(position,thisMessage);
                        myAdapter.notifyItemChanged(position);

                        //messages.remove(position);
                        //myAdapter.notifyItemRemoved(position);

                        Snackbar.make(nameText, "You add this message to database" + position, Snackbar.LENGTH_LONG)
                                .setAction("UNDO", clk1 -> {

                                    messages.remove(position);


                                }).show();



                    }).create().show();
            });

             */

            nameText = itemView.findViewById(R.id.text_view_name);
            dateText = itemView.findViewById(R.id.text_view_date);
            urlText = itemView.findViewById(R.id.text_view_url);

        }
    }


}