package app.bettermetesttask.movies.sections.compose

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.featurecommon.injection.utils.Injectable
import app.bettermetesttask.featurecommon.injection.viewmodel.SimpleViewModelProviderFactory
import app.bettermetesttask.movies.sections.MovieDetailsState
import app.bettermetesttask.movies.sections.MoviesState
import app.bettermetesttask.movies.sections.MoviesViewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

class MoviesComposeFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelProvider: Provider<MoviesViewModel>

    private val viewModel by viewModels<MoviesViewModel> {
        SimpleViewModelProviderFactory(
            viewModelProvider
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {

                val lifecycle = LocalLifecycleOwner.current.lifecycle

                val viewState by rememberSaveable { viewModel.moviesStateFlow }
                val openMovieDetails by rememberSaveable { viewModel.openMovieDetails }

                val likeMovie = remember {
                    return@remember { movie: Movie ->
                        viewModel.likeMovie(movie)
                    }
                }

                val openMovie = remember {
                    return@remember { movie: Movie ->
                        viewModel.openMovieDetails(movie)
                    }
                }

                val load = remember {
                    return@remember {
                        viewModel.loadMovies()
                    }
                }

                val closeMovie = remember {
                    return@remember {
                        viewModel.closeMovieDetails()
                    }
                }

                DisposableEffect(lifecycle) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> {
                                if (viewState is MoviesState.Initial) {
                                    load()
                                }
                            }

                            else -> {}
                        }
                    }
                    lifecycle.addObserver(observer)
                    onDispose {
                        lifecycle.removeObserver(observer)
                    }
                }

                MoviesComposeScreen(viewState, likeMovie = likeMovie, reload = load, openMovie)

                if (openMovieDetails is MovieDetailsState.Open && viewState is MoviesState.Loaded) {
                    val selectedMovie by remember(viewState) {
                        derivedStateOf {
                            with(viewState as MoviesState.Loaded)
                            {
                                movies.firstOrNull { it.id == (openMovieDetails as MovieDetailsState.Open).movie.id }
                                    ?: (openMovieDetails as MovieDetailsState.Open).movie
                            }
                        }
                    }
                    DetailsSheet(
                        selectedMovie,
                        likeMovie,
                        closeMovie
                    )
                }
            }
        }
    }
}

@Composable
private fun MoviesComposeScreen(
    moviesState: MoviesState,
    likeMovie: (Movie) -> Unit,
    reload: () -> Unit = {},
    openDetails: (Movie) -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        when (moviesState) {
            MoviesState.Initial -> {}
            is MoviesState.Loaded -> {
                LazyColumn {
                    items(moviesState.movies, { it.id }) { item ->
                        MovieItem(item, onItemClicked = openDetails, onLikeClicked = {
                            likeMovie(item)
                        })
                    }
                }
            }

            MoviesState.Loading -> {
                Loading()
            }

            is MoviesState.Error -> {
                Error(moviesState.message, reload)
            }
        }
    }
}

@Composable
@Preview
private fun Error(error: String = "Error", reload: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = "Error",
            modifier = Modifier.size(48.dp)
        )
        Text(text = error, color = MaterialTheme.colorScheme.error)
        Button(
            reload,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background
            ),
        ) {
            Text(text = "Reload")
        }
    }
}

@Composable
private fun Loading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun MovieItem(movie: Movie, onItemClicked: (movie: Movie) -> Unit, onLikeClicked: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = { onItemClicked(movie) }),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MoviePoster(movie, false)

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = movie.title, fontSize = 18.sp, color = Color.Black)
                Text(text = movie.description, fontSize = 14.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(onClick = { onLikeClicked(movie.id) }) {
                Icon(
                    imageVector = if (movie.liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like Button",
                    tint = if (movie.liked) Color.Red else Color.Gray
                )
            }
        }
    }
}

@Composable
private fun MoviePoster(movie: Movie, detailsSize: Boolean) {
    AsyncImage(
        model = movie.posterPath,
        contentDescription = "Movie Poster",
        error = painterResource(id = R.drawable.ic_menu_report_image),
        modifier = Modifier
            .size(if (detailsSize) 200.dp else 60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Gray)
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
private fun PreviewsMoviesComposeScreen() {
    MoviesComposeScreen(MoviesState.Loaded(
        List(20) { index ->
            Movie(
                index,
                "Title $index",
                "Overview $index",
                "",
                liked = index % 2 == 0,
            )
        }
    ), likeMovie = {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsSheet(movie: Movie, likeMovie: (Movie) -> Unit, close: () -> Unit) {
    val state = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val onDismiss = remember {
        return@remember {
            scope.launch {
                close()
                state.hide()

            }
            Unit
        }
    }
    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MoviePoster(movie, true)
            Spacer(modifier = Modifier.size(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = movie.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                IconButton(onClick = { likeMovie(movie) }) {
                    Icon(
                        imageVector = if (movie.liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like Button",
                        tint = if (movie.liked) Color.Red else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.size(16.dp))

            Text(
                text = movie.description,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }


    }
}