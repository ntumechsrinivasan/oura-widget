package dev.srini.ourawidget.ui

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import dev.srini.ourawidget.OuraWidget
import dev.srini.ourawidget.R
import dev.srini.ourawidget.data.OuraRepository
import dev.srini.ourawidget.data.OuraResult
import dev.srini.ourawidget.data.TokenStore
import dev.srini.ourawidget.work.WidgetUpdateScheduler
import kotlinx.coroutines.launch

class ConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // If the system backs out of the configure flow without us finishing,
        // treat it as cancelled rather than silently placing a broken widget.
        setResult(RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))

        val tokenStore = TokenStore(applicationContext)
        val repository = OuraRepository(applicationContext)

        setContent {
            MaterialTheme {
                Surface {
                    SetupScreen(
                        initialToken = tokenStore.getToken(),
                        onSave = { token -> saveAndValidate(tokenStore, repository, token) },
                        onFinishSetup = ::finishSetup
                    )
                }
            }
        }
    }

    private suspend fun saveAndValidate(
        tokenStore: TokenStore,
        repository: OuraRepository,
        token: String
    ): SaveOutcome {
        tokenStore.setToken(token)
        return when (repository.refresh()) {
            is OuraResult.Success -> SaveOutcome.Success
            is OuraResult.Unauthorized -> {
                tokenStore.clear()
                SaveOutcome.InvalidToken
            }
            is OuraResult.NetworkError, is OuraResult.NotConfigured -> SaveOutcome.NetworkError
        }
    }

    private fun finishSetup() {
        WidgetUpdateScheduler.ensurePeriodicRefresh(applicationContext)
        WidgetUpdateScheduler.refreshNow(applicationContext)

        lifecycleScope.launch {
            OuraWidget().updateAll(applicationContext)
        }

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
        }
        finish()
    }
}

private enum class SaveOutcome { Success, InvalidToken, NetworkError }

@Composable
private fun SetupScreen(
    initialToken: String?,
    onSave: suspend (String) -> SaveOutcome,
    onFinishSetup: () -> Unit
) {
    var token by remember { mutableStateOf(initialToken.orEmpty()) }
    var isSaving by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val invalidTokenMessage = stringResource(R.string.setup_error_invalid)
    val networkErrorMessage = stringResource(R.string.setup_error_network)
    val emptyTokenMessage = stringResource(R.string.setup_error_empty)

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = stringResource(R.string.setup_title), style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.padding(top = 8.dp))
            Text(text = stringResource(R.string.setup_body), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.padding(top = 24.dp))

            OutlinedTextField(
                value = token,
                onValueChange = {
                    token = it
                    errorText = null
                },
                label = { Text(stringResource(R.string.setup_token_label)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
                isError = errorText != null
            )

            if (errorText != null) {
                Spacer(modifier = Modifier.padding(top = 8.dp))
                Text(
                    text = errorText.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.padding(top = 24.dp))

            Button(
                onClick = {
                    if (token.isBlank()) {
                        errorText = emptyTokenMessage
                        return@Button
                    }
                    isSaving = true
                    scope.launch {
                        when (onSave(token.trim())) {
                            SaveOutcome.Success -> {
                                isSaving = false
                                onFinishSetup()
                            }
                            SaveOutcome.InvalidToken -> {
                                isSaving = false
                                errorText = invalidTokenMessage
                            }
                            SaveOutcome.NetworkError -> {
                                isSaving = false
                                errorText = networkErrorMessage
                            }
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                }
                Text(if (isSaving) "Checking..." else stringResource(R.string.setup_save))
            }

            if (!initialToken.isNullOrBlank()) {
                TextButton(onClick = onFinishSetup, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.setup_continue))
                }
            }
        }
    }
}
