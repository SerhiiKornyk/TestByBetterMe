package app.bettermetesttask.datamovies.repository

import android.util.Log
import app.bettermetesttask.datamovies.database.entities.MovieEntity
import app.bettermetesttask.datamovies.repository.stores.MoviesLocalStore
import app.bettermetesttask.datamovies.repository.stores.MoviesMapper
import app.bettermetesttask.datamovies.repository.stores.MoviesRestStore
import app.bettermetesttask.domaincore.utils.Result
import app.bettermetesttask.domaincore.utils.connectivity.ConnectivityManager
import app.bettermetesttask.domainmovies.entries.Movie
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.times

@ExtendWith(MockitoExtension::class)
internal class MoviesRepositoryTest {

    private lateinit var mockLocalStore: MoviesLocalStore
    private lateinit var mockRemote: MoviesRestStore
    private lateinit var mockMapper: MoviesMapper
    private lateinit var mockConnectivityManager: ConnectivityManager
    private var testInstance: MoviesRepositoryImpl? = null

    companion object {
        private val moviesRemote = listOf(
            Movie(1, "Title 1", "Overview 1", "Poster 1"),
            Movie(2, "Title 2", "Overview 2", "Poster 2"),
            Movie(3, "Title 3", "Overview 3", "Poster 3")
        )

        private val moviesLocal = listOf(
            MovieEntity(1, "Title 1", "Overview 1", "Poster 1"),
            MovieEntity(2, "Title 2", "Overview 2", "Poster 2"),
            MovieEntity(3, "Title 3", "Overview 3", "Poster 3")
        )

        private val emptyListRemote = emptyList<Movie>()

        private val emptyListLocal = emptyList<MovieEntity>()
    }

    @BeforeEach
    fun setUp() {
        mockLocalStore = mock()
        mockRemote = mock()
        mockMapper = mock()
        mockConnectivityManager = mock()
        testInstance =
            MoviesRepositoryImpl(mockLocalStore, mockMapper, mockConnectivityManager, mockRemote)
    }

    @AfterEach
    fun tearDown() {
        testInstance = null
        Mockito.reset(mockLocalStore, mockMapper, mockConnectivityManager, mockRemote)
    }

    @Test
    fun `getMovies Internet, Server send not empty list EXPECTED movies`() = runTest {
        fakeNetworkIsAvailable()
        fakeServerSentNotEmptyList()
        fakeMapToLocalBehavior()
        val actual = testInstance?.getMovies()
        val expected = Result.Success(moviesRemote)

        verifyLocalStoreSaveMovies()
        Assertions.assertEquals(actual, expected)


    }

    @Test
    fun `getMovies Internet, Server send empty list, saved local data EXPECTED movies`() =
        runTest {
            fakeNetworkIsAvailable()
            fakeServerSentEmptyList()
            fakeLocalNotEmptyList()
            fakeMapFromLocalBehavior()

            val actual = testInstance?.getMovies()
            val expected = Result.Success(moviesRemote)

            verifyLocalStoreLoadMovies()


            Assertions.assertEquals(actual, expected)
        }

    @Test
    fun `getMovies No Internet, saved local data EXPECTED movies`() =
        runTest {
            fakeNetworkIsAvailable(false)
            fakeServerSentEmptyList()
            fakeLocalNotEmptyList()
            fakeMapFromLocalBehavior()

            val actual = testInstance?.getMovies()
            val expected = Result.Success(moviesRemote)

            verifyLocalStoreLoadMovies()


            Assertions.assertEquals(actual, expected)
        }

    @Test
    fun `getMovies No Internet,  no saved local data EXPECTED Exception `() =
        runTest {
            fakeNetworkIsAvailable(false)
            fakeServerSentEmptyList()
            fakeLocalEmptyList()
            fakeMapFromLocalBehavior()

            val actual = testInstance?.getMovies() as Result.Error
            val expected = Result.Error(IllegalStateException("No internet connection"))

            verifyLocalStoreLoadMovies()


            Assertions.assertEquals(actual.error::class.java, expected.error::class.java)
            Assertions.assertEquals(actual.error.message, expected.error.message)

        }


    @Test
    fun `getMovies  Internet, Server Send Empty List,  no saved local data EXPECTED Exception `() =
        runTest {
            fakeNetworkIsAvailable(true)
            fakeServerSentEmptyList()
            fakeLocalEmptyList()
            fakeMapFromLocalBehavior()

            val actual = testInstance?.getMovies() as Result.Error
            val expected = Result.Error(IllegalStateException("No movies found"))

            verifyLocalStoreLoadMovies()


            Assertions.assertEquals(actual.error::class.java, expected.error::class.java)
            Assertions.assertEquals(actual.error.message, expected.error.message)

        }

    @Test
    fun `addMovieToFav  EXPECTED Verify Pass`() =
        runTest {
            testInstance?.addMovieToFavorites(1)
            Mockito.verify(mockLocalStore, times(1)).likeMovie(1)
        }

    @Test
    fun `removeMovieFromFav  EXPECTED Verify Pass`() =
        runTest {
            testInstance?.removeMovieFromFavorites(1)
            Mockito.verify(mockLocalStore, times(1)).dislikeMovie(1)
        }


    @Test
    fun `observeMovies  EXPECTED Verify Pass`() =
        runTest {
            val actual = testInstance?.observeLikedMovieIds()
            val expected = mockLocalStore.observeLikedMoviesIds()

            Assertions.assertEquals(actual, expected)

        }


    private suspend fun fakeServerSentEmptyList() {
        Mockito.`when`(mockRemote.getMovies()).thenReturn(emptyListRemote)
    }

    private suspend fun fakeLocalNotEmptyList() {
        Mockito.`when`(mockLocalStore.getMovies()).thenReturn(moviesLocal)
    }

    private suspend fun fakeLocalEmptyList() {
        Mockito.`when`(mockLocalStore.getMovies()).thenReturn(emptyListLocal)
    }

    private suspend fun fakeServerSentNotEmptyList() {
        Mockito.`when`(mockRemote.getMovies()).thenReturn(moviesRemote)
    }

    private fun fakeMapToLocalBehavior() {
        Mockito.`when`(mockMapper.mapToLocal(any())).thenCallRealMethod()
    }

    private fun fakeMapFromLocalBehavior() {
        Mockito.`when`(mockMapper.mapFromLocal(any())).thenCallRealMethod()
    }


    private fun fakeNetworkIsAvailable(available: Boolean = true) {
        Mockito.`when`(mockConnectivityManager.isNetworkAvailable()).thenReturn(available)
    }

    private suspend fun verifyLocalStoreSaveMovies() {
        Mockito.verify(mockLocalStore).saveMovies(moviesLocal)
    }

    private suspend fun verifyLocalStoreLoadMovies() {
        Mockito.verify(mockLocalStore).getMovies()
    }


    // To mock dependencies - you can use the following syntax
    //    private val someClass: String = mock()

    // To test suspend function - you can use `runTest`
    //    @Test
    //    fun `test suspend function`() = runTest {
    //       Verify something
    //
    //    }

    // To verify a method is called - you can use the following syntax
    //    verify(someObj).someMethod()

}