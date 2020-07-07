package com.tvmaze.tvinfo.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.tvmaze.tvinfo.util.Utility
import com.squareup.picasso.Picasso
import com.tvmaze.tvinfo.R
import kotlinx.android.synthetic.main.activity_detail.*
import org.json.JSONObject

class DetailActivity : AppCompatActivity() {

    private var image: String? = null
    private var network: String = ""
    private var status: String = ""
    private var premiered: String = ""
    private var runtime: String = "-"
    private var time: String = "-"
    private var movieId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        if (intent.hasExtra("id") && intent.getStringExtra("id") != null) {
            movieId = intent.getStringExtra("id")
            getMovieDetails(movieId)
        } else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val retryClick = findViewById<Button>(R.id.btn_retry)
        retryClick?.setOnClickListener {
            getMovieDetails(movieId)
            detailErrorCard.visibility = View.GONE
        }


    }

    // Fetch movie's information from api
    fun getMovieDetails(movieId: String) {

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
        val urlLeft = "https://api.tvmaze.com/shows/"
        val urlRight = "?embed[]=episodes&embed[]=images&embed[]=seasons"
        val url: String = urlLeft + movieId + urlRight

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->

                val embed = response.getJSONObject("_embedded")
                val images = embed.getJSONArray("images")
                val episodes = embed.getJSONArray("episodes")
                val seasons = embed.getJSONArray("seasons")
                val summary = response.getString("summary")

                if (response.has("runtime") && !response.isNull("runtime")) {
                    runtime = response.getString("runtime")
                    time = ((runtime.toInt() * episodes.length()) / 60).toString()
                }

                if (response.has("premiered") && !response.isNull("premiered")) {
                    premiered = response.getString("premiered").substring(0, 4)
                }

                if (response.has("status") && !response.isNull("status")) {
                    status = " - " + response.getString("status")
                }

                if (response.has("network") && !response.isNull("network")) {
                    val company: JSONObject = response.getJSONObject("network")
                    network = " | " + company.getString("name")
                }

                val generalInfo = premiered + status + network
                movieInfo.text = generalInfo

                for (i in 0 until images.length()) {
                    val item = images.getJSONObject(i)
                    if (item.getString("type").equals("background")) {
                        val resolation = item.getJSONObject("resolutions")
                        val banner = resolation.getJSONObject("original")
                        image = banner.getString("url")
                    }
                }

                if (image != null) {
                    val util = Utility()
                    val imageAddress = util.fixImageAddress(image.toString())
                    Picasso.get().load(imageAddress).into(moviePoster)
                } else {
                    moviePoster.setImageResource(R.drawable.placeholder_no_photo)
                }

                movieTitle.text = response.getString("name")

                episodeNum.text = episodes.length().toString()
                seasonNum.text = seasons.length().toString()
                episodeTime.text = runtime
                totalTime.text = time

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    movieSummary.text = Html.fromHtml(summary, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    movieSummary.text = Html.fromHtml(summary)
                }

                detailProgressbar.visibility = View.GONE
                detailView.visibility = View.VISIBLE

            },
            Response.ErrorListener { error ->
                Log.d("JSON_ERROR", error.toString())
                detailErrorCard.visibility = View.VISIBLE
                detailProgressbar.visibility = View.GONE
            }
        )
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest)
        //initialize the progress dialog and show it
        detailProgressbar.visibility = View.VISIBLE
    }
}