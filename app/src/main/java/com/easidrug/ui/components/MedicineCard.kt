package com.easidrug.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.easidrug.ui.UIConnector

@Composable
fun MedicineCard(
    connector: UIConnector,
    medicine: UIConnector.ScheduleEntry,
    animatable: Animatable<Float, AnimationVector1D>,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .graphicsLayer {
                scaleX = animatable.value
                scaleY = animatable.value
            }
            .fillMaxWidth()
//            .align(Alignment.Center)
            .padding(top = 20.dp, end = 5.dp, start = 5.dp)
            .clip(RoundedCornerShape(30.dp))
            .border(
                2.dp,
                MaterialTheme.colorScheme.tertiary,
                RoundedCornerShape(30.dp)
            )
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )

    ) {
        MedicineListItem(
            medicineName = medicine.pillName,
            schedule = medicine.schedule,
            onEditClick = {
                onEdit()
            },
            onDeleteClick = {
                onDelete()
//                connector.deleteSchedule(medicine.pillName)
            },
            connector = connector
        )
    }
}