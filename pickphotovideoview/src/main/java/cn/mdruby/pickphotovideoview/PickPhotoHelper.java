package cn.mdruby.pickphotovideoview;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by wanbo on 2016/12/31.
 * 修改  by Went_Gone
 */
public class PickPhotoHelper {
    private static final String TAG = "PickPhotoHelper";
    private Activity activity;
    private static PickPhotoListener listener;
    private boolean isShowVideo = false;
    private HashMap<String,List<MediaModel>> mGroupMapMedia = new LinkedHashMap<>();
    private Observable<List<MediaModel>> observable;
    private Subscriber subscriber;

    public PickPhotoHelper(Activity activity, PickPhotoListener listener) {
        this.activity = activity;
        this.listener = listener;
    }

    /*public PickPhotoHelper(final Activity activity, Subscriber subscriber){
        this.activity = activity;
        this.subscriber = subscriber;
    }*/

    public void getImages(final boolean showGif) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = activity.getContentResolver();

                //jpeg & png & gif
                Cursor mCursor;
                Cursor cursor = null;

                String[] photoColumns = new String[]{
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.TITLE,
                        MediaStore.Images.Media.MIME_TYPE
                };
                String photoSelectedColumns = "";
                if (showGif){
                    photoSelectedColumns = MediaStore.Images.Media.MIME_TYPE + "=? or "
                            + MediaStore.Images.Media.MIME_TYPE + "=? or "
                            + MediaStore.Images.Media.MIME_TYPE + "=?";
                }else {
                    photoSelectedColumns = MediaStore.Images.Media.MIME_TYPE + "=? or "
                            + MediaStore.Images.Media.MIME_TYPE + "=? or"
                            + MediaStore.Video.Media.MIME_TYPE +"=?";
                }
                String[] selectionArgs = null;
                if (showGif){
                    selectionArgs = new String[]{"image/jpeg", "image/png", "image/gif"};
                }else {
                    selectionArgs = new String[]{"image/jpeg", "image/png","video/mp4"};
                }
                String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " desc";

                String[] mediaColumns = new String[]{
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.TITLE,
                        MediaStore.Images.Media.MIME_TYPE,
                        MediaStore.Images.Media.DATE_ADDED,
                };


                if(showGif){
                    mCursor = mContentResolver.query(mImageUri, mediaColumns,photoSelectedColumns,
                            selectionArgs, sortOrder);
                }else {
                    mCursor = mContentResolver.query(mImageUri, mediaColumns,photoSelectedColumns,
                            selectionArgs,sortOrder);
                }


