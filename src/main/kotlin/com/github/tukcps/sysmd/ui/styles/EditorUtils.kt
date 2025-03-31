@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED")

package com.github.tukcps.sysmd.ui.styles

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier


@Composable
fun VerticalScrollbar(
    modifier: Modifier,
    scrollState: LazyListState
) = androidx.compose.foundation.VerticalScrollbar(
    rememberScrollbarAdapter(scrollState),
    modifier
)


