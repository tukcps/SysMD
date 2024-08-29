package com.github.tukcps.sysmd.ui

/*
@Composable
fun connectionDialog(openDialog: MutableState<Boolean>,reconnect:()->Unit){
    Dialog(
        title = "Connect to Backend",
        resizable = false,
        onCloseRequest = {openDialog.value=false },
        state = rememberDialogState(position = WindowPosition(Alignment.Center), size = DpSize(width = 300.dp, height = 600.dp))
    ) {

        Surface(modifier = Modifier.size(width = 300.dp, height = 600.dp), elevation = 5.dp) {
            Column(Modifier.fillMaxSize(), Arrangement.spacedBy(10.dp)) {
                uploadInput("",openDialog,true,reconnect)
            }
        }
    }
}

@Composable
fun uploadPopup(uploadText: String, onDismissRequest:()->Unit) {
    Popup(alignment = Alignment.TopEnd, offset = IntOffset(-10,10), focusable = true, onDismissRequest = onDismissRequest) {
        uploadInput(uploadText, mutableStateOf(false), false,{})
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun uploadInput(uploadText: String, open:MutableState<Boolean>,showCancelButton:Boolean, reconnect: () -> Unit) {
    var baseUrlText by remember  { mutableStateOf(AgilaCloudSession.baseURI) }
    var entryUrlText by remember { mutableStateOf(AgilaCloudSession.entryURI) }
    var portText by AgilaRepository.port
    var usernameText by AgilaRepository.usernameState
    var pwdText by AgilaRepository.passwordState
    Surface(modifier = Modifier.size(width = 300.dp, height = 600.dp), elevation = 5.dp) {
        Column(Modifier.fillMaxSize(), Arrangement.spacedBy(10.dp)) {
            BasicText("Please enter the login credentials for the AGILA Cloud")
            OutlinedTextField(baseUrlText, { baseUrlText = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                label = { Text("BaseURI") }
            )
            OutlinedTextField(entryUrlText, { entryUrlText = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                label = { Text("EntryURI") }
            )
            OutlinedTextField(portText, { portText = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                label = { Text("Port") }
            )
            OutlinedTextField(usernameText, { usernameText = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                label = { Text("Username") }
            )
            OutlinedTextField(
                pwdText, { pwdText = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                keyboardOptions = KeyboardOptions(KeyboardCapitalization.Characters),
                // uncomment to make password invisible, only off for testing purposes
                visualTransformation = PasswordVisualTransformation(),
            )
            /**
             * the confirm Button
             */
            Button(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                onClick = {
                    AgilaCloudSession.pwdText = pwdText
                    AgilaCloudSession.baseURI = baseUrlText
                    AgilaCloudSession.entryURI = entryUrlText
                    //TODO allow only int input
                    AgilaCloudSession.port = try {
                        portText.toInt()
                    } catch (e: Exception) {
                        0
                    }
                    AgilaCloudSession.username = usernameText
                    AgilaCloudSession.confirm = true
                    // configuring Rest-Helper and uploading
                    Rest.baseURI = baseUrlText
                    Rest.entryURI = entryUrlText
                    Rest.port = AgilaCloudSession.port
                    open.value = false
                    reconnect()
                }) { Text("Confirm") }
            if (showCancelButton)
                Button(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    onClick = {
                        open.value = false
                    }) { Text("Cancel") }
        }
    }
}
*/