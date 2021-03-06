/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.wordstack;

import android.content.ClipData;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private static final int WORD_LENGTH = 5;
    public static final int LIGHT_BLUE = Color.rgb(176, 200, 255);
    public static final int LIGHT_GREEN = Color.rgb(200, 255, 200);
    private ArrayList<String> words = new ArrayList<>();
    private Random random = new Random();
    private StackedLayout stackedLayout;
    private String word1, word2;
    private Stack <LetterTile> placeTiles = new Stack();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("words.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while((line = in.readLine()) != null) {
                String word = line.trim();
                if (word.length() == 5){
                    words.add(word);
                }
            }
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }
        LinearLayout verticalLayout = (LinearLayout) findViewById(R.id.vertical_layout);
        stackedLayout = new StackedLayout(this);
        verticalLayout.addView(stackedLayout, 3);

        View word1LinearLayout = findViewById(R.id.word1);
        word1LinearLayout.setOnDragListener(new DragListener());
        View word2LinearLayout = findViewById(R.id.word2);
        word2LinearLayout.setOnDragListener(new DragListener());

        if(savedInstanceState!=null){
            checkForDataAndRestoreGame(savedInstanceState);
        }
    }

//    private class TouchListener implements View.OnTouchListener {
//
//        @Override
//        public boolean onTouch(View v, MotionEvent event) {
//            if (event.getAction() == MotionEvent.ACTION_DOWN && !stackedLayout.empty()) {
//                LetterTile tile = (LetterTile) stackedLayout.peek();
//                placeTiles.push(tile);
//                tile.moveToViewGroup((ViewGroup) v);
//                tile.freeze();
//
//                if (stackedLayout.empty()) {
//                    TextView messageBox = (TextView) findViewById(R.id.message_box);
//                    messageBox.setText(word1 + " " + word2);
//                }
//                return true;
//            }
//            return false;
//        }
//    }


    private class DragListener implements View.OnDragListener {

        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(LIGHT_GREEN);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundColor(Color.WHITE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign Tile to the target Layout
                    LetterTile tile = (LetterTile) event.getLocalState();
                    tile.moveToViewGroup((ViewGroup) v);

                    placeTiles.push(tile);
                    tile.freeze();

                    if (stackedLayout.empty()) {
                        TextView messageBox = (TextView) findViewById(R.id.message_box);
                        messageBox.setText(word1 + " " + word2);
                    }

                    return true;
            }
            return false;
        }
    }

    public boolean onStartGame(View view) {
        LinearLayout word1LinearLayout = (LinearLayout) findViewById(R.id.word1);
        LinearLayout word2LinearLayout = (LinearLayout) findViewById(R.id.word2);
        word1LinearLayout.removeAllViews();
        word2LinearLayout.removeAllViews();
        stackedLayout.clear();

        TextView messageBox = (TextView) findViewById(R.id.message_box);
        messageBox.setText("Game started");
        int length = words.size();
        int int1 = random.nextInt(length);
        int int2 = random.nextInt(length);
        word1 = words.get(int1);
        word2 = words.get(int2);
        int counter1 = word1.length();
        int counter2 = word2.length();
        StringBuilder scrambled = new StringBuilder();

        // randomly pick which word to grab a letter from and increment
        // that counter until either word runs out and then pick all
        // the letters in the word that was not exhausted

        while(counter1 != 0 && counter2 != 0){
            int rand = random.nextInt(2);
            if (rand == 0){
                scrambled.append(word1.charAt(word1.length()-counter1));
                counter1 = counter1 - 1;
            }
            else{
                scrambled.append(word2.charAt(word2.length()-counter2));
                counter2 = counter2 - 1;
            }
        }
        if(counter1==0){
            scrambled.append(word2.substring(word2.length()-counter2));
        }
        else{
            scrambled.append(word1.substring(word1.length()-counter1));
        }

        messageBox.setText(scrambled.toString());

        // create new LetterTile objects representing each letter
        // of the string and push them (in reverse order!) onto stackedLayout
        for(int i = scrambled.length() - 1; i >= 0; i--){
            LetterTile tile = new LetterTile(this, scrambled.charAt(i));
            stackedLayout.push(tile);
        }

        return true;
    }

    public boolean onUndo(View view) {
        if(stackedLayout.getSize() < (WORD_LENGTH*2)){
            LetterTile popped = placeTiles.pop();
            popped.moveToViewGroup((ViewGroup) stackedLayout);

            return true;
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        ViewGroup word1LinearLayout = (ViewGroup) findViewById(R.id.word1);
        ViewGroup word2LinearLayout = (ViewGroup) findViewById(R.id.word2);

        char[] charWord1 = new char[word1LinearLayout.getChildCount()];
        for (int i = 0; i < word1LinearLayout.getChildCount(); i++){
            LetterTile letter = (LetterTile) word1LinearLayout.getChildAt(i);
            charWord1[i] = letter.getChar();
        }

        char[] charWord2 = new char[word2LinearLayout.getChildCount()];
        for (int i = 0; i < word2LinearLayout.getChildCount(); i++){
            LetterTile letter = (LetterTile) word2LinearLayout.getChildAt(i);
            charWord2[i] = letter.getChar();
        }

        //storing the letters the user already put in word1
        outState.putCharArray("player word1",charWord1);

        //storing the letters the user already put in word2
        outState.putCharArray("player word2",charWord2);

        char[] charTile = new char[stackedLayout.getStack().size()];
        int size = stackedLayout.getStack().size();
        for(int i = 0; i < size; i ++){
            LetterTile letter = (LetterTile) stackedLayout.getStack().pop();
            charTile[i] = letter.getChar();
            Log.i("letter",letter.toString());
        }

        //storing the letters that are left in stacked layout tiles
        outState.putCharArray("tile remaining",charTile);

        //storing the original word1 and word2
        outState.putString("word1", word1);
        outState.putString("word1", word2);


        super.onSaveInstanceState(outState);
    }

    public void checkForDataAndRestoreGame(Bundle savedInstanceState) {
        if(savedInstanceState.getCharArray("player word1") != null){
            ViewGroup word1LinearLayout = (ViewGroup) findViewById(R.id.word1);
            for(int i = 0; i < savedInstanceState.getCharArray("player word1").length; i ++) {
                LetterTile newTile = new LetterTile(this, savedInstanceState.getCharArray("player word1")[i]);
                word1LinearLayout.addView(newTile);
            }
        }
        if(savedInstanceState.getCharArray("player word2") != null){
            ViewGroup word2LinearLayout = (ViewGroup) findViewById(R.id.word2);
            for(int i = 0; i < savedInstanceState.getCharArray("player word2").length; i ++) {
                LetterTile newTile = new LetterTile(this, savedInstanceState.getCharArray("player word2")[i]);
                word2LinearLayout.addView(newTile);
            }
        }
        if(savedInstanceState.getString("word1") != null){
            this.word1=savedInstanceState.getString("word1");
        }
        if(savedInstanceState.getString("word2") != null){
            this.word2=savedInstanceState.getString("word2");
        }
        if(savedInstanceState.getCharArray("tile remaining") != null){
            for(int i = savedInstanceState.getCharArray("tile remaining").length - 1; i >= 0; i --) {
                LetterTile newTile = new LetterTile(this, savedInstanceState.getCharArray("tile remaining")[i]);
                stackedLayout.push(newTile);
            }
        }
    }
}
