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

public class SetListWithCheckBoxAdapter extends ArrayAdapter<Set> {

    private Context mContext;
    private int mResource;
    private List<Set> mSetList;
    private int lastPosition = -1;
    private Bitmap image;
    private Boolean didTaskFail = false, isFirst = true;
    private Activity activity;

    /**
     * Holds variables in a View
     */
    private static class ViewHolder {
        public ImageView image1;
        public ImageView image2;
        public ImageView image3;
        int nextIndexToHandle;
        ArrayList<ImageView> imageViewList;
        private ArrayList<GetPictureTask> tasks = new ArrayList<>(3);
        public CheckBox checkBox;
    }
    /**
     * Default constructor for the PersonListAdapter
     * @param context
     * @param resource
     * @param setList
     */
    public SetListWithCheckBoxAdapter(Context context, int resource, List<Set> setList, Activity activity) {
        super(context, resource, setList);
        mContext = context;
        mResource = resource;
        mSetList = setList;
        this.activity = activity;

        for (Set set : mSetList){
            set.setIsChecked(false);
        }
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //create the view result for showing the animation
        final View result;

        //ViewHolder object
        final ViewHolder holder;

        //Current set
        final Set set = mSetList.get(position);;

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
            holder = new SetListWithCheckBoxAdapter.ViewHolder();
            initializeHolderSettings(holder, convertView, parent);
            holder.checkBox.setTag(set);
        }
        else{
            holder = (SetListWithCheckBoxAdapter.ViewHolder) convertView.getTag();
        }

        holder.checkBox.setChecked(set.getIsChecked());

        result = convertView;

        //Cancelling any other previous tasks that are running on this specific holder
        for (GetPictureTask taskToCancel : holder.tasks) {
            taskToCancel.cancel(true);
        }

        Animation animation = AnimationUtils.loadAnimation(mContext,
                (position > lastPosition) ? R.anim.load_up_anim : R.anim.load_down_anim);
        result.startAnimation(animation);
        lastPosition = position;

        holder.nextIndexToHandle = 0;
        GetPictureTask task = new GetPictureTask(holder, set);
        holder.tasks.add(task);
        //There is always at least one picture in a set (because there is at least one item) so we can populate the first picture.
        task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getPicture&email=" + Closet.getInstance().getUserEmail() + "&item_id="+ set.getItems().get(holder.nextIndexToHandle).getId()});

        if (set.getItems().size() < 2) { //if the set has only one item
            holder.image2.setImageDrawable(mContext.getResources().getDrawable(R.drawable.image_failed));
            holder.image3.setImageDrawable(mContext.getResources().getDrawable(R.drawable.image_failed));
        }
        else if (set.getItems().size() < 3){ //if the set has 2 items
            holder.image3.setImageDrawable(mContext.getResources().getDrawable(R.drawable.image_failed));

        }

        return convertView;
    }

    public void initializeHolderSettings(ViewHolder holder, View convertView, ViewGroup parent)
    {
        holder.image1 = (ImageView) convertView.findViewById(R.id.list_view_set_image1);
        holder.image2 = (ImageView) convertView.findViewById(R.id.list_view_set_image2);
        holder.image3 = (ImageView) convertView.findViewById(R.id.list_view_set_image3);
        holder.imageViewList = new ArrayList<>();
        holder.imageViewList.add(holder.image1);
        holder.imageViewList.add(holder.image2);
        holder.imageViewList.add(holder.image3);
        holder.checkBox = (CheckBox)convertView.findViewById(R.id.list_view_item_checkbox);
        holder.checkBox.setOnCheckedChangeListener((ChooseSetsToDelete)mContext);

        convertView.setTag(holder);
    }

    public void updateSetList(List<Set> setListToUpdate){
        mSetList = setListToUpdate;
    }

    private class GetPictureTask extends AsyncTask<String, Void, byte[]> {
        ViewHolder holder;
        Set set;

        public GetPictureTask(ViewHolder holder, Set set){
            this.holder = holder;
            this.set = set;
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
        protected void onPostExecute(byte[] imageBytes)
        {
            if(didTaskFail){
                didTaskFail = false;
                holder.imageViewList.get(holder.nextIndexToHandle).setImageResource(R.drawable.showallclothes);
                if(isFirst){
                    isFirst = false;
                    popErrorDialog(false);
                }

                holder.nextIndexToHandle++;
                if (holder.nextIndexToHandle < 3) {
                    if ((set.getItems().size() == 2 && holder.nextIndexToHandle == 1) || (set.getItems().size() >= 3)) {

                        GetPictureTask task = new GetPictureTask(holder, set);
                        holder.tasks.add(task);
                        task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getPicture&email=" + Closet.getInstance().getUserEmail() + "&item_id=" + set.getItems().get(holder.nextIndexToHandle).getId()});
                    }
                }
            }
            else {
                try {
                    image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    holder.imageViewList.get(holder.nextIndexToHandle).setImageBitmap(image);
                    holder.nextIndexToHandle++;

                    if (holder.nextIndexToHandle < 3) {
                        if (set.getItems().size() == 2 && holder.nextIndexToHandle == 1) { //if the set has 2 items

                            GetPictureTask task = new GetPictureTask(holder, set);
                            holder.tasks.add(task);
                            task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getPicture&email=" + Closet.getInstance().getUserEmail() + "&item_id=" + set.getItems().get(holder.nextIndexToHandle).getId()});
                        } else if (set.getItems().size() >= 3 && holder.nextIndexToHandle < 3) { //if the set has at least 3 items we want to display the first 3 item images

                            GetPictureTask task = new GetPictureTask(holder, set);
                            holder.tasks.add(task);
                            task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getPicture&email=" + Closet.getInstance().getUserEmail() + "&item_id=" + set.getItems().get(holder.nextIndexToHandle).getId()});
                        }
                    }
                }
                catch (Exception ex) {
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
