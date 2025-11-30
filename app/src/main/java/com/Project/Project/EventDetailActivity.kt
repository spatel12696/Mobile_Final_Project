package com.Project.Project

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.MediaController
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

class EventDetailActivity : AppCompatActivity() {

    private lateinit var dbHelper: EventDatabaseHelper
    private lateinit var event: Event
    private var mediaPlayer: MediaPlayer? = null
    private var isAudioPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

        dbHelper = EventDatabaseHelper(this)

        val nameText = findViewById<TextView>(R.id.eventName)
        val locationText = findViewById<TextView>(R.id.eventLocation)
        val dateText = findViewById<TextView>(R.id.eventDate)
        val descText = findViewById<TextView>(R.id.eventDescription)
        val saveButton = findViewById<Button>(R.id.saveEventButton)
        val backButton = findViewById<Button>(R.id.backButton)
        val mapButton = findViewById<Button>(R.id.viewOnMapButton)
        val videoView = findViewById<VideoView>(R.id.videoView)

        event = Event(
            name = intent.getStringExtra("eventName") ?: "",
            location = intent.getStringExtra("eventLocation") ?: "",
            date = intent.getStringExtra("eventDate") ?: "",
            time = intent.getStringExtra("eventTime") ?: "",
            description = intent.getStringExtra("eventDescription") ?: "",
            latitude = intent.getDoubleExtra("eventLatitude", 0.0),
            longitude = intent.getDoubleExtra("eventLongitude", 0.0)
        )

        nameText.text = event.name
        locationText.text = event.location
        dateText.text = "${event.date}  • ${event.time}"
        descText.text = event.description

        when (event.name.lowercase().replace(" ", "")) {
            "downtownmusicfest" -> playVideo(videoView, R.raw.musicfest)
            "foodcarnival" -> playVideo(videoView, R.raw.foodcarnival)
            "artexhibitspotlight" -> playVideo(videoView, R.raw.artexhibit)
            "funfair" -> playVideo(videoView, R.raw.funfair)
            "musicfest" -> {
                playAudio(R.raw.musicfestmp3)
                videoView.visibility = View.GONE
            }
            else -> videoView.visibility = View.GONE
        }

        var alreadySaved = false
        dbHelper.isEventSaved(event.name, onResult = { saved ->
            alreadySaved = saved
            saveButton.text = if (saved) "Remove from Saved Events" else "Add to Saved Events"
        }, onError = {
            saveButton.text = "Add to Saved Events"
        })

        saveButton.setOnClickListener {
            if (alreadySaved) {
                dbHelper.deleteSavedEventByName(event.name,
                    onComplete = {
                        alreadySaved = false
                        saveButton.text = "Add to Saved Events"
                        Toast.makeText(this, "Removed from Saved Events", Toast.LENGTH_SHORT).show()
                    },
                    onError = {
                        Snackbar.make(saveButton, "Couldn't remove right now.", Snackbar.LENGTH_SHORT).show()
                    }
                )
            } else {
                dbHelper.saveEvent(
                    event,
                    onComplete = {
                        alreadySaved = true
                        saveButton.text = "Remove from Saved Events"
                        Toast.makeText(this, "Added to Saved Events", Toast.LENGTH_SHORT).show()
                    },
                    onError = { ex ->
                        val message = ex.message ?: "Couldn't save right now."
                        Snackbar.make(saveButton, message, Snackbar.LENGTH_SHORT).show()
                    }
                )
            }
        }

        mapButton.setOnClickListener {
            if (event.latitude != 0.0 && event.longitude != 0.0) {
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra("latitude", event.latitude)
                intent.putExtra("longitude", event.longitude)
                intent.putExtra("eventName", event.name)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Location not available for this event.", Toast.LENGTH_SHORT).show()
            }
        }

        backButton.setOnClickListener {
            stopMedia()
            finish()
        }
    }

    private fun playVideo(videoView: VideoView, resId: Int) {
        videoView.visibility = View.VISIBLE
        val uri = Uri.parse("android.resource://$packageName/$resId")
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)
        videoView.setVideoURI(uri)
        videoView.setOnPreparedListener { it.isLooping = true }
        videoView.start()
    }

    private fun playAudio(resId: Int) {
        stopMedia()
        mediaPlayer = MediaPlayer.create(this, resId)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
        isAudioPlaying = true
    }

    private fun stopMedia() {
        if (isAudioPlaying) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            isAudioPlaying = false
        }
    }

    override fun onPause() {
        super.onPause()
        stopMedia()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMedia()
    }
}
