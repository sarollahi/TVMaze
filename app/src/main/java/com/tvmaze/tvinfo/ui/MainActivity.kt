package com.tvmaze.tvinfo.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.tvmaze.tvinfo.R
import com.tvmaze.tvinfo.data.MovieRecyclerViewAdapter
import com.tvmaze.tvinfo.model.Movie
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    private var movieList: MutableList<Movie> = ArrayList()
    private var image: String? = null
    private var tvShowName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        refreshGridView()
        movieRecyclerView.apply {
            adapter = MovieRecyclerViewAdapter(movieList)
        }

        val searchClick = findViewById<Button>(R.id.btn_search)
        searchClick?.setOnClickListener {
            tvShowName = searchQuery.text.toString()
            if (tvShowName != "") {
                movieList = getMovies(tvShowName)
                checkKeyboardVisibility()
            }
        }


        searchQuery.setOnKeyListener { v, keyCode, event ->
            getMoviesOnEnter(keyCode, event)
        }

        val retryClick = findViewById<Button>(R.id.btn_retry)
        retryClick?.setOnClickListener {
            movieList = getMovies(tvShowName)
            errorCard.visibility = View.GONE
        }
    }

    // Get movies when user release Enter key
    fun getMoviesOnEnter(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
            tvShowName = searchQuery.text.toString()
            if (tvShowName != "") {
                movieList = getMovies(tvShowName)
                checkKeyboardVisibility()
            }
            return true
        }
        return false
    }

    // Get movies list
    fun getMovies(movieName: String): MutableList<Movie> {

        movieList.clear()
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
        val url: String = "https://api.tvmaze.com/search/shows?q=" + movieName

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                if (response.length() > 1) {
                    for (i in 0 until response.length()) {
                        val item = response.getJSONObject(i)
                        val item_details = item.getJSONObject("show")

                        if (item_details.has("image") && !item_details.isNull("image")) {
                            val images = item_details.getJSONObject("image")
                            image = images.getString("medium")
                        } else {
                            image = ""
                        }
                        val movie =
                            Movie(
                                item_details.getString("id"),
                                item_details.getString("name"),
                                image
                            )

                        movieList.add(movie)
                        showResult()
                    }
                } else {
                    movieRecyclerView.visibility = View.GONE
                    logoCard.visibility = View.VISIBLE
                    val view: View = window.decorView.rootView
                    Snackbar.make(view, "Nothing Found!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                }

                //Check for chnaging adapter.
                movieRecyclerView.adapter!!.notifyDataSetChanged()
                progressbar.visibility = View.GONE

            },
            Response.ErrorListener { error ->
                Log.d("JSON_ERROR", error.toString())
                showError()
            }
        )
        // Add the request to the RequestQueue.
        queue.add(jsonArrayRequest)
        hideLogo()
        return movieList
    }

    //Make logo card hidden
    private fun hideLogo() {
        logoCard.visibility = View.GONE
        progressbar.visibility = View.VISIBLE
    }

    //Make error card visible
    private fun showError() {
        errorCard.visibility = View.VISIBLE
        progressbar.visibility = View.GONE
    }

    //Make recyclerview visible
    private fun showResult() {
        movieRecyclerView.visibility = View.VISIBLE
        logoCard.visibility = View.GONE
    }

    //Manage recyclerView layout
    private fun refreshGridView() {
        val columns = calculateNoOfColumns(this, 85F)
        movieRecyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, columns)
        }
    }

    // Calculate number of columns based on screen size
    fun calculateNoOfColumns(context: Context, columnWidthDp: Float): Int {
        val displayMetrics = context.resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        return (screenWidthDp / columnWidthDp + 0.5).toInt()
    }

    // Check keyboard visibility
    private fun checkKeyboardVisibility() {
        if (searchCard.isVisible) {
            hideKeyboard()
            searchCard.visibility = View.GONE
        } else {
            searchCard.visibility = View.VISIBLE
            showKeyboard(this, searchQuery)
        }
    }

    // Open virtual keyboard
    fun showKeyboard(context: Context, view: View) {
        view.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    // Hide virtual keyboard
    fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
    }

    //Refresh grid view with changing orientation
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        refreshGridView()
    }

    override fun onResume() {
        super.onResume()
        refreshGridView()
    }

    // Inflate the menu; this adds items to the action bar if it is present
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * Handle action bar item clicks here. The action bar will
     * automatically handle clicks on the Home/Up button, so long
     * as you specify a parent activity in AndroidManifest.xml
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                checkKeyboardVisibility()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

