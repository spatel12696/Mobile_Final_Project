package com.Project.Project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SavedEventsActivity : AppCompatActivity() {

    private lateinit var dbHelper: EventDatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventAdapter
    private lateinit var emptyText: TextView
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_events)

        dbHelper = EventDatabaseHelper(this)
        recyclerView = findViewById(R.id.savedEventsRecyclerView)
        emptyText = findViewById(R.id.emptyText)
        backButton = findViewById(R.id.backButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = EventAdapter(mutableListOf()) { selectedEvent ->
            val intent = Intent(this, EventDetailActivity::class.java)
            intent.putExtra("eventName", selectedEvent.name)
            intent.putExtra("eventLocation", selectedEvent.location)
            intent.putExtra("eventDate", selectedEvent.date)
            intent.putExtra("eventDescription", selectedEvent.description)
            intent.putExtra("eventTime", selectedEvent.time)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        loadSavedEvents()

        backButton.setOnClickListener { finish() }
    }

    private fun loadSavedEvents() {
        dbHelper.getSavedEvents(
            onResult = { savedEvents ->
                if (savedEvents.isEmpty()) {
                    emptyText.text = "No events added."
                    emptyText.visibility = TextView.VISIBLE
                    recyclerView.visibility = RecyclerView.GONE
                } else {
                    emptyText.visibility = TextView.GONE
                    recyclerView.visibility = RecyclerView.VISIBLE
                    adapter.updateData(savedEvents)
                }
            },
            onError = {
                emptyText.text = "Unable to load saved events."
                emptyText.visibility = TextView.VISIBLE
                recyclerView.visibility = RecyclerView.GONE
            }
        )
    }

    override fun onResume() {
        super.onResume()
        loadSavedEvents()
    }
}
