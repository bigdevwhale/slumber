package app.slumber.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.slumber.R
import app.slumber.ui.T
import app.slumber.ui.theme.U
import app.slumber.ui.theme.UType

/**
 * A time of day, set on a clock face as on an alarm clock (#1): any hour and minute, so a night owl who
 * sleeps at five in the morning can say so. [minutes] after midnight in and out.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeField(label: String, minutes: Int, modifier: Modifier = Modifier, onPick: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Row(
        modifier.fillMaxWidth().border(1.dp, U.c.line, RoundedCornerShape(14.dp)).clickable { open = true }.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = UType.Sub, modifier = Modifier.weight(1f))
        Text(T.time(minutes), style = UType.Strong.copy(color = U.c.lamp))
    }
    if (open) {
        val state = rememberTimePickerState(minutes / 60 % 24, minutes % 60, android.text.format.DateFormat.is24HourFormat(LocalContext.current))
        AlertDialog(
            onDismissRequest = { open = false },
            containerColor = U.c.bg,
            confirmButton = { TextButton({ onPick(state.hour * 60 + state.minute); open = false }) { Text(stringResource(R.string.ok), color = U.c.lamp) } },
            dismissButton = { TextButton({ open = false }) { Text(stringResource(R.string.cancel), color = U.c.muted) } },
            text = { TimePicker(state, colors = TimePickerDefaults.colors(selectorColor = U.c.lamp)) },
        )
    }
}
