package com.easidrug.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easidrug.ui.UIConnector

@Composable
fun MedicineListItem(
    medicineName: String,
    schedule: MutableList<UIConnector.DayTimePair<String, UIConnector.Time>>,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    connector: UIConnector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = medicineName,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
//            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Column(modifier = Modifier.weight(1.7f)) {
            schedule.forEach { pair ->
                Text(
                    text = connector.formatScheduleNew(pair),
                    fontSize = 16.sp,
                )
            }
        }

        Row(modifier = Modifier.weight(1f)) {
            IconButton(
                onClick = onEditClick
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(
                onClick = onDeleteClick
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}