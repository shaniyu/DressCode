package com.example.dresscode;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.common.util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


public class ItemWithButtonListAdapter extends ArrayAdapter<Item> {

    private Context mContext;
    private int mResource;
    private List<Item> mItemList;
    private int lastPosition = -1;
    private int itemIdToRemove, setId;
    private int currItemId = 0;
    private Bitmap imageFromServer;
    private Boolean didTaskFail = false, isFirst = true;
    private Activity activity;
    private Item currItem;

    /**
     * Holds variables in a View
     */
    private static class ViewHolder {
        public ImageView image;
        public Button deleteBtn;
        public ArrayList<GetPictureTask> tasks;
    }
    /**
     * Default constructor for the PersonListAdapter
     * @param context
     * @param resource
     * @param itemList
     */
    public ItemWithButtonListAdapter(Context context, int resource, List<Item> itemList, int setId, Activity activity) {
        super(context, resource, itemList);
        mContext = context;
        mResource = resource;
        mItemList = itemList;
        this.setId = setId;
        this.activity = activity;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        //create the view result for showing the animation
        final View result;

        //ViewHolder object
        final ViewHolder holder;

        Item item = mItemList.get(position);

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
            holder= new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.list_view_item_image_in_set);
            holder.deleteBtn = (Button) convertView.findViewById(R.id.delete_btn);
            holder.tasks = new ArrayList<>();
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        result = convertView;
        handleDeleteButton(holder, position);

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
        task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getPicture&email=" + Closet.getInstance().getUserEmail() +"&item_id="+ currItemId});
        holder.tasks.add(task);

        return convertView;
    }

    public void handleDeleteButton(ViewHolder holder, final int position)
    {
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
                builder.setMessage("Are you sure you want to remove the chosen item from the set?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currItem = mItemList.get(position);

                        itemIdToRemove = currItem.getId();
                        RemoveItemFromSetTask task = new RemoveItemFromSetTask();
                        task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=deleteItemFromSet&email=" + Closet.getInstance().getUserSettings().getEmail() + "&item_id=" + itemIdToRemove + "&set_id=" + setId});
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        });
    }

    public void updateListToDisplay(List<Item> itemList){
        mItemList.clear();
        mItemList.addAll(itemList);
        this.notifyDataSetChanged();
    }

    private void handleEmptyItemList() {
        if(mItemList.size() == 0){
            AlertDialog.Builder builder = new AlertDialog.Builder((manageSet)mContext);
            builder.setMessage("Note that this set will be deleted since it doesn't contain any items");
            builder.setPositiveButton("OK", dialogClickListener);
            builder.show();
        }
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //YES button was clicked
                    ((manageSet)mContext).onBackPressed();
                    break;
            }
        }
    };

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
        protected void onPostExecute(byte[] imageBytes)
        {
            if (didTaskFail){
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

    public class RemoveItemFromSetTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String output = null;
            for (String url : urls) {
                output = getOutputFromUrl(url);
            }
            return output;
        }

        private String getOutputFromUrl(String url) {
            StringBuffer output = new StringBuffer("");
            try {
                InputStream stream = getHttpConnection(url);
                if(stream != null) {
                    BufferedReader buffer = new BufferedReader(
                            new InputStreamReader(stream));
                    String s = "";
                    while ((s = buffer.readLine()) != null)
                        output.append(s);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return output.toString();
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
                httpConnection.connect();

                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                }
            } catch (Exception ex) {
                didTaskFail = true;
                ex.printStackTrace();
            }
            return stream;
        }

        @Override
        protected void onPostExecute(String output) {
            if(didTaskFail || output.equals(constants.DB_EXCEPTION)){
                didTaskFail = false;
                popErrorDialog(true);
            }
            else {
                //Remove the item from the list of items that being displayed
                mItemList.remove(currItem);
                //Updating flag
                Closet.getInstance().setSetsUpdated(false);
                notifyDataSetChanged();
                if (mContext instanceof manageSet) {
                    ((manageSet) mContext).resetCurrentSet(itemIdToRemove);
                }

                handleEmptyItemList();
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