                if (mCursor == null) {
                    return;
                }
                ArrayList<String> dirNames = new ArrayList<>();
                while (mCursor.moveToNext()) {
                    // get image path
                    String path = mCursor.getString(mCursor
                            .getColumnIndex(MediaStore.Images.Media.DATA));

                    long addDate = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                    String mimeType = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));

                    File file = new File(path);
                    if(!file.exists()){
                        continue;
                    }

                    // get image parent name
                    String parentName = new File(path).getParentFile().getName();
                    // save all Photo
                    if (!mGroupMapMedia.containsKey(PickConfig.ALL_PHOTOS)) {
                        dirNames.add(PickConfig.ALL_PHOTOS);
                        ArrayList<MediaModel> mediaModels=new ArrayList<MediaModel>();
                        MediaModel mediaModel = new MediaModel();
                        mediaModel.setFile(file);
                        mediaModel.setPath(path);
                        mediaModel.setThumPath(path);
                        mediaModel.setAddDate(addDate);
                        mediaModel.setMimeType(mimeType);
                        mediaModels.add(mediaModel);
                        mGroupMapMedia.put(PickConfig.ALL_PHOTOS, mediaModels);
                    } else {
                        MediaModel mediaModel = new MediaModel();
                        mediaModel.setFile(file);
                        mediaModel.setPath(path);
                        mediaModel.setThumPath(path);
                        mediaModel.setAddDate(addDate);
                        mediaModel.setMimeType(mimeType);
                        mGroupMapMedia.get(PickConfig.ALL_PHOTOS).add(mediaModel);
//                        mGroupMapMedia.get(PickConfig.ALL_PHOTOS).add(path);
                    }
                    // save by parent name
                    if (!mGroupMapMedia.containsKey(parentName)) {
                        dirNames.add(parentName);
                        ArrayList<MediaModel> mediaModels=new ArrayList<MediaModel>();
                        MediaModel mediaModel = new MediaModel();
                        mediaModel.setFile(file);
                        mediaModel.setPath(path);
                        mediaModel.setThumPath(path);
                        mediaModel.setAddDate(addDate);
                        mediaModel.setMimeType(mimeType);
                        mediaModels.add(mediaModel);
                        mGroupMapMedia.put(parentName, mediaModels);
                    } else {
                        MediaModel mediaModel = new MediaModel();
                        mediaModel.setFile(file);
                        mediaModel.setPath(path);
                        mediaModel.setThumPath(path);
                        mediaModel.setAddDate(addDate);
                        mediaModel.setMimeType(mimeType);
                        mGroupMapMedia.get(parentName).add(mediaModel);
                    }
                }
                mCursor.close();


                //视频
                if (isShowVideo){
                    String[] thumbColumns = new String[]{
                            MediaStore.Video.Thumbnails.DATA,
                            MediaStore.Video.Thumbnails.VIDEO_ID
                    };
                    String[] videoColumns = new String[]{
                            MediaStore.Video.Media.DATA,
                            MediaStore.Video.Media._ID,
                            MediaStore.Video.Media.TITLE,
                            MediaStore.Video.Media.MIME_TYPE,
                            MediaStore.Video.Media.DATE_ADDED,
                            MediaStore.Video.Media.DURATION
                    };

                    cursor = mContentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoColumns, null, null, null);

                    while (cursor.moveToNext()){
                        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                        long addDate = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
                        String mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE));
                        long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));

                        String converted = String.format("%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(duration),
                                TimeUnit.MILLISECONDS.toSeconds(duration) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
                        );

                        File file = new File(path);
                        if(!file.exists()){
                            continue;
                        }
                        String parentName = new File(path).getParentFile().getName();
                        Log.d(PickConfig.TAG, parentName + ":" + path);

                        //缩略图
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                        String selection = MediaStore.Video.Thumbnails.VIDEO_ID +"=?";
                        String[] selectionArgs1 = new String[]{
                                id+""
                        };
                        Cursor thumbCursor = mContentResolver.query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, thumbColumns, selection, selectionArgs1, null);
                        String thumbPath = "";
                        if(thumbCursor.moveToFirst()){
                            thumbPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA));
                        }

                        // save all Photo
                        if (!mGroupMapMedia.containsKey(PickConfig.ALL_PHOTOS)) {
                            dirNames.add(PickConfig.ALL_PHOTOS);
                            ArrayList<MediaModel> mediaModels=new ArrayList<MediaModel>();
                            MediaModel mediaModel = new MediaModel();
                            mediaModel.setFile(file);
                            mediaModel.setPath(path);
                            mediaModel.setThumPath(thumbPath);
                            mediaModel.setAddDate(addDate);
                            mediaModel.setMimeType(mimeType);
                            mediaModel.setDuration(duration);
                            mediaModel.setDurationStr(converted);
                            mediaModels.add(mediaModel);
                            mGroupMapMedia.put(PickConfig.ALL_PHOTOS, mediaModels);
                        } else {
                            MediaModel mediaModel = new MediaModel();
                            mediaModel.setFile(file);
                            mediaModel.setPath(path);
                            mediaModel.setThumPath(thumbPath);
                            mediaModel.setAddDate(addDate);
                            mediaModel.setMimeType(mimeType);
                            mediaModel.setDuration(duration);
                            mediaModel.setDurationStr(converted);
                            mGroupMapMedia.get(PickConfig.ALL_PHOTOS).add(mediaModel);
                        }
                        // save by parent name
                        if (!mGroupMapMedia.containsKey(parentName)) {
                            dirNames.add(parentName);
                            ArrayList<MediaModel> mediaModels=new ArrayList<MediaModel>();
                            MediaModel mediaModel = new MediaModel();
                            mediaModel.setFile(file);
                            mediaModel.setPath(path);
                            mediaModel.setThumPath(thumbPath);
                            mediaModel.setAddDate(addDate);
                            mediaModel.setMimeType(mimeType);
                            mediaModel.setDuration(duration);
                            mediaModel.setDurationStr(converted);
                            mediaModels.add(mediaModel);
                            mGroupMapMedia.put(parentName, mediaModels);
                        } else {
                            MediaModel mediaModel = new MediaModel();
                            mediaModel.setFile(file);
                            mediaModel.setPath(path);
                            mediaModel.setThumPath(thumbPath);
                            mediaModel.setAddDate(addDate);
                            mediaModel.setMimeType(mimeType);
                            mediaModel.setDuration(duration);
                            mediaModel.setDurationStr(converted);
                            mGroupMapMedia.get(parentName).add(mediaModel);
                        }
                    }
                    cursor.close();
                }

                Iterator<Map.Entry<String, List<MediaModel>>> iterator = mGroupMapMedia.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, List<MediaModel>> entry = iterator.next();
                    entry.getKey();
                    List<MediaModel> value = entry.getValue();
                    Collections.sort(value, new SortByDate());
                }

                GroupMedia groupMedia = new GroupMedia();
                groupMedia.setGroupMedias(mGroupMapMedia);
                PickPreferences.getInstance(activity).saveImageList(groupMedia);
                DirImage dirImage = new DirImage();
                dirImage.dirName = dirNames;
                PickPreferences.getInstance(activity).saveDirNames(dirImage);
                r.sendEmptyMessage(0);
            }
        }).start();
    }

    public void setShowVideo(boolean b) {
        this.isShowVideo = b;
    }

    /**
     * 日期比较器
     */
    class SortByDate implements Comparator {
        public int compare(Object o1, Object o2) {
            MediaModel s1 = (MediaModel) o1;
            MediaModel s2 = (MediaModel) o2;
            if((s1.getAddDate()-s2.getAddDate())<0)
//                return -1;
                return 1;
            else if((s1.getAddDate()-s2.getAddDate())>0)
//                return 1;
                return -1;
            else return 0;
        }
    }

    private static Handler r = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0){

                listener.pickSuccess();
            }
        }
    };
}
