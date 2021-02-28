package com.example.dictionaryusingperfecthashing;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dictionaryusingperfecthashing.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class WordList{
    int size; //total size of json file or total words contained in it
    JSONArray words; //english and bangla words that are contained in json file

}
class Hashtable{


    int length;

    long[][]secondHashTable;
    int[] i;


    Word word;

    Hashtable(int length){
        this.length = length;
    }
    Hashtable(long secondHashTable[][]){
        this.secondHashTable = secondHashTable;
    }
    Hashtable(int[] i){
        this.i = i;
    }
    public Hashtable(Word word) {
        this.word = word;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public long[][] getSecondHashTable() {
        return secondHashTable;
    }

    public void setSecondHashTable(long[][] secondHashTable) {
        this.secondHashTable = secondHashTable;
    }

    public int[] getI() {
        return i;
    }

    public void setI(int[] i) {
        this.i = i;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }
}

class Word{
    String en;
    String bn;
    int primaryHash;

    Word(String en, String bn){
        this.en = en;
        this.bn = bn;
    }

    Word(int primaryHash) {
        this.primaryHash = primaryHash;
    }


    public String getEn() {
        return en;
    }

    public void setEn(String en) {
        this.en = en;
    }

    public String getBn() {
        return bn;
    }

    public void setBn(String bn) {
        this.bn = bn;
    }

    public int getPrimaryHash() {
        return primaryHash;
    }

    public void setPrimaryHash(int primaryHash) {
        this.primaryHash = primaryHash;
    }
}


public class MainActivity extends AppCompatActivity {
    //declaring the xml fields
    SearchView search;
    Button translate;
    EditText input;
    TextView noWord;
    boolean isWordValid = true;
    WordList dictionary = new WordList();
    int base = 256;
    static long prime = 999999999989L; //12 digit prime declaration
    long initiala = -1;
    long initialb= -1 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        translate =  findViewById(R.id.translate); //the button translate to bangla
        input = findViewById(R.id.input); //the word to be searched for taken from the user
        noWord = findViewById(R.id.noword);
        final String[] inputText = new String[1];// input text taken from the user

