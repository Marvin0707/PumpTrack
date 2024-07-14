package com.example.pumptrack

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier

@Composable
fun AddFab(showDialog: MutableState<Boolean>, modifier: Modifier = Modifier) {
    FloatingActionButton(onClick = {  showDialog.value = true}, modifier = modifier) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
    }
}
