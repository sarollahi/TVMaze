package com.tvmaze.tvinfo.data

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.tvmaze.tvinfo.ui.DetailActivity
import com.tvmaze.tvinfo.R
import com.tvmaze.tvinfo.model.Movie
import com.tvmaze.tvinfo.util.Utility
import com.squareup.picasso.Picasso

class MovieRecyclerViewAdapter(private val list: List<Movie>) :
    RecyclerView.Adapter<MovieViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MovieViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie: Movie = list[position]
        holder.bind(movie)
    }

    override fun getItemCount(): Int = list.size

}

class MovieViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item, parent, false)) {
    private var mTitleView: TextView? = null
    private var mImageView: ImageView? = null
    private var imageAddress: String? = null
    private var movieId: String? = null
    private var mcardView: CardView? = null


    init {
        mTitleView = itemView.findViewById(R.id.movieTitle)
        mImageView = itemView.findViewById(R.id.movieImage)
        mcardView = itemView.findViewById(R.id.cardview)
    }

    fun bind(movie: Movie) {
        mTitleView!!.text = movie.title
        if (movie.posterPath.isNullOrEmpty()) {
            mImageView!!.setImageResource(R.drawable.placeholder_no_photo)
        } else {
            val util = Utility()
            imageAddress = util.fixImageAddress(movie.posterPath)
            Picasso.get().load(imageAddress).placeholder(R.drawable.placeholder_no_photo)
                .into(mImageView)
        }
        mcardView?.setOnClickListener {
            movieId = movie.id
            val activity = itemView.context as Activity
            val intent = Intent(activity, DetailActivity::class.java)
            intent.putExtra("id", movieId)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            activity.startActivity(intent)
        }
    }
}