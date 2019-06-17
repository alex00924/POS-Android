package com.bulletcart.videorewards.Fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bulletcart.videorewards.Global.GlobalFunctions;
import com.bulletcart.videorewards.R;
import com.bulletcart.videorewards.Activities.FragmentsActivity;
import com.bulletcart.videorewards.Adapters.VideosAdapter;
import com.bulletcart.videorewards.app.App;
import com.bulletcart.videorewards.Model.Videos;
import com.bulletcart.videorewards.Utils.CustomRequest;
import com.bulletcart.videorewards.Utils.Dialogs;
import com.thefinestartist.ytpa.enums.Quality;
import com.thefinestartist.ytpa.utils.YouTubeThumbnail;
import com.thefinestartist.ytpa.utils.YouTubeUrlParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.bulletcart.videorewards.Global.GlobalConstants.APP_VIDEOS;
import static com.bulletcart.videorewards.Global.GlobalConstants.DEBUG_MODE;

/**
 * Created by DroidOXY
 */
 
public class VideosFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    TextView emptyText;
    ImageView emptyImage;
    RecyclerView videos;
    public static VideosAdapter videosAdapter;
    public static ArrayList<Videos> allvideos;
    ProgressBar progressBar;
    Context ctx;
    private SwipeRefreshLayout swipeView;
    public VideosFragment() {
        // Required empty public constructor
    }

    View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ctx = getActivity();
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_videos, container, false);

        emptyText = view.findViewById(R.id.empty);
        emptyImage = view.findViewById(R.id.emptyImage);
        progressBar = view.findViewById(R.id.progressBar);

        videos = view.findViewById(R.id.videos);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ctx);
        videos.setLayoutManager(layoutManager);
        videos.setItemAnimator(new DefaultItemAnimator());

        allvideos = new ArrayList<>();

        videosAdapter = new VideosAdapter(ctx,allvideos);
        videos.setAdapter(videosAdapter);

        swipeView = view.findViewById(R.id.swipe_view);
        swipeView.setOnRefreshListener(this);
        swipeView.setColorSchemeColors(Color.GRAY, Color.GREEN, Color.BLUE,
                Color.RED, Color.CYAN);
        swipeView.setDistanceToTriggerSync(100);// in dips
        swipeView.setSize(SwipeRefreshLayout.DEFAULT);// LARGE also can be used
        videos.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                swipeView.setEnabled(dy == 0);
            }
        });

        getVideos();
        return view;
    }

    public void getVideos() {
        //if logged in
        if (App.getInstance().getId() > 0) {
            CustomRequest transactionsRequest = new CustomRequest(Request.Method.POST, APP_VIDEOS,null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try{

                                JSONObject Response = new JSONObject(App.getInstance().deData(response.toString()));

                                if (!Response.getBoolean("error")) {

                                    allvideos.clear();
                                    JSONArray videos = Response.getJSONArray("videos");

                                    for (int i = 0; i < videos.length(); i++) {

                                        JSONObject obj = videos.getJSONObject(i);

                                        Videos singleVideoItem = new Videos();

                                        String videoId = obj.getString("video_id");

                                        singleVideoItem.setVideoId(videoId);
                                        singleVideoItem.setTitle(obj.getString("video_title"));
                                        singleVideoItem.setSubtitle(obj.getString("video_subtitle"));
                                        singleVideoItem.setAmount(obj.getString("video_amount"));
                                        singleVideoItem.setLimit(obj.getString("video_limit"));
                                        singleVideoItem.setLimitPlayingTimes(obj.getString("video_limit_playing_times"));
                                        singleVideoItem.setWatchedTimes(obj.getString("video_watched_times"));
                                        singleVideoItem.setDuration(obj.getString("video_duration"));

                                        String videoURL = obj.getString("video_url");
                                        String videoThumbnailUrl = obj.getString("video_thumbnail");

                                        if(videoThumbnailUrl.equals("none")){
                                            singleVideoItem.setImage(YouTubeThumbnail.getUrlFromVideoId(YouTubeUrlParser.getVideoId(videoURL), Quality.HIGH));
                                        }else{
                                            singleVideoItem.setImage(obj.getString("video_thumbnail"));
                                        }

                                        singleVideoItem.setVideoURL(videoURL);
                                        singleVideoItem.setOpenLink(obj.getString("video_open_link"));
                                        singleVideoItem.setStatus(obj.getString("video_status"));

                                        if(obj.get("video_status").equals("Active")){
                                            allvideos.add(singleVideoItem);
                                            progressBar.setVisibility(View.GONE);
                                        }

//                                    if(obj.get("video_status").equals("Active") && !App.getInstance().get("APPVIDEO_"+videoId,false)){
//                                        allvideos.add(singleVideoItem);
//                                        progressBar.setVisibility(View.GONE);
//                                    }

                                    }

                                    videosAdapter.notifyDataSetChanged();

                                    checkHaveVideos();

                                } else if(Response.getInt("error_code") == 699) {
                                    // already watched and got point
                                    GlobalFunctions.showToast(ctx, Response.getString("error_description"));
                                } else if(Response.getInt("error_code") == 699 || Response.getInt("error_code") == 999){

                                    Dialogs.validationError(ctx,Response.getInt("error_code"));

                                }else{

                                    if(!DEBUG_MODE){
                                        Dialogs.serverError(ctx, getResources().getString(R.string.ok), new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                finish();
                                            }
                                        });
                                    }

                                }


                            }catch (Exception e){

                                if(!DEBUG_MODE){
                                    Dialogs.serverError(ctx, getResources().getString(R.string.ok), new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            finish();
                                        }
                                    });
                                }else{
                                    Dialogs.errorDialog(ctx,"Got Error",e.toString() + ", please contact developer immediately",true,false,"","ok",null);
                                }

                            }

                        }},new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    if(!DEBUG_MODE){
                        Dialogs.serverError(ctx, getResources().getString(R.string.ok), new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                finish();
                            }
                        });
                    }else{
                        Dialogs.errorDialog(ctx,"Got Error",error.toString(),true,false,"","ok",null);
                    }

                }}){
                @Override
                protected Map<String,String> getParams(){
                    Map<String,String> params = new HashMap<>();
                    params = App.getInstance().getCredential();
                    return params;
                }
            };

            App.getInstance().addToRequestQueue(transactionsRequest);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    void checkHaveVideos(){

        if(progressBar.getVisibility() == View.VISIBLE){
            emptyText.setVisibility(View.VISIBLE);
            emptyImage.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    void finish(){

        Activity close = getActivity();
        if(close instanceof FragmentsActivity){
            FragmentsActivity show = (FragmentsActivity) close;
            show.closeActivity();
        }

    }

    @Override
    public void onRefresh() {
        getVideos();
        swipeView.postDelayed(new Runnable() {
            @Override
            public void run() {

                swipeView.setRefreshing(false);
            }
        }, 1000);
    }
}