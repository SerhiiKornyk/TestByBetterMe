package app.bettermetesttask.datamovies.repository

import app.bettermetesttask.datamovies.repository.stores.MoviesLocalStore
import app.bettermetesttask.datamovies.repository.stores.MoviesMapper
import app.bettermetesttask.datamovies.repository.stores.MoviesRestStore
import app.bettermetesttask.domaincore.utils.Result
import app.bettermetesttask.domaincore.utils.connectivity.ConnectivityManager
import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.domainmovies.repository.MoviesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class MoviesRepositoryImpl @Inject constructor(
    private val localStore: MoviesLocalStore,
    private val mapper: MoviesMapper,
    private val connectivityManager: ConnectivityManager,
    private val restStore: MoviesRestStore = MoviesRestStore()
) : MoviesRepository {

    override suspend fun getMovies(): Result<List<Movie>> {
        if (!connectivityManager.isNetworkAvailable()) {
            return handleNoInternetState()
        }

        return if (restStore.getMovies().isEmpty()) {
            handleRemoteEmptyState(false)
        } else {
            return Result.of {
                val movies = restStore.getMovies()
                val localMovies = movies.map { mapper.mapToLocal(it) }

                localStore.saveMovies(
                    localMovies
                )
                movies
            }

        }
    }


    override fun observeLikedMovieIds(): Flow<List<Int>> {
        return localStore.observeLikedMoviesIds()
    }

    override suspend fun addMovieToFavorites(movieId: Int) {
        localStore.likeMovie(movieId)
    }

    override suspend fun removeMovieFromFavorites(movieId: Int) {
        localStore.dislikeMovie(movieId)
    }

    private suspend fun handleRemoteEmptyState(noConnection: Boolean): Result<List<Movie>> {
        return Result.of {
            val local = localStore.getMovies()

            if (local.isEmpty()) {
                throw IllegalStateException(if (noConnection) "No internet connection" else "No movies found")
            }
            local.map {
                mapper.mapFromLocal(it)
            }
        }
    }

    private suspend fun handleNoInternetState(): Result<List<Movie>> {
        return Result.of {
            val local = localStore.getMovies()

            if (local.isEmpty()) {
                throw IllegalStateException("No internet connection")
            }
            local.map {
                mapper.mapFromLocal(it)
            }
        }
    }

    private fun handleErrorState(message: String): Result<List<Movie>> {
        return Result.Error(IllegalStateException(message))
    }
}