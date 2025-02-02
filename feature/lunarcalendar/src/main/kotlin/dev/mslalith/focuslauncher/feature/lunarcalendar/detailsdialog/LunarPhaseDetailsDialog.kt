package dev.mslalith.focuslauncher.feature.lunarcalendar.detailsdialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mslalith.focuslauncher.core.common.model.getOrNull
import dev.mslalith.focuslauncher.core.model.lunarphase.LunarPhaseDetails
import dev.mslalith.focuslauncher.feature.lunarcalendar.LunarCalendarViewModel
import dev.mslalith.focuslauncher.feature.lunarcalendar.R

@Composable
fun LunarPhaseDetailsDialog(
    onClose: () -> Unit
) {
    LunarPhaseDetailsDialogInternal(
        onClose = onClose
    )
}

@Composable
internal fun LunarPhaseDetailsDialogInternal(
    lunarCalendarViewModel: LunarCalendarViewModel = hiltViewModel(),
    onClose: () -> Unit
) {
    val lunarPhaseDetailsState by lunarCalendarViewModel.lunarCalendarState.collectAsStateWithLifecycle()
    lunarPhaseDetailsState.lunarPhaseDetails.getOrNull()?.let { phaseDetails ->
        LunarPhaseDetailsDialog(
            lunarPhaseDetails = phaseDetails,
            onClose = onClose
        )
    }
}

@Composable
internal fun LunarPhaseDetailsDialog(
    lunarPhaseDetails: LunarPhaseDetails,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(
                onClick = onClose,
                content = { Text(text = stringResource(id = R.string.ok)) }
            )
        },
        text = {
            DialogContent(lunarPhaseDetails = lunarPhaseDetails)
        }
    )
}

@Composable
private fun DialogContent(
    lunarPhaseDetails: LunarPhaseDetails,
    contentColor: Color = AlertDialogDefaults.textContentColor
) {
    Column {
        TodayLunarPhase(
            lunarPhaseDetails = lunarPhaseDetails,
            contentColor = contentColor
        )

        Divider(
            modifier = Modifier.padding(vertical = 16.dp)
        )

        NextMajorPhaseDetails(
            nextPhaseDetails = lunarPhaseDetails.nextPhaseDetails,
            contentColor = contentColor
        )

        Divider(
            modifier = Modifier.padding(vertical = 16.dp)
        )

        LunarRiseAndSetDetails(
            lunarPhaseDetails = lunarPhaseDetails,
            contentColor = contentColor
        )
    }
}
