package app.bettermetesttask.movies.sections

import android.os.Parcelable
import app.bettermetesttask.domainmovies.entries.Movie
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
sealed class MoviesState : Parcelable {

    object Initial : MoviesState()

    object Loading : MoviesState()

    data class Loaded(val movies: @RawValue List<Movie>) : MoviesState()

    data class Error(val message: String) : MoviesState()
}

@Parcelize
sealed class MovieDetailsState : Parcelable {
    object Closed : MovieDetailsState()
    data class Open(val movie: @RawValue Movie) : MovieDetailsState()
}