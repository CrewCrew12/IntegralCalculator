package com.example.integralcalculator.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.integralcalculator.domain.model.CalcRecord
import com.example.integralcalculator.presentation.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onItemClick: (CalcRecord) -> Unit = {}
) {
    val records by viewModel.history.collectAsState(initial = emptyList())
    val userId by viewModel.userId.collectAsState(initial = null)

    var selectedRecord by remember { mutableStateOf<CalcRecord?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История вычислений") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 28.sp)
                    }
                }
            )
        }
    ) { padding ->
        if (selectedRecord != null) {
            DetailScreen(
                record = selectedRecord!!,
                onBack = { selectedRecord = null }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (records.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("История вычислений пуста\nПосчитайте что-нибудь!")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(records, key = { it.id }) { record ->
                            HistoryItem(
                                record = record,
                                onClick = { selectedRecord = record }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(record: CalcRecord, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(record.timestamp))
    val latexExpression = buildShortExpression(record)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Выражение",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LatexTextView(
                latex = latexExpression,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DetailScreen(
    record: CalcRecord,
    onBack: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(record.timestamp))
    val integralExpression = buildFullExpression(record)
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Text("←", fontSize = 24.sp)
            }
            Text(
                text = "Назад к списку",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Выражение",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LatexTextView(
                    latex = integralExpression,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Результат",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LatexTextView(
                    latex = record.result,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun buildShortExpression(record: CalcRecord): String {
    return if (record.isDefinite) {
        "\\int_{${record.lowerLimit}}^{${record.upperLimit}} ${record.expression} d${record.variable}"
    } else {
        "\\int ${record.expression} d${record.variable}"
    }
}

private fun buildFullExpression(record: CalcRecord): String {
    return if (record.isDefinite) {
        "\\int_{${record.lowerLimit}}^{${record.upperLimit}} (${record.expression}) d${record.variable}"
    } else {
        "\\int (${record.expression}) d${record.variable}"
    }
}