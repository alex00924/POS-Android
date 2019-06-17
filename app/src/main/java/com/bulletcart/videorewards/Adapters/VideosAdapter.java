package com.bulletcart.videorewards.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bulletcart.videorewards.Activities.MainActivity;
import com.bulletcart.videorewards.Global.GlobalFunctions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bulletcart.videorewards.R;
import com.bulletcart.videorewards.Activities.FragmentsActivity;
import com.bulletcart.videorewards.app.App;
import com.bulletcart.videorewards.Model.Videos;

import java.util.List;

/**
 * Created by DroidOXY
 */

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.ViewHolder>{

    private Context context;
    private List<Videos> listItem;

    public VideosAdapter(Context context, List<Videos> listItem) {
        this.context = context;
        this.listItem = listItem;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_list,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        String PrevItem;
        final Videos video = listItem.get(position);

        final String videoId = video.getVideoId();
        final String title = video.getTitle();
        final String subtitle = video.getSubtitle();
        final String videoURL = video.getVideoURL();
        final String videoPoints = video.getAmount();
        final String videoLimit = video.getLimit();
        final String videoLimitPlayingTimes = video.getLimitPlayingTimes();
        final String videoWatchedTimes = video.getWatchedTimes();
        final String timeDuration = video.getDuration().equals("null") ? "Full Play": video.getDuration();
        final String image = video.getImage();
        final String openLink = video.getOpenLink();
        final String status = video.getStatus();

        holder.title.setText(title);
        holder.subtitle.setText(subtitle);
        if(App.getInstance().get("APPVIDEO_"+videoId,false)){

//            holder.SingleItem.setVisibility(View.GONE);

        }else{

            holder.subtitle.setText(subtitle);
        }
        holder.duration.setText("Duration : " + timeDuration + "s");
        holder.amount.setText("+ " + videoPoints);

        Glide.with(context).load(image)
                .apply(new RequestOptions().override(60,60))
                .apply(RequestOptions.placeholderOf(R.drawable.ic_movie_placeholder))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .into(holder.image);


        holder.SingleItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean isLimited = true;
                if (videoLimitPlayingTimes.equals("null") || videoLimitPlayingTimes.equals("")) {
                    isLimited = false;
                } else if(!videoLimitPlayingTimes.equals("null") && !videoLimitPlayingTimes.equals("") && Integer.parseInt(videoWatchedTimes) < Integer.parseInt(videoLimitPlayingTimes)) {
                    isLimited = false;
                }
                if (!isLimited) {
                    try {
                        ((MainActivity)context).playVideo(videoId, videoPoints, videoURL,timeDuration,openLink, position);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    GlobalFunctions.showToast(context, context.getString(R.string.video_limit_playing_times));
//                    holder.SingleItem.setVisibility(View.GONE);
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return listItem.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView date,title,subtitle,amount,duration;
        ImageView image;
        LinearLayout SingleItem;
        ViewHolder(View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.date);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            duration = itemView.findViewById(R.id.duration);
            amount = itemView.findViewById(R.id.amount);
            image = itemView.findViewById(R.id.image);
            SingleItem = itemView.findViewById(R.id.SingleItem);
        }
    }
}
