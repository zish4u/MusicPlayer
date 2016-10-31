package test.musicplayer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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
    private SeekBar sb;
    Thread updateSeekbar;
    private ImageButton play,fastForward,fastBackward,playNext,playPrev;
    TextView seekStartPos,seekEndPos,songName;
    String[] name;

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
