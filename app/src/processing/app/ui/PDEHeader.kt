package processing.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import processing.app.ui.theme.LocalLocale

data class SearchState(val query: String, val onQueryChange: (String) -> Unit)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun Header(
    searchable: SearchState? = null,
    headlineKey: String,
    headline: @Composable () -> Unit = {
        val locale = LocalLocale.current
        Text(locale[headlineKey])
    },
    descriptionKey: String,
    description: @Composable () -> Unit = {
        val locale = LocalLocale.current
        Text(locale[descriptionKey])
    },
    searchKey: String = "search",
    searchPlaceholder: @Composable () -> Unit = {
        val locale = LocalLocale.current
        Text(locale[searchKey])
    },
    extraContent: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 36.dp, top = 48.dp, end = 24.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium)) {
                headline()
            }
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodySmall) {
                description()
            }
        }
        extraContent()
        Spacer(modifier = Modifier.width(96.dp))
        searchable?.apply {
            SearchBar(
                modifier = Modifier
                    .widthIn(max = 250.dp),
                inputField = {
                    SearchBarDefaults.InputField(
                        query = query,
                        onQueryChange = onQueryChange,
                        onSearch = {

                        },
                        trailingIcon = {
                            if (query.isEmpty()) {
                                Icon(Icons.Default.Search, contentDescription = null)
                            } else {
                                IconButton(
                                    onClick = { onQueryChange("") }
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                }
                            }
                        },
                        expanded = false,
                        onExpandedChange = { },
                        placeholder = { searchPlaceholder() }
                    )
                },
                expanded = false,
                onExpandedChange = {},
                content = {}
            )
        }
    }
}