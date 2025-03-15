package app.bettermetesttask.movies.sections

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bettermetesttask.domaincore.utils.Result
import app.bettermetesttask.domaincore.utils.connectivity.ConnectivityManager
import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.domainmovies.interactors.AddMovieToFavoritesUseCase
import app.bettermetesttask.domainmovies.interactors.ObserveMoviesUseCase
import app.bettermetesttask.domainmovies.interactors.RemoveMovieFromFavoritesUseCase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class MoviesViewModel @Inject constructor(
    private val observeMoviesUseCase: ObserveMoviesUseCase,
    private val likeMovieUseCase: AddMovieToFavoritesUseCase,
    private val dislikeMovieUseCase: RemoveMovieFromFavoritesUseCase,
) : ViewModel() {

    private val moviesMutableFlow: MutableState<MoviesState> = mutableStateOf(MoviesState.Initial)

    val moviesStateFlow: State<MoviesState>
        get() = moviesMutableFlow

    private val _openMovieDetails: MutableState<MovieDetailsState> =
        mutableStateOf(MovieDetailsState.Closed)
    val openMovieDetails: State<MovieDetailsState>
        get() = _openMovieDetails


    fun loadMovies() {
        moviesMutableFlow.value = MoviesState.Loading

        viewModelScope.launch {
            observeMoviesUseCase()
                .collectLatest { result ->
                    if (result is Result.Success) {
                        moviesMutableFlow.value = (MoviesState.Loaded(result.data))
                        return@collectLatest
                    }

                    if (result is Result.Error) {
                        moviesMutableFlow.value =
                            MoviesState.Error(result.error.message ?: "Failed to get movies")
                    }

                }
        }
    }

    fun likeMovie(movie: Movie) {
        viewModelScope.launch {
            if (!movie.liked) {
                likeMovieUseCase(movie.id)
            } else {
                dislikeMovieUseCase(movie.id)
            }
        }
    }

    fun openMovieDetails(movie: Movie) {
        _openMovieDetails.value = MovieDetailsState.Open(movie)
    }

    fun closeMovieDetails() {
        _openMovieDetails.value = MovieDetailsState.Closed
    }
}
