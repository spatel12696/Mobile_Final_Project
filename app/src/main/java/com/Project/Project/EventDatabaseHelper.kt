package com.Project.Project

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EventDatabaseHelper(context: Context) {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val eventsCollection = firestore.collection("events")
    private fun savedEventsCollection(uid: String) = firestore.collection("users")
        .document(uid)
        .collection("saved_events")

    private fun withUserId(onReady: (String) -> Unit, onError: (Exception) -> Unit) {
        val current = auth.currentUser
        if (current != null) {
            onReady(current.uid)
        } else {
            auth.signInAnonymously()
                .addOnSuccessListener { result ->
                    onReady(result.user?.uid ?: "local")
                }
                .addOnFailureListener { ex -> onError(ex) }
        }
    }

    private val defaultEvents = listOf(
        Event(
            id = 1,
            name = "Downtown Music Fest",
            location = "Oshawa Centre",
            date = "Nov 15, 2025",
            time = "6:00 PM",
            description = "Live bands and food trucks in downtown Oshawa!",
            latitude = 43.945,
            longitude = -78.895
        ),
        Event(
            id = 2,
            name = "Food Carnival",
            location = "Lakeview Park",
            date = "Nov 22, 2025",
            time = "12:00 PM",
            description = "Enjoy cuisines from all around the world!",
            latitude = 43.952,
            longitude = -78.901
        ),
        Event(
            id = 3,
            name = "Art Exhibit Spotlight",
            location = "Robert McLaughlin Gallery",
            date = "Nov 25, 2025",
            time = "3:00 PM",
            description = "Explore modern and abstract art installations.",
            latitude = 43.950,
            longitude = -78.910
        ),
        Event(
            id = 4,
            name = "Fun Fair",
            location = "Memorial Park",
            date = "Dec 1, 2025",
            time = "10:00 AM",
            description = "Exciting rides, games, and local food stalls!",
            latitude = 43.94,
            longitude = -78.88
        ),
        Event(
            id = 5,
            name = "Fun Fair",
            location = "North Oshawa Grounds",
            date = "Dec 8, 2025",
            time = "11:00 AM",
            description = "Family fun fair with live performances and snacks!",
            latitude = 43.96,
            longitude = -78.86
        ),
        Event(
            id = 6,
            name = "Music Fest",
            location = "Tribute Communities Centre",
            date = "Dec 15, 2025",
            time = "5:00 PM",
            description = "Rock night with local and international bands!",
            latitude = 43.897,
            longitude = -78.863
        ),
        Event(
            id = 7,
            name = "Food Carnival",
            location = "Harmony Creek Park",
            date = "Dec 20, 2025",
            time = "1:00 PM",
            description = "Delicious street food and dessert trucks all day!",
            latitude = 43.93,
            longitude = -78.88
        ),
        Event(
            id = 8,
            name = "Food Carnival",
            location = "Simcoe Street Plaza",
            date = "Dec 28, 2025",
            time = "2:00 PM",
            description = "Experience cultural foods and music shows!",
            latitude = 43.915,
            longitude = -78.87
        )
    )

    fun seedDefaultsIfEmpty(
        onResult: (List<Event>) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        eventsCollection.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    val batch = firestore.batch()
                    defaultEvents.forEach { event ->
                        batch.set(eventsCollection.document(), event.toMap())
                    }
                    batch.commit()
                        .addOnSuccessListener { fetchEvents(onResult, onError) }
                        .addOnFailureListener { ex ->
                            onResult(defaultEvents)
                            onError(ex)
                        }
                } else {
                    onResult(snapshot.toEvents())
                }
            }
            .addOnFailureListener { ex ->
                onResult(defaultEvents)
                onError(ex)
            }
    }

    fun fetchEvents(
        onResult: (List<Event>) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        eventsCollection.get()
            .addOnSuccessListener { snapshot ->
                val events = snapshot.toEvents()
                if (events.isEmpty()) {
                    onResult(defaultEvents)
                } else {
                    onResult(events)
                }
            }
            .addOnFailureListener { ex ->
                onResult(defaultEvents)
                onError(ex)
            }
    }

    fun saveEvent(
        event: Event,
        onComplete: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        withUserId(onReady = { uid ->
            savedEventsCollection(uid).document(event.name)
                .set(event.toMap())
                .addOnSuccessListener { onComplete() }
                .addOnFailureListener { onError(it) }
        }, onError = onError)
    }

    fun isEventSaved(
        name: String,
        onResult: (Boolean) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        withUserId(onReady = { uid ->
            savedEventsCollection(uid).document(name)
                .get()
                .addOnSuccessListener { onResult(it.exists()) }
                .addOnFailureListener { onError(it) }
        }, onError = onError)
    }

    fun getSavedEvents(
        onResult: (List<Event>) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        withUserId(onReady = { uid ->
            savedEventsCollection(uid).get()
                .addOnSuccessListener { snapshot ->
                    onResult(snapshot.toEvents())
                }
                .addOnFailureListener { onError(it) }
        }, onError = onError)
    }

    fun deleteSavedEventByName(
        name: String,
        onComplete: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        withUserId(onReady = { uid ->
            savedEventsCollection(uid).document(name)
                .delete()
                .addOnSuccessListener { onComplete() }
                .addOnFailureListener { onError(it) }
        }, onError = onError)
    }

    private fun Event.toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "name" to name,
        "location" to location,
        "date" to date,
        "time" to time,
        "description" to description,
        "latitude" to latitude,
        "longitude" to longitude
    )

    private fun com.google.firebase.firestore.QuerySnapshot.toEvents(): List<Event> =
        documents.mapNotNull { doc ->
            val name = doc.getString("name") ?: return@mapNotNull null
            val location = doc.getString("location") ?: ""
            val date = doc.getString("date") ?: ""
            val time = doc.getString("time") ?: ""
            val description = doc.getString("description") ?: ""
            val latitude = doc.getDouble("latitude") ?: 0.0
            val longitude = doc.getDouble("longitude") ?: 0.0
            val id = doc.getLong("id")?.toInt() ?: 0

            Event(
                id = id,
                name = name,
                location = location,
                date = date,
                time = time,
                description = description,
                latitude = latitude,
                longitude = longitude
            )
        }
}
