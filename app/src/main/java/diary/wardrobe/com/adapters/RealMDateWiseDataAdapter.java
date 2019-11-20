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
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import diary.wardrobe.com.realmModels.Item;
import diary.wardrobe.com.wardrobediary.MainActivity;
import diary.wardrobe.com.wardrobediary.R;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;


public class RealMDateWiseDataAdapter extends RealmRecyclerViewAdapter<Item, RealMDateWiseDataAdapter.MyViewHolder> {

    private final Context activity;

    public RealMDateWiseDataAdapter(Context activity, OrderedRealmCollection<Item> data) {
        super(activity, data,true);
        this.activity = activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        if(activity instanceof MainActivity) {
             itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_view_list_home, parent, false);
        }else{
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_view_list, parent, false);
        }
        return new MyViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Item obj = getData().get(position);
        Uri uri = Uri.parse("file://"+obj.getUri().toString());
        holder.Photo.setImageURI(uri);
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        public TextView desp;
        public SimpleDraweeView Photo;

        public MyViewHolder(View view) {
            super(view);
           // desp = (TextView)itemView.findViewById(R.id.country_name);
            Photo = (SimpleDraweeView)itemView.findViewById(R.id.country_photo);
        }

        @Override
        public boolean onLongClick(View v) {
//            activity.deleteItem(data);
            return true;
        }
    }
}
