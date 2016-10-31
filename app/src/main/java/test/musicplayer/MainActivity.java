package test.musicplayer;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView playList;
    private String[] items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playList=(ListView)findViewById(R.id.playList);
        //Finding Songs List.
        final ArrayList<File> mySongs=findSongs(Environment.getExternalStorageDirectory());
        items=new String[mySongs.size()];
        for (int i=0; i<mySongs.size(); i++){
            items[i]=mySongs.get(i).getName().toString().replace(".mp3","").replace(".wav","");
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(getApplicationContext(),R.layout.song_layout,R.id.textView,items);
        playList.setAdapter(adapter);
        playList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(getApplicationContext(),Player.class).putExtra("pos",position).putExtra("songlist",mySongs));
            }
        });
    }

    //Method for finding song list.
    public ArrayList<File> findSongs(File root){
        ArrayList arrayList=new ArrayList();
        File[] file=root.listFiles();
        for(File singleFile: file){
            if(singleFile.isDirectory()&& !singleFile.isHidden()){
                arrayList.addAll(findSongs(singleFile));
            }
            else if(singleFile.getName().endsWith(".mp3")||singleFile.getName().endsWith(".wav")){
                arrayList.add(singleFile);
            }
        }
        return arrayList;
    }
}
