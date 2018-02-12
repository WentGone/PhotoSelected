package cn.mdruby.pickphotovideoview.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.mdruby.pickphotovideoview.DirImage;
import cn.mdruby.pickphotovideoview.GroupMedia;
import cn.mdruby.pickphotovideoview.MediaModel;
import cn.mdruby.pickphotovideoview.OnRVItemClickListener;
import cn.mdruby.pickphotovideoview.PickPreferences;
import cn.mdruby.pickphotovideoview.R;
import cn.mdruby.pickphotovideoview.abstracts.OnRVListClickListener;

/**
 * Created by Administrator on 2018/1/7.
 */

public class RVPhotoListAdapter extends RecyclerView.Adapter {
    private Context context;
    private List<String> dirImageStrings;
    private HashMap<String,List<MediaModel>> groupImages;

//    private GroupMedia groupImage;
//    private DirImage dirImage;


    public RVPhotoListAdapter(Context context, HashMap<String, List<MediaModel>> groupImages,List<String> dirImageStrings) {
        this.context = context;
        this.dirImageStrings = dirImageStrings;
        this.groupImages = groupImages;
    }

    public RVPhotoListAdapter(Context context, GroupMedia groupImage, DirImage dirImage) {
        this.context = context;
//        this.groupImage = groupImage;
//        this.dirImage = dirImage;
    }

    public RVPhotoListAdapter(Context context) {
        this.context = context;
//        this.groupImage = PickPreferences.getInstance(context).getListImage();
//        this.dirImage = PickPreferences.getInstance(context).getDirImage();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RVPhotoListViewHolder(LayoutInflater.from(context).inflate(R.layout.item_photo_list_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RVPhotoListViewHolder){
            ((RVPhotoListViewHolder) holder).bindView(position);
        }
    }

    @Override
    public int getItemCount() {
        return dirImageStrings == null?0:dirImageStrings.size();
        /*if(dirImage != null ) {
            return dirImage.dirName.size();
        }else {
            return 0;
        }*/
    }

    public List<MediaModel> getItem(int position){
//        String dirName = dirImage.dirName.get(position);
//        return groupImage.getGroupMedias().get(dirName);
        String dirName = dirImageStrings.get(position);
        return groupImages.get(dirName);
    }

    public String getDirName(int position){
//        return dirImage.dirName.get(position);
        return dirImageStrings.get(position);
    }

    private class RVPhotoListViewHolder extends RecyclerView.ViewHolder{
        private ImageView mIV;
        private TextView mTV;
        private View mViewRoot;

        public RVPhotoListViewHolder(View itemView) {
            super(itemView);
            mIV = itemView.findViewById(R.id.item_photo_list_layout_IV);
            mTV = itemView.findViewById(R.id.item_photo_list_layout_TV);
            mViewRoot = itemView.findViewById(R.id.item_photo_list_layout_Root);
        }

        private void bindView(final int position){
//            String dirName = dirImage.dirName.get(position);
            String dirName = dirImageStrings.get(position);
//            ArrayList<String> paths = groupImage.mGroupMap.get(dirName);
//            List<MediaModel> paths = groupImage.getGroupMedias().get(dirName);
            List<MediaModel> paths = groupImages.get(dirName);
            mTV.setText(dirName+" "+String.format(context.getString(R.string.pick_photo_size),paths.size() + ""));
            Glide.with(context).load(Uri.parse("file://" + paths.get(0).getThumPath())).into(mIV);
//            itemView.setTag(R.id.pick_dir_name,dirName);
            mViewRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onRVListClickListener != null){
                        onRVListClickListener.onClickItem(position);
                    }
                }
            });
        }
    }
    private OnRVItemClickListener onRVListClickListener;

    public void setOnRVListClickListener(OnRVListClickListener onRVListClickListener) {
        this.onRVListClickListener = onRVListClickListener;
    }
}
