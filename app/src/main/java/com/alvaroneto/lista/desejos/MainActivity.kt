package com.alvaroneto.lista.desejos

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.SparseBooleanArray
import android.view.View
import android.widget.ArrayAdapter
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private var gson = Gson()
    lateinit var mGoogleSignClient: GoogleSignInClient;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        sharedPreferences = getSharedPreferences("lista_de_desejos", Context.MODE_PRIVATE)

        val listViewTasks = findViewById<android.widget.ListView>(R.id.listViewTasks);
        val createTask = findViewById<android.widget.EditText>(R.id.createTask);

        val itemList = getData();

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, itemList);

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build();
        mGoogleSignClient = GoogleSignIn.getClient(this, gso);

        val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance();

        listViewTasks.adapter = adapter;
        adapter.notifyDataSetChanged()

        findViewById<View>(R.id.add).setOnClickListener {

            if(createTask.text.toString().trim() == ""){
                Toast.makeText(this, "Por favor informe uma descrição", Toast.LENGTH_SHORT).show()
            }
            else
            {
                itemList.add(createTask.text.toString());
                listViewTasks.adapter = adapter;
                adapter.notifyDataSetChanged()

                saveData(itemList)
                createTask.text.clear()
            }
        }

        findViewById<View>(R.id.delete).setOnClickListener {
            val position: SparseBooleanArray = listViewTasks.checkedItemPositions

            if(position.size() == 0){
                Toast.makeText(this, "Marque ao menos um item para deletar", Toast.LENGTH_SHORT).show()
            }
            else
            {
                val count = listViewTasks.count;
                var item = count - 1;
                while(item >= 0){
                    if(position.get(item)){
                        adapter.remove(itemList.get(item))
                    }
                    item--;
                }

                saveData(itemList)
                position.clear();
                adapter.notifyDataSetChanged()
            }
        }

        findViewById<View>(R.id.clear).setOnClickListener{
            itemList.clear()
            saveData(itemList)
            adapter.notifyDataSetChanged()
        }

        findViewById<View>(R.id.logout).setOnClickListener{
            firebaseAuth.signOut();
            mGoogleSignClient.signOut();

            val activity = Intent(this, LoginScreen::class.java);
            startActivity(activity);
        }
    }

    private fun getData(): ArrayList<String> {
        val arrayJson = sharedPreferences.getString("lista", null);
        return if(arrayJson.isNullOrEmpty()){
            arrayListOf();
        }else{
            gson.fromJson(arrayJson, object: TypeToken<ArrayList<String>>(){}.type)
        }
    }

    private fun saveData(array: ArrayList<String>){
        val arrayJson = gson.toJson(array);
        val editor = sharedPreferences.edit();
        editor.putString("lista", arrayJson);
        editor.apply();
    }
}