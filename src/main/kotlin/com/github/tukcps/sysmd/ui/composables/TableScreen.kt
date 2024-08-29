package com.github.tukcps.sysmd.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun TableScreen(
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Data Table Demo")
                },
                navigationIcon = {
                    IconButton(onClick = { /* Handle navigation icon clicked */ }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Navigation menu")
                    }
                },
                backgroundColor = Color.Green,
                contentColor = Color.White
            )
        }
    ) {
        LazyColumn(
            Modifier.padding(8.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {

                }
            }
        }
    }}


data class Invoice(val invoice: String, val date: String, val status: String, val amount: String)

val invoiceList = listOf(
    Invoice("51023", "15/04/2023", "Unpaid", amount = "$2,600"),
    Invoice("51024", "17/04/2023", "Pending", amount = "$900"),
    Invoice("51025", "20/04/2023", "Paid", amount = "$7,560"),
    Invoice("51026", "23/04/2023", "Pending", amount = "$300"),
    Invoice("51027", "30/04/2023", "Paid", amount = "$5,890"),
)