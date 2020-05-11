package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.GamesActivity;
import com.example.myapplication.R;
import com.example.myapplication.model.Banner;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BannerSliderAdapter extends SliderViewAdapter<BannerSliderAdapter.SliderAdapterVH> {

    private List<Banner> bannerList;
    private Context context;

    public BannerSliderAdapter(Context context, List<Banner> bannerList) {
        this.context = context;
        this.bannerList = bannerList;
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(final ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, final int position) {
        viewHolder.textViewDescription.setText(bannerList.get(position).getBannerName());
        Picasso.get().load(bannerList.get(position).getBannerImage()).into(viewHolder.imageViewBackground);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPositionContent(position);
            }
        });
    }

    @Override
    public int getCount() {
        if (bannerList == null) {
            return 0;
        }
        return bannerList.size();
    }

    class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        View itemView;
        ImageView imageViewBackground;
        TextView textViewDescription;

        public SliderAdapterVH(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.image_banner);
            textViewDescription = itemView.findViewById(R.id.text_banner);
            this.itemView = itemView;
        }
    }

    private void setPositionContent(int pos) {
        if (bannerList.get(pos).getBannerName().equals("banner1")) {
            Intent intent = new Intent(context, GamesActivity.class);
            context.startActivity(intent);
        } else {
            Log.i("(-.-)?", "setPositionContent: " + pos);
        }
    }
}
