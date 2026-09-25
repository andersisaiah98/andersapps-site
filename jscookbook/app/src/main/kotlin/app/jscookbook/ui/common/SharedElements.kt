package app.jscookbook.ui.common

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import app.jscookbook.core.designsystem.theme.JsSprings
import app.jscookbook.core.designsystem.theme.JsTheme

val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

/** Where a recipe tile was tapped, so the hero flies back to the right tile. */
object TileOrigin {
    const val HomeRecent = "home-recent"
    const val HomeFavorites = "home-favorites"
    const val Cookbook = "cookbook"
    const val Category = "category"
    const val Editor = "editor"
}

fun recipeImageKey(recipeId: String, origin: String) = "recipe-image:$recipeId:$origin"

@OptIn(ExperimentalSharedTransitionApi::class)
private val HeroBounds = BoundsTransform { _, _ ->
    spring(
        dampingRatio = JsSprings.Hero.dampingRatio,
        stiffness = JsSprings.Hero.stiffness,
        visibilityThreshold = Rect.VisibilityThreshold,
    )
}

/**
 * Marks a recipe image as the shared element between its tile and the detail hero. A no-op
 * outside a navigation transition or when motion is reduced.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.recipeImageSharedElement(recipeId: String, origin: String): Modifier {
    val shared = LocalSharedTransitionScope.current ?: return this
    val visibility = LocalNavAnimatedVisibilityScope.current ?: return this
    if (JsTheme.reducedMotion) return this
    return with(shared) {
        this@recipeImageSharedElement.sharedElement(
            sharedContentState = rememberSharedContentState(recipeImageKey(recipeId, origin)),
            animatedVisibilityScope = visibility,
            boundsTransform = HeroBounds,
        )
    }
}
