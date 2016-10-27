package test.musicplayer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import java.util.ArrayList;

public class Player extends AppCompatActivity implements View.OnClickListener {

    MediaPlayer mediaPlayer;
    ArrayList mySongs;
    int position;
    Uri u;
    SeekBar sb;
    Thread updateSeekbar;
    Button play,fastForward,fastBackward,playNext,playPrev;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        play=(Button)findViewById(R.id.play_btn);
        fastForward=(Button)findViewById(R.id.ffrwd_btn);
        fastBackward=(Button)findViewById(R.id.fback_btn);
        playNext=(Button)findViewById(R.id.nxt_btn);
        playPrev=(Button)findViewById(R.id.prev_btn);
        sb=(SeekBar)findViewById(R.id.seekBar);

        updateSeekbar=new Thread(){
            @Override
            public void run() {
                int totalDuration=mediaPlayer.getDuration();
                int currentPosition=0;
                while(currentPosition<totalDuration){
                    try {
                        sleep(5000);
                        currentPosition=mediaPlayer.getCurrentPosition();
                        sb.setProgress(currentPosition);
                    }
                    catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
                //
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
        mediaPlayer.start();
        sb.setMax(mediaPlayer.getDuration());
        updateSeekbar.start();
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

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

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.play_btn:
                if (mediaPlayer.isPlaying()) {
                    play.setText("|>");
                    mediaPlayer.pause();
                } else {
                    play.setText("||");
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
                mediaPlayer.start();
                sb.setMax(mediaPlayer.getDuration());
                break;
            case R.id.prev_btn:
                mediaPlayer.stop();
                mediaPlayer.release();
                position = (position - 1 < 0) ? mySongs.size() - 1 : position - 1;
                u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                mediaPlayer.start();
                sb.setMax(mediaPlayer.getDuration());
                break;

        }
    }
}