        try{
            InputStream is = getAssets().open("E2Bdatabase.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer,"UTF-8");
            dictionary.words = new JSONArray(json);         //Assigning the json to words of wordList class declared above.
            dictionary.size = dictionary.words.length();

            //create array of word object
            Word[] word = new Word[dictionary.size] ;
            long max = 0;
            String strr = "";
            for(int i=0;i<dictionary.size;i++){
                String en = dictionary.words.getJSONObject(i).getString("en");
                String bn = dictionary.words.getJSONObject(i).getString("bn");

                //create & initialize actual word objects using constructor
                word[i] = new Word(en,bn);  //assigning the values of en and bn to attributes en and bn of class word

            }
            String c = word[1036].getEn();



            generateHashTable(dictionary.size, word);



        }catch (IOException e){
            e.printStackTrace();
        }catch(JSONException e){
            e.printStackTrace();
        }



        final long[] key = new long[1];
        translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputText[0] =input.getText().toString().toLowerCase();  //getting input from the text field
                long stringKey = stringToNumber(inputText[0]);
                long firstKey = getFirstKey(inputText[0]);
                int primaryHash = getPrimaryHash(inputText[0],dictionary.size);
                String str = String.valueOf(primaryHash);
                Toast.makeText(getApplicationContext(),str,Toast.LENGTH_LONG).show();/*



                for(int i=0;i<dictionary.size;i++){
                    try {
                    String en = dictionary.words.getJSONObject(i).getString("en");
                    if(en.equals(inputText[0])){
                        long primary = getPrimaryHash(en,dictionary.size);
                        String strla = String.valueOf(primary);
                        Toast.makeText(getApplicationContext(),strla,Toast.LENGTH_LONG).show();
                    }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }*/

                //}
                //sjdkasjsjdlasjdlaksjdl

                //get_json(word);
                //dump();

            }
        });


    }



    //this function will convert a word to a number

    long stringToNumber(String string){
        long stringKey = 0;

        for(int i=0; i<string.length(); i++){

            stringKey = ((stringKey*base)%prime+string.charAt(i))%prime;  //like cat = 99*256^2+97*256^1+116*256^0

        }


        return stringKey;


    }

    long getFirstKey(String string){
        long random_a ;
        long random_b ;
        random_a = (long) (1+ Math.floor(Math.random()*(prime-1)));
        random_b = (long) Math.floor(Math.random()*prime);



        //if the hash table is being generated

        if(this.initiala == -1  || this.initialb == -1){
            this.initiala=random_a;
            this.initialb=random_b;
        }

        //if the hash table is already generated
        //now searching is being conducted

        else{
            random_a=this.initiala;
            random_b=this.initialb;
        }
        long stringKey = this.stringToNumber(string);
        BigInteger a = BigInteger.valueOf(random_a);
        BigInteger b =BigInteger.valueOf(random_b);
        BigInteger stringk = BigInteger.valueOf(stringKey);
        BigInteger ak = a.multiply(stringk);
        BigInteger akplusb = ak.add(b);
        BigInteger p =BigInteger.valueOf(prime);
        BigInteger Bigfirskey = akplusb.mod(p);  //ak+b
        long firstkey = Bigfirskey.longValue();


        return firstkey;
    }
    int getPrimaryHash(String string, int m){
        long firstkey = this.getFirstKey(string);
        BigInteger BigfirstKey = BigInteger.valueOf(firstkey);
        BigInteger dictionarysize = BigInteger.valueOf(m);
        BigInteger BigprimaryHash = BigfirstKey.mod(dictionarysize);
        int primaryHash = BigprimaryHash.intValue();


        //int primaryHash = (int)firstkey%m; //((ak+b)%p)%m where m is the size of the dictionary
        return primaryHash;
    }

    //this fuction will generate a second unique key in case of secondary hashing

    long getSecondKey(long random_a,long random_b,int  m, String string){
        long firstkey = this.getFirstKey(string);
        BigInteger BigfirstKey = BigInteger.valueOf(firstkey);
        //BigInteger slotsize = BigInteger.valueOf(m);
        BigInteger a = BigInteger.valueOf(random_a);
        BigInteger b = BigInteger.valueOf(random_b);

        BigInteger ak1 = a.multiply(BigfirstKey);
        BigInteger ak1plusb = ak1.add(b);
        BigInteger p =BigInteger.valueOf(prime);
        BigInteger Bigsecondkey = ak1plusb.mod(p);  //ak'+b
        long secondkey = Bigsecondkey.longValue();


        return secondkey;

    }

    //this function will generate a secondary hash key

    int getSecondaryHash(long a,long  b,int  m,String string){

        long secondkey = this.getSecondKey(a,b,m,string);
        BigInteger BigSecondKey = BigInteger.valueOf(secondkey);
        BigInteger slotsize = BigInteger.valueOf(m);
        BigInteger bigSecondaryHash = BigSecondKey.mod(slotsize);
        int secondaryhash = bigSecondaryHash.intValue();

        return secondaryhash;  //((ak'+b)%p)%m where m is the square of the total number of words collided in the same slot

    }

    void generateHashTable(int dictionarySizey, Word[] wordy) {
        int dictionarySize = dictionarySizey;
        Word[] word = wordy;
        ArrayList<Word> stringy=new ArrayList<Word>();
        int[] count;
        int[] count2;
        count = new int[dictionarySize];
        count2 = new int[dictionarySize];
        Arrays.fill(count, 0);
        Arrays.fill(count2, 0);
        Hashtable[] hashtable = new Hashtable[dictionarySize];


        int maximumCollisions=0;




        int save=0;
        List<Integer>[] array = new ArrayList[dictionarySize];

        for(int i=0;i<dictionarySize;i++){


            word[i].en = word[i].en.toLowerCase();
            String string = word[i].getEn();
            int primaryHash = getPrimaryHash(string,dictionarySize);
//            array[primaryHash].add(i);



            count[primaryHash]++;   //if count is greater than 1 for each primary hash index , then a primary hash index have been repeated twice and collision has been occured.
            if(count[primaryHash]>maximumCollisions){
                maximumCollisions = count[primaryHash];
                save = primaryHash;
            }
            word[i] = new Word(primaryHash);










        }
        //Toast.makeText(getApplicationContext(),String.valueOf(maximumCollisions),Toast.LENGTH_LONG).show();\




        int max = 0;
        String[][] myTwoDimensionalStringArray;
        myTwoDimensionalStringArray = new String[dictionarySize][];
        int[] hash_arr;



        for(int j=0;j<dictionarySize;j++){


            int primaryHash = word[j].primaryHash;
            myTwoDimensionalStringArray[primaryHash] = new String[count[primaryHash]];
            myTwoDimensionalStringArray[primaryHash][count2[primaryHash]] = word[j].en;
            count2[primaryHash]++;

            if(count[j]>=1){

                int length = count[j]*count[j]; //this length is the size of each slot in the hash table
                hashtable[j] = new Hashtable(length);
                if(hashtable[j].length>max)max = j;




                int[] secondaryKeysSize;
                secondaryKeysSize = new int[length];
                Arrays.fill(secondaryKeysSize,0);


                //secondaryKeysSize = (array[primaryHash]).stream().mapToInt(i -> i).toArray();
                hashtable[j] = new Hashtable(secondaryKeysSize);

            }

        }
        /*ArrayList string = new ArrayList<>();
        for(int i=0;i<count[save];i++){
            string.add(myTwoDimensionalStringArray[save][i]);
        }*/

        //Toast.makeText(getApplicationContext(),String.valueOf(string),Toast.LENGTH_LONG).show();


    }




     /*public void get_json(String word){
         long count = 0;
        String json;
        try{
            InputStream is = getAssets().open("E2Bdatabase.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();



            json = new String(buffer,"UTF-8");
            JSONArray jsonArray = new JSONArray(json);



            long words = 0;
            for(int i = 0; i < jsonArray.length(); i++){

                JSONObject wordList = jsonArray.getJSONObject(i);

                if(wordList.getString("en").equals(wordList)){
                    numberofwordslist.add(wordList.getString("bn"));

                }
                if(wordList.getString("en")!= null){
                    count++;
                }

            }
                long length = jsonArray.length();
                String len = String.valueOf(count);

                Toast.makeText(getApplicationContext(),len,Toast.LENGTH_LONG).show();


                numberofwordslist.clear();




        }catch (IOException e){
            e.printStackTrace();
        }catch(JSONException e){
            e.printStackTrace();
        }


     }

     public void dump(){
         long[][] hello = new long[103655][100];
         hello[10][25] = 999999999989L;
         String jello = String.valueOf(hello[10][25]);
         Toast.makeText(getApplicationContext(),jello,Toast.LENGTH_LONG).show();
     }
*/

}
