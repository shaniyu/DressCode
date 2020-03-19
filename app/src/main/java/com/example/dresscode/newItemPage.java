package com.example.dresscode;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class newItemPage extends AppCompatActivity {
    private Spinner seasonSpinner, typeSppiner , colorSpinner, categorySpinner, shelveNumberSpinner;
    private int numberOfShelves, qr;
    private String personalRating;
    private EditText descriptionEditTxt;
    private RatingBar ratingBar;
    private Button browseBtn, saveBtn, rotateLeftBtn, rotateRightBtn;
    private ConstraintLayout rotationView;
    private byte[] byteArray;
    private TextView selectImageTxt;
    private Uri selectedImageUri;
    private Bitmap originBitmap, selectedImageBitmap;
    private ImageView selectedItemImageView;
    private Boolean didTaskFail = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item_page);
        getSupportActionBar().hide();
        descriptionEditTxt = findViewById(R.id.descriptionEditTxt);
        selectImageTxt = findViewById(R.id.selectImageTxt);

        saveBtn = (Button) findViewById((R.id.removeBtn));
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidInput())
                {
                    insertItemIntoDBTask task = new insertItemIntoDBTask();
                    try {
                        String query = new String(constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=newItem" + "&email=" + Closet.getInstance().getUserEmail() + "&item_id=" + qr + "&type=" + typeSppiner.getSelectedItem().toString() + "&last_laundry_date=" + "&shelf_number=" + shelveNumberSpinner.getSelectedItem() + "&is_in_closet=" + 1 + "&personal_rate=" + ((int) ratingBar.getRating()) + "&last_worn_date=" + "&season=" + seasonSpinner.getSelectedItem().toString() + "&category=" + categorySpinner.getSelectedItem().toString() + "&color=" + colorSpinner.getSelectedItem().toString() + "&comment=" + descriptionEditTxt.getText());
                        task.execute(query);
                    }
                    catch (Exception e){
                        Toast.makeText(newItemPage.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        browseBtn = (Button) findViewById((R.id.browseBtn));
        browseBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent();
                intent1.setType("image/*");
                intent1.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent1, ""), constants.PICK_IMAGE_CODE);
            }
        });
        //get parameters from last activity
        Intent intent = getIntent();
        numberOfShelves = Closet.getInstance().getUserSettings().getNumberOfShelves();
        qr = intent.getIntExtra("qrCode",0);

        initializeRotationButtons();
        initializeSpinners();
    }

    private void initializeRotationButtons() {
        rotationView = (ConstraintLayout)findViewById(R.id.rotationView);
        rotateLeftBtn = (Button)findViewById(R.id.rotateLeftBtn);
        rotateRightBtn = (Button)findViewById(R.id.rotateRightBtn);

        rotateLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateImageAndBitmap(-90);
            }
        });

        rotateRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateImageAndBitmap(90);
            }
        });
    }

    private void rotateImageAndBitmap(float degrees) {
        Matrix matrix = new Matrix();
        matrix.setRotate(degrees);
        originBitmap = Bitmap.createBitmap(originBitmap, 0, 0, originBitmap.getWidth(), originBitmap.getHeight(), matrix, true);
        selectedItemImageView.setRotation(selectedItemImageView.getRotation() + degrees);

        if (originBitmap != null)
        {
            try
            {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                originBitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                byteArray = byteArrayOutputStream.toByteArray();
            }
            catch (Exception e)
            {
                Log.w("OOooooooooo","exception");
            }
        }
    }


    private void initializeSpinners()
    {
        List<String> seasons = new ArrayList<>();
        seasons.add(0,"Select season");
        seasons.addAll(ESeason.names());
        ArrayAdapter seasonAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item,seasons);
        seasonSpinner = (Spinner) findViewById(R.id.spinner_dropdown_season);
        seasonSpinner.setAdapter(seasonAdapter);
        seasonSpinner.setSelection(0, false);

        List<String> types = new ArrayList<>();
        types.add(0,"Select type");
        types.addAll(EType.names());
        ArrayAdapter typeAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, types);
        typeSppiner = (Spinner) findViewById(R.id.spinner_dropdown_type);
        typeSppiner.setAdapter(typeAdapter);
        typeSppiner.setSelection(0,false);

        List<String> categories = new ArrayList<>();
        categories.add(0,"Select category");
        categories.addAll(ECategory.names());
        ArrayAdapter categoryAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, categories);
        categorySpinner = (Spinner) findViewById(R.id.spinner_dropdown_category);
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setSelection(0, false);

        List<String> colors = new ArrayList<>();
        colors.add(0,"Select color");
        colors.addAll(EColor.names());
        ArrayAdapter colorAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, colors);
        colorSpinner = (Spinner) findViewById(R.id.spinner_dropdown_color);
        colorSpinner.setAdapter(colorAdapter);
        colorSpinner.setSelection(0, false);

        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setMax(5);

        //if rating value is changed, display the current rating value
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                personalRating = (String.valueOf(rating));

            }
        });

        List shelveNumberList = new ArrayList();
        shelveNumberList.add(0, "Select shelf");
        for (int i = 1; i <= numberOfShelves; i++) {
            shelveNumberList.add(Integer.toString(i));
        }
        ArrayAdapter<Integer> shelveSpinnerArrayAdapter = new ArrayAdapter<Integer>(
                this, android.R.layout.simple_spinner_item, shelveNumberList);
        shelveNumberSpinner = (Spinner)findViewById(R.id.spinner_dropdown_shelve);
        shelveNumberSpinner.setAdapter(shelveSpinnerArrayAdapter);
        shelveNumberSpinner.setSelection(0, false);

    }
    private boolean isValidInput() {
        boolean result = true;
        if (typeSppiner.getSelectedItemPosition() == 0)
        {
            result = false;
            ((TextView) typeSppiner.getChildAt(0)).setTextColor(Color.parseColor("#f95757"));
        }
        if (categorySpinner.getSelectedItemPosition() == 0)
        {
            result = false;
            ((TextView) categorySpinner.getChildAt(0)).setTextColor(Color.parseColor("#f95757"));

        }
        if ( seasonSpinner.getSelectedItemPosition() == 0)
        {
            result = false;
            ((TextView) seasonSpinner.getChildAt(0)).setTextColor(Color.parseColor("#f95757"));
        }
        if (colorSpinner.getSelectedItemPosition() == 0)
        {
            result = false;
            ((TextView) colorSpinner.getChildAt(0)).setTextColor(Color.parseColor("#f95757"));
        }
        if (shelveNumberSpinner.getSelectedItemPosition() == 0)
        {
            result = false;
            ((TextView) shelveNumberSpinner.getChildAt(0)).setTextColor(Color.parseColor("#f95757"));
        }
        if ( selectedImageUri == null )
        {
            result = false;
            selectImageTxt.setText("Select an image");
            selectImageTxt.setTextColor(Color.parseColor("#f95757"));
        }
        if(descriptionEditTxt.getText().length() > constants.maxDescriptionLength)
        {
            result = false;
            descriptionEditTxt.setText("");
            descriptionEditTxt.setHint("Text length must be up to 33 characters");
            descriptionEditTxt.setHintTextColor(Color.parseColor("#FF7F7F"));
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == constants.PICK_IMAGE_CODE) {
            if (resultCode != Activity.RESULT_CANCELED) {
                InputStream imageStream;
                originBitmap = null;
                selectedImageUri = data.getData();
                selectImageTxt.setText("");
                // show the selected image in a small image view
                showSelectedImageInAView();

                try
                {
                    imageStream = getContentResolver().openInputStream(selectedImageUri);
                    originBitmap = BitmapFactory.decodeStream(imageStream);
                    //Trying to resize the image
                    originBitmap = Bitmap.createScaledBitmap(originBitmap, constants.imageCompressingSizeInPixels, constants.imageCompressingSizeInPixels, false);
                    // set the view of the selected item
                }
                catch(FileNotFoundException e)
                {
                    System.out.println(e.getMessage().toString());
                }
                if (originBitmap != null)
                {
                    try
                    {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        originBitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byteArray = byteArrayOutputStream.toByteArray();
                    }
                    catch (Exception e)
                    {
                        Log.w("OOooooooooo","exception");
                    }
                }
                // End getting the selected image, setting in imageview and converting it to byte and base 64
            }
            else
            {
                System.out.println("Error Occurred");
            }
        }
        else
        {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if(result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(this, "You canceled the scanning", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                }
            }
            else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void showSelectedImageInAView()
    {
        try
        {
            selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            // trying to compress it
            selectedImageBitmap = Bitmap.createScaledBitmap(selectedImageBitmap, constants.smallImageViewSize, constants.smallImageViewSize, false);

            selectedItemImageView = findViewById(R.id.selectedItemView);
            selectedItemImageView.setImageBitmap(selectedImageBitmap);

            rotationView.setVisibility(View.VISIBLE);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // Class for inserting item into DB
    private class insertItemIntoDBTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String output = null;
            for (String url : urls) {
                url = url.replace(" ","%20");
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
                httpConnection.setDoInput(true);
                httpConnection.setRequestMethod("POST");
                httpConnection.setDoOutput(true);
                httpConnection.setRequestProperty("Content-Type", "image/jpeg");
                OutputStream outputStream = httpConnection.getOutputStream();
                outputStream.write(byteArray);
                outputStream.close();
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
                Closet.getInstance().setItemsUpdated(false);
                finish();
            }
        }
    }

    private void popErrorDialog(Boolean isPost) {
        ErrorDialog errorDialog = new ErrorDialog(newItemPage.this, newItemPage.this, isPost);
        // do when errorDialog finish its work
        errorDialog.setDialogResult(new ErrorDialog.OnMyDialogResult(){
            public void finish(String result){
                newItemPage.this.finish();
            }
        });
        errorDialog.show();
    }
}
