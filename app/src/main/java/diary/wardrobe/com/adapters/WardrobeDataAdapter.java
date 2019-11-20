package diary.wardrobe.com.adapters;

/**
 * Created by user on 2/24/2017.
 */

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.HashMap;

import diary.wardrobe.com.activities.WardrobeActivity;
import diary.wardrobe.com.realmModels.WardrobeItemUri;
import diary.wardrobe.com.wardrobediary.R;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;


public class WardrobeDataAdapter extends RealmRecyclerViewAdapter<WardrobeItemUri, WardrobeDataAdapter.MyViewHolder> {

    private final Context activity;
    private boolean headerRequired;
    public HashMap<Integer, String> hashMap;
    boolean cbList;
    public boolean longClick = false;

    public WardrobeDataAdapter(Context activity, OrderedRealmCollection<WardrobeItemUri> data, boolean headerRequired, boolean cbList) {
        super(activity, data, true);
        this.activity = activity;
        this.headerRequired = headerRequired;
        hashMap = new HashMap<>();
        this.cbList = cbList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wardrobr_adapter_layout, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        WardrobeItemUri obj = getData().get(position);
        if (position == 0 && headerRequired) {
            //  holder.headerLayout.setVisibility(View.VISIBLE);
        }
        if (Uri.parse(obj.getItemsUri().toString()) != null) {
            Uri uri = Uri.parse("file://"+obj.getItemsUri().toString());
            holder.Photo.setImageURI(uri);
            holder.Photo.setTag(position);
        }
        holder.checkBox.setTag(position);
        holder.timesUsed.setText(""+obj.getTimesUsed());

        //for checkbox
        if (cbList) {
            holder.checkBox.setVisibility(View.VISIBLE);
        } else {
            holder.Photo.setOnLongClickListener(onlongclickListner);
            holder.checkBox.setVisibility(View.GONE);
        }



        //for dryclean
        if(getData().get(position).isPutToWashAndIron()){
           holder.dryCleanCover.setVisibility(View.VISIBLE);
        }else{
            holder.dryCleanCover.setVisibility(View.GONE);
        }

    }
    View.OnLongClickListener onlongclickListner=new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            if (view.getId() == R.id.country_photo && !longClick) {
            Toast.makeText(context, "Long click event", Toast.LENGTH_SHORT).show();
            ((WardrobeActivity) context).updateAdapter();
            RelativeLayout relativeLayou = (RelativeLayout) view.getParent();
            CheckBox cb = (CheckBox) relativeLayou.getChildAt(1);
            cb.setChecked(true);
            longClick = true;
        }

            return true;
        }
    };

    class MyViewHolder extends RecyclerView.ViewHolder /*implements View.OnLongClickListener */{
        public TextView desp;
        public SimpleDraweeView Photo;

        //btn date plan
        TextView btnDatePlan,timesUsed;
        RelativeLayout headerLayout;
        CheckBox checkBox;
        FrameLayout dryCleanCover;

        public MyViewHolder(View view) {
            super(view);
            // desp = (TextView) itemView.findViewById(R.id.country_name);
            Photo = (SimpleDraweeView) itemView.findViewById(R.id.country_photo);
//            Photo.setOnLongClickListener(this);
            btnDatePlan = (TextView) itemView.findViewById(R.id.datebtnplan);
            checkBox = (CheckBox) itemView.findViewById(R.id.seleCheck);
            dryCleanCover=(FrameLayout) itemView.findViewById(R.id.drycleancover);
            timesUsed=(TextView) itemView.findViewById(R.id.timesused);


//            headerLayout=(RelativeLayout)itemView.findViewById(R.id.title_layout);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    int position = (int) compoundButton.getTag();
                    if (b) {

                        hashMap.put(getData().get(position).getId(), getData().get(position).getItemsUri());
                    } else {
                        if (hashMap.containsKey(position))
                            hashMap.remove(position);
                    }

                    if (longClick) {
                        ((WardrobeActivity) context).refreshMenu();
                    }


                }
            });


        }

        /*@Override
        public boolean onLongClick(View v) {
//            activity.deleteItem(data);
            if (v.getId() == R.id.country_photo && !longClick) {
                Toast.makeText(context, "Long click event", Toast.LENGTH_SHORT).show();
                ((WardrobeActivity) context).updateAdapter();
                RelativeLayout relativeLayou = (RelativeLayout) v.getParent();
                CheckBox cb = (CheckBox) relativeLayou.getChildAt(1);
                cb.setChecked(true);
                longClick = true;
            }
            return true;
        }*/
    }
    public void setCheckBoxEnable(boolean value){
        cbList=value;
        longClick=value;
        notifyDataSetChanged();
    }
}
