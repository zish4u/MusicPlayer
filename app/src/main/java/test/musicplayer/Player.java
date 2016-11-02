package test.musicplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Script;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.lang.String;

public class Player extends AppCompatActivity implements View.OnClickListener {

    private static MediaPlayer mediaPlayer;
    ArrayList mySongs;
    private int position;
    Uri u;
    RelativeLayout myLayout;
    private SeekBar sb;
    Thread updateSeekbar;
    private ImageButton play,fastForward,fastBackward,playNext,playPrev;
    TextView seekStartPos,seekEndPos,songName;
    String[] name;
    ImageView albumArt;
    byte[] art;
    Bitmap songArt;
    MediaMetadataRetriever mediaMetadataRetriever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        play=(ImageButton)findViewById(R.id.play_btn);
        fastForward=(ImageButton)findViewById(R.id.ffrwd_btn);
        fastBackward=(ImageButton)findViewById(R.id.fback_btn);
        playNext=(ImageButton)findViewById(R.id.nxt_btn);
        playPrev=(ImageButton)findViewById(R.id.prev_btn);
        sb=(SeekBar)findViewById(R.id.seekBar);
        seekStartPos=(TextView)findViewById(R.id.startSeekPos);
        seekEndPos=(TextView)findViewById(R.id.endSeekPos);
        songName=(TextView)findViewById(R.id.songTitle);
        albumArt=(ImageView)findViewById(R.id.songPic);
        myLayout=(RelativeLayout)findViewById(R.id.layoutBackground);


        updateSeekbar=new Thread(){
            @Override
            public void run() {
                int totalDuration=mediaPlayer.getDuration();
                int currentPosition=0;
                while(currentPosition<totalDuration){
                    try {
                        sleep(1000);
                        currentPosition=mediaPlayer.getCurrentPosition();
                        sb.setProgress(currentPosition);

                    }
                    catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }

            }
        };

        play.setOnClickListener(this);
        fastForward.setOnClickListener(this);
        fastBackward.setOnClickListener(this);
        playNext.setOnClickListener(this);
        playPrev.setOnClickListener(this);

        if(mediaPlayer!=null){

            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent i=getIntent();
        Bundle b=i.getExtras();
        mySongs=(ArrayList)b.getParcelableArrayList("songlist");
        position=b.getInt("pos",0);
        u=Uri.parse(mySongs.get(position).toString());
        mediaPlayer=MediaPlayer.create(getApplicationContext(),u);
        updateSeekbar.start();
        seekEndPos.setText(" "+songEndTime());
        sb.setMax(mediaPlayer.getDuration());
        songImage();
        songName.setText(" "+songTitle(mySongs.get(position).toString()));
        mediaPlayer.start();
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

                long seconds =(TimeUnit.MILLISECONDS.toSeconds(progress));
                long minutes = TimeUnit.SECONDS.toMinutes(seconds);
                seekStartPos.setText(minutes+":"+String.format("%02d",seconds%60));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

    }

    public String songEndTime(){
        long endTime=  mediaPlayer.getDuration();
        long seconds=TimeUnit.MILLISECONDS.toSeconds(endTime);
        long minutes=TimeUnit.SECONDS.toMinutes(seconds);
        String songEnd=minutes+":"+String.format("%02d",seconds%60);
        return songEnd;
    }

    public String songTitle(String name){
        String[] arr=name.split("/");
        return arr[arr.length-1];
    }

    public void songImage(){
       mediaMetadataRetriever=new MediaMetadataRetriever();
       mediaMetadataRetriever.setDataSource(mySongs.get(position).toString());
       try{
           art=mediaMetadataRetriever.getEmbeddedPicture();
           songArt=BitmapFactory.decodeByteArray(art, 0, art.length);
           albumArt.setImageBitmap(songArt);
           //setting Song Art Image as a layout background.
           Bitmap back=blurRenderScript(this,songArt,25);
           BitmapDrawable layout_background=new BitmapDrawable(back);
           myLayout.setBackgroundDrawable(layout_background);

       }
       catch(Exception e){
            albumArt.setImageResource(R.drawable.music);
           Drawable myDrawable = getResources().getDrawable(R.drawable.music);
           Bitmap anImage      = ((BitmapDrawable) myDrawable).getBitmap();
           myLayout.setBackgroundDrawable(new BitmapDrawable(blurRenderScript(this,anImage,25)));
        }
    }

    @SuppressLint("newApi")

    public static Bitmap blurRenderScript(Context context, Bitmap image, int radius){
        try {
            image=RGB565toARGB888(image);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        Bitmap bitmap=Bitmap.createBitmap(image.getWidth(),image.getHeight(),Bitmap.Config.ARGB_8888);
        RenderScript renderScript=RenderScript.create(context);
        Allocation blurInput=Allocation.createFromBitmap(renderScript,image);
        Allocation blurOutput=Allocation.createFromBitmap(renderScript,bitmap);
        ScriptIntrinsicBlur blur=ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));//Working fine in above 17 api level.
        blur.setInput(blurInput);  //Working only fine above 17 api level.
        blur.setRadius(radius);    //Working only fine above 17 api level.
        blur.forEach(blurOutput);  //Working only fine above 17 api level.
        blurOutput.copyTo(bitmap); //Working only fine above 17 api level.
        renderScript.destroy();
        return bitmap;
    }

    private static Bitmap RGB565toARGB888(Bitmap img) throws Exception{
        int numPixels=img.getWidth()*img.getHeight();
        int[] pixels=new int[numPixels];

        //Get JPEG pixels. Each int is color value for one pixel.
        img.getPixels(pixels,0,img.getWidth(),0,0,img.getWidth(),img.getHeight());
        //Create a bitmap of appropriate format.
        Bitmap result=Bitmap.createBitmap(img.getWidth(),img.getHeight(),Bitmap.Config.ARGB_8888);
        //set RGB pixels.
        result.setPixels(pixels,0,result.getWidth(),0,0,result.getWidth(),result.getHeight());
        return  result;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.play_btn:
                if (mediaPlayer.isPlaying()) {
                    //play.setOnClickListener();
                    play.setImageResource(R.drawable.playbutton_selector);
                    mediaPlayer.pause();
                } else {
                    play.setImageResource(R.drawable.playbutton_selector);
                    mediaPlayer.start();
                }
                break;
            case R.id.ffrwd_btn:
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 5000);
                break;
            case R.id.fback_btn:
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000);
                break;
            case R.id.nxt_btn:
                mediaPlayer.stop();
                mediaPlayer.release();
                position = (position + 1) % mySongs.size();
                u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                songImage();
                songName.setText(" "+songTitle(mySongs.get(position).toString()));
                seekEndPos.setText(" "+songEndTime());
                mediaPlayer.start();
                sb.setMax(mediaPlayer.getDuration());
                break;
            case R.id.prev_btn:
                mediaPlayer.stop();
                mediaPlayer.release();
                position = (position - 1 < 0) ? mySongs.size() - 1 : position - 1;
                u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                songImage();
                songName.setText(" "+songTitle(mySongs.get(position).toString()));
                seekEndPos.setText(" " +songEndTime());
                mediaPlayer.start();
                sb.setMax(mediaPlayer.getDuration());
                break;

        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mediaPlayer.stop();
    }
}
