package com.example.dresscode;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.google.android.gms.common.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class ItemListWithCheckBoxAdapter extends ArrayAdapter<Item> {

    private Context mContext;
    private int mResource;
    private List<Item> mItemList;
    private int lastPosition = -1;
    private String className;
    private int currItemId = 0;
    private Bitmap imageFromServer;
    private Boolean didTaskFail = false, isFirst = true;
    private Activity activity;

    /**
     * Holds variables in a View
     */
    private static class ViewHolder {
        public ImageView image;
        public CheckBox checkBox;
        public ArrayList<GetPictureTask> tasks;

    }
    /**
     * Default constructor for the PersonListAdapter
     * @param context
     * @param resource
     * @param itemList
     */
    public ItemListWithCheckBoxAdapter(Context context, int resource, List<Item> itemList, String className, Activity activity) {
        super(context, resource, itemList);
        mContext = context;
        mResource = resource;
        mItemList = itemList;
        this.className = className;
        this.activity = activity;

        for (Item item : mItemList){
            item.setIsChecked(false);
        }
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //create the view result for showing the animation
        final View result;

        //ViewHolder object
        final ViewHolder holder;

        Item item = mItemList.get(position);

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.list_view_item_image);
            holder.checkBox = (CheckBox)convertView.findViewById(R.id.list_view_item_checkbox);
            holder.tasks = new ArrayList<>();
            holder.checkBox.setTag(item);

            if(className.equals("RemoveItems")) {
                holder.checkBox.setOnCheckedChangeListener((RemoveItems) mContext);
            }
            else if(className.equals("ChooseItemsForSet")){
                holder.checkBox.setOnCheckedChangeListener((ChooseItemsForSet) mContext);
            }

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.checkBox.setChecked(item.getIsChecked());

        result = convertView;

        //Cancelling any other previous tasks that are running on this specific holder
        for (GetPictureTask taskToCancel : holder.tasks) {
            taskToCancel.cancel(true);
        }

        holder.tasks.clear();

        Animation animation = AnimationUtils.loadAnimation(mContext,
                (position > lastPosition) ? R.anim.load_up_anim : R.anim.load_down_anim);
        result.startAnimation(animation);
        lastPosition = position;

        GetPictureTask task = new GetPictureTask(holder.image);
        currItemId = getItem(position).getId();
        holder.tasks.add(task);
        task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getPicture&email=" + Closet.getInstance().getUserEmail() + "&item_id="+ currItemId});

        return convertView;
    }

    public void updateItemList(List<Item> itemListToUpdate){
        mItemList = itemListToUpdate;
    }

    private class GetPictureTask extends AsyncTask<String, Void, byte[]> {
        ImageView imageToPopulate;

        public GetPictureTask(ImageView imageToPopulate) {
            this.imageToPopulate = imageToPopulate;
        }

        @Override
        protected byte[] doInBackground(String... urls) {
            byte[] output = null;
            for (String url : urls) {
                output = getOutputFromUrl(url);
            }
            return output;
        }

        private byte[] getOutputFromUrl(String url) {
            StringBuffer output = new StringBuffer("");
            byte[] imageBytes = null;

            try {
                InputStream stream = getHttpConnection(url);
                if(stream != null) {
                    imageBytes = IOUtils.toByteArray(stream);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return imageBytes;
        }

        // Makes HttpURLConnection and returns InputStream
        private InputStream getHttpConnection(String urlString)
                throws IOException {
            InputStream stream = null;
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            try {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("GET");
                httpConnection.setDoOutput(true);
                httpConnection.connect();
                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                }
            } catch (Exception ex) {
                if(ex.getClass() != InterruptedIOException.class) {
                    didTaskFail = true;
                }
                ex.printStackTrace();
            }
            return stream;
        }

        @Override
        protected void onPostExecute(byte[] imageBytes) {
            if (didTaskFail) {
                didTaskFail = false;
                imageToPopulate.setImageResource(R.drawable.showallclothes);
                if(isFirst){
                    isFirst = false;
                    popErrorDialog(false);
                }
            }
            else {
                try {
                    imageFromServer = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    imageToPopulate.setImageBitmap(imageFromServer);

                } catch (Exception ex) {
                    ex.getMessage();
                }
            }
        }
    }

    private void popErrorDialog(Boolean isPost) {
        ErrorDialog errorDialog = new ErrorDialog(activity, activity, isPost);
        // do when errorDialog finish its work
        errorDialog.setDialogResult(new ErrorDialog.OnMyDialogResult(){
            public void finish(String result){ }
        });
        errorDialog.show();
    }
}