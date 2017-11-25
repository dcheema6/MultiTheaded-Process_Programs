package ca.uwo.eng.se3313.lab2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /**
     * View that showcases the image
     */
    private ImageView ivDisplay;

    /**
     * Skip button
     */
    private ImageButton skipBtn;

    /**
     * Progress bar showing how many seconds left (percentage).
     */
    private ProgressBar pbTimeLeft;

    /**
     * Label showing the seconds left.
     */
    private TextView tvTimeLeft;

    /**
     * Control to change the interval between switching images.
     */
    private SeekBar sbWaitTime;

    /**
     * Editable text to change the interval with {@link #sbWaitTime}.
     */
    private EditText etWaitTime;

    /**
     * List of image URLs of cute animals that will be displayed.
     */
    private static final List<String> urlList = new ArrayList<String>() {{
        add("http://i.imgur.com/CPqbVW8.pg");
        add("http://i.imgur.com/Ckf5OeO.jpg");
        add("http://i.imgur.com/3jq1bv7.jpg");
        add("http://i.imgur.com/8bSITuc.jpg");
        add("http://i.imgur.com/JfKH8wd.jpg");
        add("http://i.imgur.com/KDfJruL.jpg");
        add("http://i.imgur.com/o6c6dVb.jpg");
        add("http://i.imgur.com/B1bUG2K.jpg");
        add("http://i.imgur.com/AfxvVuq.jpg");
        add("http://i.imgur.com/DSDtm.jpg");
        add("http://i.imgur.com/SAVYw7S.jpg");
        add("http://i.imgur.com/4HznKil.jpg");
        add("http://i.imgur.com/meeB00V.jpg");
        add("http://i.imgur.com/CPh0SRT.jpg");
        add("http://i.imgur.com/8niPBvE.jpg");
        add("http://i.imgur.com/dci41f3.jpg");
    }};

    private ImageDownload imageDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Insert your code here (and within the class!)
        //Create Handler for event seekbar and Edit view
        ((SeekBar) findViewById(R.id.sbWaitTime)).setOnSeekBarChangeListener(new SeekBarListener());
        ((EditText) findViewById(R.id.etWaitTime)).setOnEditorActionListener(new EditTextKeyListener());

        //creating a new thread and passing a Handler to post back to UIThread
        Handler uiThread = new Handler();
        imageDownload = new ImageDownload(uiThread);
        imageDownload.start();
    }

    /**
    * Action listener to be used for EditView evEditText
     * returns true when enter key is pressed and false if any other key is pressed on touch keyboard by the user
     *
     * When user press enter key,
     * if the entered string in editView is not empty and is a integer between 5 and 60 inclusive
     * function updates the seek bar and calls resetTimer() method for the background thread imageDownload
     * then it hides the touch keyboard
    * */
    public class EditTextKeyListener implements TextView.OnEditorActionListener{
    private EditText myEditText;

        @Override
        public boolean onEditorAction(TextView textView, int key, KeyEvent keyEvent) {
            myEditText = (EditText) findViewById(R.id.etWaitTime);

            //When user click done key value = 6
            if(key == 6) {
                //str is value entered by user in editview
                String str = (myEditText).getText().toString();
                //Check for empty string
                if (!(str.matches(""))) {
                    int text = Integer.parseInt(str);
                    //Display error message if conditions not met
                    if (text < 5 || text > 60) {
                        (myEditText).setError("Must specify a number between 5 and 60");
                    } else {
                        //Clear error message
                        (myEditText).setError(null);
                        //change seekbar progress
                        ((SeekBar) findViewById(R.id.sbWaitTime)).setProgress(text);
                        //reset the photo display timer for background thread
                        imageDownload.resetTimer(false);
                        //Hide the touch keyboard
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(myEditText.getWindowToken(), 0);
                    }
                } else {
                    //Display error if empty string found
                    (myEditText).setError("Must specify a number between 5 and 60");
                }
                //return is not being used so doesnt matter
                return true;
            }
            return false;
        }
    }

    /**
     * Listener for SeekBar sbWaitTime
     *
     * if initiated by user: on progress being changed it dynamically updates the editView
     *
     * when user is done with updating the seekBar it calls the resetTimer function for background thread imageDownload
     * */
    public class SeekBarListener implements SeekBar.OnSeekBarChangeListener{

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean isInitiatedByUser) {
            //If progress changed is initiated by user handle here/ when is not initiated by user will be handled by respective initiators
            if (isInitiatedByUser) {
                //set min value of bar to 5
                if (progress < 5) {
                    ((SeekBar) findViewById(R.id.sbWaitTime)).setProgress(5);
                }
                //Change the edittext etWaitTime value
                ((EditText) findViewById(R.id.etWaitTime)).setText((new Integer(((SeekBar) findViewById(R.id.sbWaitTime)).getProgress())).toString());
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //When user leaves the touchbar reset the photo display timer
            imageDownload.resetTimer(false);
        }
    }

    /**
     * Background thread downloads the images and post them on uiThread using handled passed in the args
     *
     * Refer to the comments in code for function flow
     * */
    public class ImageDownload extends Thread {
        private Handler uiThread;
        private boolean resetTimer;
        private boolean skipPhoto;
        private int timer;
        private int photoCounter;

        public ImageDownload(Handler handler){
            uiThread = handler;
        }

        public void run() {
            //Thread keeps downloading images until killed by parent Thread
            while (true) {
                skipPhoto = false;
                //reset photo counter when all counter reaches the size of list the images are displayed from
                if (photoCounter >= urlList.size()) {
                    photoCounter = 0;
                }

                ImageView imageView = (ImageView) findViewById(R.id.ivDisplay);
                //Try for a connection, display default image when image not found or when connection is not established
                try {
                    //Get photo and store it as Bitmap
                    final Bitmap bitmap = BitmapFactory.decodeStream(new java.net.URL(urlList.get(photoCounter)).openStream());
                    uiThread.post(() -> {
                        if (bitmap == null) {
                            imageView.setImageResource(R.drawable.cat_error);
                        } else {
                            //Display bitmap
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                }
                catch (Exception e) {
                    uiThread.post(() -> {
                        imageView.setImageResource(R.drawable.cat_error);
                    });
                }
                //This loop continues only when timer is reset by external entities
                //Loop also breaks when photo is skipped regardless of timer value left
                do {
                    //break loop if photo needs to be skipped timer will be reset automatically
                    if (skipPhoto) break;
                    resetTimer = false;
                    TextView textTimer = ((TextView) findViewById(R.id.tvTimeLeft));
                    ProgressBar myBar = (ProgressBar) findViewById(R.id.pbTimeLeft);
                    //Timer value retrieved from seek bar
                    timer = ((SeekBar) findViewById(R.id.sbWaitTime)).getProgress();

                    int maxTimer = timer;
                    while (timer >= 0) {
                        if (resetTimer) break;

                        //Keep updating ProgressBar pbTimeLeft and TextView tvTimeLeft
                        uiThread.post(() -> {
                            textTimer.setText((new Integer(timer)).toString());
                            myBar.setMax(maxTimer);
                            myBar.setProgress(maxTimer - timer);
                        });
                        //Thread sleeps for 1 sec as a counter
                        try {
                            Thread.sleep(1000);
                        }catch (Exception e){}
                        //Depreciating timer value
                        timer--;
                    }
                }while (resetTimer);
                //adding to photo counter for download next image
                photoCounter++;
            }
        }

        //Function to reset timer and option of skipping an image
        public void resetTimer(boolean skipPhoto){
            //interrupt the background thread just in case the thread is sleeping
            this.interrupt();
            this.resetTimer = true;
            this.skipPhoto = skipPhoto;
        }
    }

    //SkipButtion onClick function for skipping images
    public void onSkipClick(View view){
        imageDownload.resetTimer(true);
    }
}
