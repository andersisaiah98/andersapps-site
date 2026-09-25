package app.jscookbook.ui.photos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import app.jscookbook.core.data.repository.RecipeRepository
import app.jscookbook.core.designsystem.component.JsIconButton
import app.jscookbook.core.designsystem.icon.JsIcons
import app.jscookbook.core.designsystem.theme.JsTheme
import app.jscookbook.core.model.Photo
import app.jscookbook.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.saket.telephoto.flick.FlickToDismiss
import me.saket.telephoto.flick.FlickToDismissState
import me.saket.telephoto.flick.rememberFlickToDismissState
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PhotoViewerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    recipes: RecipeRepository,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<Routes.PhotoViewer>()
    val startIndex = route.index

    val photos: StateFlow<List<Photo>?> = recipes.observeRecipe(route.recipeId)
        .map { it?.photos.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@Composable
fun PhotoViewerRoute(onClose: () -> Unit, viewModel: PhotoViewerViewModel = hiltViewModel()) {
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val list = photos ?: return
    if (list.isEmpty()) {
        LaunchedEffect(Unit) { onClose() }
        return
    }
    PhotoViewer(photos = list, startIndex = viewModel.startIndex.coerceIn(0, list.lastIndex), onClose = onClose)
}

/**
 * Full-screen photos: pinch to zoom with rubber-band edges, double-tap to zoom to the tap point,
 * fling with momentum, swipe sideways between photos, and flick down (or up) to dismiss.
 */
@Composable
fun PhotoViewer(photos: List<Photo>, startIndex: Int, onClose: () -> Unit) {
    val pagerState = rememberPagerState(initialPage = startIndex) { photos.size }
    val close by rememberUpdatedState(onClose)
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("screen:photos"),
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), beyondViewportPageCount = 1) { page ->
            val photo = photos[page]
            val flickState = rememberFlickToDismissState(dismissThresholdRatio = 0.12f, rotateOnDrag = true)
            FlickToDismiss(state = flickState) {
                ZoomableAsyncImage(
                    model = (photo.localPath ?: photo.thumbnailPath)?.let(::File),
                    contentDescription = photo.caption.ifBlank { "Photo ${page + 1} of ${photos.size}" },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            val gesture = flickState.gestureState
            if (gesture is FlickToDismissState.GestureState.Dismissing) {
                LaunchedEffect(Unit) {
                    delay(gesture.animationDuration / 2)
                    close()
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            JsIconButton(JsIcons.Close, contentDescription = "Close photos", onClick = onClose, tint = Color.White)
            if (photos.size > 1) {
                Text(
                    "${pagerState.currentPage + 1} / ${photos.size}",
                    style = JsTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
        val caption = photos[pagerState.currentPage].caption
        if (caption.isNotBlank()) {
            Text(
                caption,
                style = JsTheme.typography.bodyLarge,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .navigationBarsPadding()
                    .padding(20.dp),
            )
        }
    }
}
