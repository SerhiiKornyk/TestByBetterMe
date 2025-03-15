package app.bettermetesttask.datamovies.repository.stores

import app.bettermetesttask.datamovies.database.entities.MovieEntity
import app.bettermetesttask.domainmovies.entries.Movie
import javax.inject.Inject

class MoviesMapper @Inject constructor() {


    fun mapToLocal(movie: Movie): MovieEntity {
        return with(movie) {
            MovieEntity(id, title, description, posterPath)
        }
    }

    fun mapFromLocal(movieEntity: MovieEntity): Movie {
        return with(movieEntity) {
            Movie(id, title, description, posterPath  ?: "")
        }
    }
}
