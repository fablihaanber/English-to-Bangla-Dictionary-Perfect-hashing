package com.example.dictionaryusingperfecthashing;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

class WordList{
    int size; //total size of json file or total words contained in it
    JSONArray words; //english and bangla words that are contained in json file

}
class Hashtable{

    long a;
    long b;
    int length;

    Hashtable(int length){
        this.length = length;
    }

    public long getA() {
        return a;
    }

    public void setA(long a) {
        this.a = a;
    }

    public long getB() {
        return b;
    }

    public void setB(long b) {
        this.b = b;
    }
}


class Word{
    String en;
    String bn;
    int primaryHash;
    int secondaryHash;

    Word(String en, String bn){
        this.en = en;
        this.bn = bn;
    }


    public String getEn() {
        return en;
    }

    public int getPrimaryHash() {
        return primaryHash;
    }

    public void setPrimaryHash(int primaryHash) {
        this.primaryHash = primaryHash;
    }

    public int getSecondaryHash() {
        return secondaryHash;
    }

    public void setSecondaryHash(int secondaryHash) {
        this.secondaryHash = secondaryHash;
    }
}


public class MainActivity extends AppCompatActivity {
    //declaring the xml fields
    Button translate;
    EditText input;
    TextView MeaningWord, foundWord;

    WordList dictionary = new WordList();
    int base = 256;
    static long prime = 999999999989L; //12 digit prime declaration
    long initiala = -1;
    long initialb= -1 ;
    int[] count;
    int[][] hash_arr;
    Word[] word;
    Hashtable[] hashtable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        translate =  findViewById(R.id.translate); //the button translate to bangla
        input = findViewById(R.id.input); //the word to be searched for taken from the user
        MeaningWord = findViewById(R.id.noword);
        foundWord = findViewById(R.id.Foundword);

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
            word = new Word[dictionary.size] ;
            long max = 0;
            String strr = "";
            for(int i=0;i<dictionary.size;i++){
                String en = dictionary.words.getJSONObject(i).getString("en");
                String bn = dictionary.words.getJSONObject(i).getString("bn");

                //create & initialize actual word objects using constructor
                word[i] = new Word(en,bn);  //assigning the values of en and bn to attributes en and bn of class word

            }

