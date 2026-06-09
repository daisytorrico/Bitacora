package com.catedra.bitacora.features.discovery.presentation.explorer

import androidx.compose.animation.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.catedra.bitacora.R
import com.catedra.bitacora.core.ui.components.common.AppTopBar
import com.catedra.bitacora.core.ui.components.common.BitacoraChip
import com.catedra.bitacora.core.ui.components.travel.TravelItem
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerScreen(
    viewModel: ExplorerViewModel,
    onTravelClick: (String) -> Unit,
    onSeeAllClick: () -> Unit,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var isSearchVisible by remember { mutableStateOf(false) }
    var showMonthDropdown by remember { mutableStateOf(false) }
    var menuYear by remember { mutableIntStateOf(java.time.LocalDate.now().year) }

    LaunchedEffect(showMonthDropdown) {
        if (showMonthDropdown) {
            menuYear = uiState.selectedYear ?: java.time.LocalDate.now().year
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadDiscoveryData(isSilent = true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItemIndex >= totalItemsCount - 2
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            when {
                uiState.isFilterModeActive -> viewModel.loadMoreFiltered()
                !uiState.isSearchModeActive -> viewModel.loadMoreFollowing()
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = stringResource(R.string.explore),
                actions = {
                    IconButton(onClick = {
                        isSearchVisible = !isSearchVisible
                        if (!isSearchVisible) viewModel.clearAll()
                    }) {
                        Icon(
                            imageVector = if (isSearchVisible) Icons.Default.Close else Icons.Default.Tune,
                            contentDescription = "Filtros y Búsqueda",
                            tint = if (uiState.isFilterModeActive || uiState.searchQuery.isNotBlank()) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedVisibility(
                visible = isSearchVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    SearchBar(
                        inputField = {
                            SearchBarDefaults.InputField(
                                query = uiState.searchQuery,
                                onQueryChange = { viewModel.onSearchQueryChange(it) },
                                onSearch = { viewModel.performSearch() },
                                expanded = false,
                                onExpandedChange = { },
                                placeholder = { Text(stringResource(R.string.search_travels)) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (uiState.searchQuery.isNotBlank()) {
                                        IconButton(onClick = { viewModel.clearSearch() }) {
                                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                                        }
                                    }
                                }
                            )
                        },
                        expanded = false,
                        onExpandedChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        windowInsets = WindowInsets(0, 0, 0, 0)
                    ) { }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            DurationFilter.SHORT to stringResource(R.string.short_duration_filter),
                            DurationFilter.MEDIUM to stringResource(R.string.medium_duration_filter),
                            DurationFilter.LONG to stringResource(R.string.long_duration_filter)
                        ).forEach { (filter, label) ->
                            val selected = uiState.selectedDuration == filter
                            BitacoraChip(
                                selected = selected,
                                onClick = { viewModel.onDurationFilterChange(filter) },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                selectedContainerAlpha = 0.2f,
                                selectedBorderAlpha = 0.7f,
                                selectedBorderWidth = 1.5.dp
                            )
                        }

                        BitacoraChip(
                            selected = uiState.isDetailedOnly,
                            onClick = { viewModel.onDetailedFilterChange() },
                            label = {
                                Text(
                                    text = stringResource(R.string.plus_five_points),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            selectedContainerAlpha = 0.2f,
                            selectedBorderAlpha = 0.7f,
                            selectedBorderWidth = 1.5.dp
                        )

                        Box {
                            val monthSelected = uiState.selectedMonth != null
                            BitacoraChip(
                                selected = monthSelected,
                                onClick = { showMonthDropdown = true },
                                label = {
                                    val text = if (uiState.selectedMonth != null) {
                                        val mName = Month.of(uiState.selectedMonth!!)
                                            .getDisplayName(TextStyle.FULL, Locale("es"))
                                            .replaceFirstChar { it.uppercase() }
                                        "$mName ${uiState.selectedYear ?: ""}"
                                    } else stringResource(R.string.month)
                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                selectedContainerAlpha = 0.2f,
                                selectedBorderAlpha = 0.7f,
                                selectedBorderWidth = 1.5.dp,
                                trailingIcon = {
                                    if (uiState.selectedMonth != null) {
                                        IconButton(
                                            onClick = { viewModel.onMonthFilterChange(null, null) },
                                            modifier = Modifier.size(18.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            )
                            DropdownMenu(
                                expanded = showMonthDropdown,
                                onDismissRequest = { showMonthDropdown = false },
                                modifier = Modifier.width(200.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { menuYear-- }) {
                                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = stringResource(
                                            R.string.previous_year
                                        ))
                                    }
                                    Text(
                                        text = menuYear.toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(onClick = { menuYear++ }) {
                                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = stringResource(
                                            R.string.next_year
                                        ))
                                    }
                                }
                                HorizontalDivider(thickness = 0.5.dp)
                                
                                Box(modifier = Modifier.heightIn(max = 300.dp)) {
                                    Column {
                                        (1..12).forEach { month ->
                                            DropdownMenuItem(
                                                text = {
                                                    val mName = Month.of(month).getDisplayName(TextStyle.FULL, Locale("es"))
                                                        .replaceFirstChar { it.uppercase() }
                                                    Text(mName)
                                                },
                                                onClick = {
                                                    viewModel.onMonthFilterChange(month, menuYear)
                                                    showMonthDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            if (uiState.isLoading && uiState.publicTravels.isEmpty() && uiState.followingTravels.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (uiState.isFilterModeActive) {
                        if (uiState.isSearching) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                }
                            }
                        } else if (uiState.filterResults.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(stringResource(R.string.no_results_on_filter))
                                }
                            }
                        } else {
                            items(uiState.filterResults) { travel ->
                                TravelItem(
                                    travel = travel,
                                    pointsCount = travel.pointsCount,
                                    onClick = { onTravelClick(travel.id) }
                                )
                            }
                            if (uiState.isLoadingMoreFiltered) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                    }
                                }
                            }
                        }
                    } else if (uiState.isSearchModeActive) {
                        if (uiState.isSearching) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                }
                            }
                        } else if (uiState.searchResults.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        stringResource(
                                            R.string.no_results_for_query,
                                            uiState.searchQuery
                                        ))
                                }
                            }
                        } else {
                            items(uiState.searchResults) { travel ->
                                TravelItem(
                                    travel = travel,
                                    pointsCount = travel.pointsCount,
                                    onClick = { onTravelClick(travel.id) }
                                )
                            }
                        }
                    } else {
                        item {
                            BotonDescubrirViajes(
                                onClick = onSeeAllClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }

                        items(uiState.publicTravels) { travel ->
                            TravelItem(
                                travel = travel,
                                pointsCount = travel.pointsCount,
                                onClick = { onTravelClick(travel.id) }
                            )
                        }

                        item {
                            BotonAventuraSeguidos(
                                onClick = { },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            )
                        }

                        items(uiState.followingTravels) { travel ->
                            TravelItem(
                                travel = travel,
                                pointsCount = travel.pointsCount,
                                onClick = { onTravelClick(travel.id) }
                            )
                        }

                        if (uiState.isLoadingMoreFollowing) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}