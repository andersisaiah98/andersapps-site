package app.jscookbook.core.data.sync

/**
 * Conflict rule for a row that arrives from the server. Both phones stamp updated_at when they
 * change a row, and the newest edit wins:
 *
 * - No local row: take the remote one.
 * - Local row changed and not yet pushed (dirty): keep it unless the remote edit is strictly
 *   newer. A kept local edit is pushed next and wins on the server too (its trigger ignores
 *   older writes), so both phones converge on it.
 * - Local row already synced: take the remote one unless it's older (an echo of something this
 *   phone has since superseded). An equal timestamp is the same edit, and re-applying is harmless.
 */
fun shouldApplyRemote(localUpdatedAt: Long?, localDirty: Boolean, remoteUpdatedAt: Long): Boolean = when {
    localUpdatedAt == null -> true
    localDirty -> remoteUpdatedAt > localUpdatedAt
    else -> remoteUpdatedAt >= localUpdatedAt
}
