package com.sddrozdov.simplechatkotlin.activity

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sddrozdov.simplechatkotlin.R
import com.sddrozdov.simplechatkotlin.accountHelper.GoogleSignInAccountHelper
import com.sddrozdov.simplechatkotlin.adapters.MessageAdapter
import com.sddrozdov.simplechatkotlin.databinding.ActivityMainBinding
import com.sddrozdov.simplechatkotlin.models.User
import com.squareup.picasso.Picasso
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Binding must not be null")

    private lateinit var auth: FirebaseAuth

    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowInsetUtil.applyWindowInsets(binding.root)

        setSupportActionBar(binding.materialToolbar)

        auth = Firebase.auth

        setUpActionBar()

        val database = Firebase.database
        val myRef = database.getReference("messages")

        binding.sendMessage.setOnClickListener {
            val user = User(
                name = auth.currentUser?.displayName ?: "Anonymous",
                message = binding.editTextText.text.toString()

            )
            val newMessageRef = myRef.push()
            newMessageRef.setValue(user)
            binding.editTextText.text.clear()
        }
        onChangeListener(myRef)
        init()
    }

    private fun init() = with(binding) {
        adapter = MessageAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        recyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.singOut) {
            GoogleSignInAccountHelper.AuthManager.signOut(this)
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        auth.signOut()
        finish()
        return super.onOptionsItemSelected(item)

    }

    private fun onChangeListener(dataBaseRef: DatabaseReference) {
        dataBaseRef.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = ArrayList<User>()
                    for (i in snapshot.children) {
                        val user = i.getValue(User::class.java)
                        if (user != null) list.add(user)
                    }
                    adapter.submitList(list)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            }
        )
    }

    private fun setUpActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        thread {
            val bitmap = Picasso.get().load(auth.currentUser?.photoUrl).get()
            val drawable = BitmapDrawable(resources, bitmap)
            runOnUiThread {
                supportActionBar?.apply {
                    setHomeAsUpIndicator(drawable)
                    title = auth.currentUser?.displayName
                }
            }
        }
    }
}

object WindowInsetUtil {
    fun applyWindowInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}