package com.Project.Project

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.abs

class HomeActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbHelper: EventDatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventAdapter
    private var allEvents: List<Event> = emptyList()

    private lateinit var sensorManager: SensorManager
    private var lastUpdate: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        dbHelper = EventDatabaseHelper(this)

        recyclerView = findViewById(R.id.eventsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = EventAdapter(mutableListOf()) { selectedEvent ->
            val intent = Intent(this, EventDetailActivity::class.java).apply {
                putExtra("eventName", selectedEvent.name)
                putExtra("eventLocation", selectedEvent.location)
                putExtra("eventDate", selectedEvent.date)
                putExtra("eventTime", selectedEvent.time)
                putExtra("eventDescription", selectedEvent.description)
                putExtra("eventLatitude", selectedEvent.latitude)
                putExtra("eventLongitude", selectedEvent.longitude)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        val mapBtn = findViewById<ImageButton>(R.id.mapButton)
        val savedBtn = findViewById<ImageButton>(R.id.savedEventsButton)
        val logoutBtn = findViewById<ImageButton>(R.id.logoutButton)
        val searchBtn = findViewById<ImageButton>(R.id.searchButton)
        val searchHint = findViewById<View>(R.id.searchHintContainer)

        mapBtn.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("showAllEvents", true)
            startActivity(intent)
        }

        savedBtn.setOnClickListener {
            startActivity(Intent(this, SavedEventsActivity::class.java))
        }

        logoutBtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        searchBtn.setOnClickListener { toggleSearchView() }
        searchHint.setOnClickListener { toggleSearchView() }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI
        )

        loadEvents()

        Toast.makeText(this, "Shake your device to undo last saved event!", Toast.LENGTH_SHORT).show()
    }

    private fun loadEvents() {
        dbHelper.seedDefaultsIfEmpty(
            onResult = { events ->
                allEvents = events
                adapter.updateData(events)
            },
            onError = {
                Toast.makeText(this, "Unable to load events right now.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun toggleSearchView() {
        val searchView = SearchView(this)
        searchView.queryHint = "Search events..."
        searchView.isIconified = false
        searchView.isFocusable = true
        searchView.requestFocusFromTouch()

        val dialog = AlertDialog.Builder(this)
            .setTitle("Search Events")
            .setView(searchView)
            .setNegativeButton("Close") { _, _ ->
                filterEvents(null)
            }
            .create()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterEvents(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterEvents(newText)
                return true
            }
        })

        dialog.show()
    }

    private fun filterEvents(query: String?) {
        val term = query?.trim().orEmpty()
        val filteredList = if (term.isEmpty()) {
            allEvents
        } else {
            val lower = term.lowercase()
            allEvents.filter { event ->
                listOf(
                    event.name,
                    event.location,
                    event.date,
                    event.time,
                    event.description
                ).any { it.contains(lower, ignoreCase = true) }
            }
        }
        adapter.updateData(filteredList)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val currentTime = System.currentTimeMillis()

            if ((currentTime - lastUpdate) > 150) {
                val diffTime = currentTime - lastUpdate
                lastUpdate = currentTime
                val speed = abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000
                if (speed > 700) showUndoDialog()
                lastX = x
                lastY = y
                lastZ = z
            }
        }
    }

    private fun showUndoDialog() {
        dbHelper.getSavedEvents(
            onResult = { savedEvents ->
                if (savedEvents.isNotEmpty()) {
                    val lastEvent = savedEvents.last()
                    AlertDialog.Builder(this)
                        .setTitle("Undo Last Saved Event?")
                        .setMessage("Do you want to remove '${lastEvent.name}' from saved events?")
                        .setPositiveButton("Yes") { _, _ ->
                            dbHelper.deleteSavedEventByName(lastEvent.name)
                            Toast.makeText(this, "Removed last saved event.", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("No", null)
                        .show()
                } else {
                    Toast.makeText(this, "No saved events to undo.", Toast.LENGTH_SHORT).show()
                }
            },
            onError = {
                Toast.makeText(this, "Couldn't fetch saved events.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onPause() { super.onPause(); sensorManager.unregisterListener(this) }
    override fun onResume() { super.onResume(); sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI) }
}