            generateHashTable(dictionary.size);

        }catch (IOException e){
            e.printStackTrace();
        }catch(JSONException e){
            e.printStackTrace();
        }

        translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputText[0] =input.getText().toString().toLowerCase();  //getting input from the text field
                String theWord = search(inputText[0],dictionary.size);
                if(theWord.equals("")){
                    foundWord.setText("No word found");

                }
                else{
                    foundWord.setText(theWord);
                    foundWord.setVisibility(View.VISIBLE);
                    MeaningWord.setVisibility(View.VISIBLE);
                }


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
        BigInteger Bigfirskey = akplusb.mod(p);  //(ak+b)%p
        long firstkey = Bigfirskey.longValue();


        return firstkey;
    }
    int getPrimaryHash(String string, int m){
        long firstkey = this.getFirstKey(string);
        BigInteger BigfirstKey = BigInteger.valueOf(firstkey);
        BigInteger dictionarysize = BigInteger.valueOf(m);
        BigInteger BigprimaryHash = BigfirstKey.mod(dictionarysize);  //((ak+b)%p)%m
        int primaryHash = BigprimaryHash.intValue();

        return primaryHash;
    }

    //this fuction will generate a second unique key in case of secondary hashing

    long getSecondKey(long random_a,long random_b,int  m, String string){

        long firstkey = this.getFirstKey(string);
        BigInteger BigfirstKey = BigInteger.valueOf(firstkey);
        BigInteger a = BigInteger.valueOf(random_a);
        BigInteger b = BigInteger.valueOf(random_b);
        BigInteger ak1 = a.multiply(BigfirstKey);   //ak'
        BigInteger ak1plusb = ak1.add(b);           //ak'+b
        BigInteger p =BigInteger.valueOf(prime);
        BigInteger Bigsecondkey = ak1plusb.mod(p);  //(ak'+b)%p
        long secondkey = Bigsecondkey.longValue();

        return secondkey;

    }

    //this function will generate a secondary hash key

    int getSecondaryHash(long a,long  b,int  m,String string){

        long secondkey = this.getSecondKey(a,b,m,string);
        BigInteger BigSecondKey = BigInteger.valueOf(secondkey);  //(ak'+b)%p
        BigInteger slotsize = BigInteger.valueOf(m);
        BigInteger bigSecondaryHash = BigSecondKey.mod(slotsize); //((ak'+b)%p)%m where m is the slot size of the hash table index
        int secondaryhash = bigSecondaryHash.intValue();

        return secondaryhash;

    }



    void generateHashTable(int dictionarySizey) {

        int dictionarySize = dictionarySizey;
        int maximumCollisions=0;
        //Count  will measure how many collisions are there where the keys have the same primary hash
        count = new int[dictionarySize];

        for(int i=0;i<dictionarySize;i++){
            count[i] = 0;  // Initializing the count array
        }

        hashtable = new Hashtable[dictionarySize];
        int[][] arrayCollidedWords = new int[dictionarySize][500]; //this array will store the indices of collided keys for that particular hash function and the space 500 is specified randomly to avoid null reference

        //looping through the indices of saved keys of dictionary
        for(int i=0;i<dictionarySize;i++){

            word[i].en = word[i].en.toLowerCase();
            String string = word[i].getEn();
            int primaryHash = getPrimaryHash(string,dictionarySize);
            int j_slot = count[primaryHash];
            arrayCollidedWords[primaryHash][j_slot] = i;
            count[primaryHash]++;   //if count is greater than 1 for each primary hash index , then a primary hash index have been repeated twice and collision has been occured.

            if(count[primaryHash]>maximumCollisions){
                maximumCollisions = count[primaryHash];
            }

            word[i].setPrimaryHash(primaryHash);

        }

        int size = maximumCollisions*maximumCollisions;
        hash_arr = new int[dictionarySize][size+100];

        //looping through the hash indices of the hash table
        for(int j=0;j<dictionarySize;j++){


            long a,b;
            int m;
            int length = count[j]*count[j]; //this length is the size of each slot in the hash table
            hashtable[j] = new Hashtable(length);
            m=length;

            if(count[j]>=1){

                    if (count[j] == 1) {    //if there is only one element in the hash slot
                        a = 0;
                        b = 0;
                        m = 1;
                        int index = arrayCollidedWords[j][0];  //Here,j is the index of the word that did not collide
                        int secondaryHash = getSecondaryHash(a,b,m,word[index].en);
                        word[index].setSecondaryHash(secondaryHash);
                        hashtable[j].setA(a);
                        hashtable[j].setB(b);
                    }

                else{
                    int[] secondaryHashSlotTable = new int[m];
                    for(int i=0;i<count[j];i++){

                        int index = arrayCollidedWords[j][i];
                        a = (long) (1+ Math.floor(Math.random()*(prime-1)));
                        b = (long) Math.floor(Math.random()*prime);
                        int secondaryHash = getSecondaryHash(a,b,m,word[index].en);


                        if(secondaryHashSlotTable[secondaryHash]==0){
                            word[index].setSecondaryHash(secondaryHash);

                        }
                        else{
                            for(int slot=0;slot<m;slot++){
                                secondaryHashSlotTable[slot] =0;
                            }
                            i = 0;
                            continue;
                        }
                        word[index].setSecondaryHash(secondaryHash);
                        hashtable[j].setA(a);
                        hashtable[j].setB(b);

                    }


                }
            }

        }

        for(int i=0;i<dictionarySize;i++){
            int pHash = word[i].getPrimaryHash();
            int sHash = word[i].getSecondaryHash();
            hash_arr[pHash][sHash] = i;

        }

    }

    String  search(String searchWord, int dictionarySize){
        String theWord = "";
        int inputPHash = getPrimaryHash(searchWord,dictionarySize);
        if(count[inputPHash]==0){
            String str = "Word not found";
            //Toast.makeText(getApplicationContext(),str,Toast.LENGTH_LONG).show();
            theWord = str;
        }
        if(count[inputPHash]==1){
            int key = hash_arr[inputPHash][0];
            String string = word[key].en;
            if(string.equals(searchWord)){
                theWord = word[key].bn;
            }

        }
        if(count[inputPHash]>1){

            long a = hashtable[inputPHash].getA();
            long b =hashtable[inputPHash].getB();
            int m = hashtable[inputPHash].length;
            int inputSHash = getSecondaryHash(a,b,m,searchWord);
            int key = hash_arr[inputPHash][inputSHash];
            String string = word[key].en;
            if(string.equals(searchWord)){
                theWord = word[key].bn;
            }
        }
        return theWord;


    }


}
