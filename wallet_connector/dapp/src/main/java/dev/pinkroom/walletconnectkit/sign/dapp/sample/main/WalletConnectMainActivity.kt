package dev.pinkroom.walletconnectkit.sign.dapp.sample.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.walletconnect.sign.client.Sign
import dev.pinkroom.walletconnectkit.core.WalletConnectKitConfig
import dev.pinkroom.walletconnectkit.core.accounts
import dev.pinkroom.walletconnectkit.sign.dapp.WalletConnectKit
import dev.pinkroom.walletconnectkit.sign.dapp.components.WalletConnectKitButton
import dev.pinkroom.walletconnectkit.sign.dapp.sample.theme.WalletConnectKitTheme
import timber.log.Timber

class WalletConnectMainActivity : ComponentActivity() {
    private lateinit var walletConnectKit: WalletConnectKit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = WalletConnectKitConfig(
            projectId = "216dc6e2b36be94b855cd28ea41fda6d",
            appUrl = "https://sparkle.fun",
        )
        walletConnectKit = WalletConnectKit.builder(this).config(config).build()
        setContent {
            WalletConnectKitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Content(walletConnectKit)
                }
            }
        }
    }
}

@Composable
fun Content(walletConnectKit: WalletConnectKit) {
    val activeSessions by walletConnectKit.activeSessions.collectAsStateWithLifecycle(initialValue = emptyList())
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        WalletConnectKitButton(walletConnectKit)

        /*if (activeSessions.isEmpty()) {
            WalletConnectKitButton(walletConnectKit)
        } else {
            // do sth  具体 操作在  AccountActions
            AccountScreen(activeSessions, walletConnectKit)
        }*/
    }
}


@Composable
private fun AccountScreen(
    activeSessions: List<Sign.Model.Session>,
    walletConnectKit: WalletConnectKit,
) {
    val accounts = activeSessions.flatMap { it.accounts }.map { it.address to it }
    walletConnectKit.activeAccount?.let { account ->
        DropdownMenu(
            modifier = Modifier.padding(32.dp),
            items = accounts,
            selectedItem = account.address to account,
            onItemClick = { walletConnectKit.activeAccount = it.second },
            leadingIcon = { DropdownWalletIcon(account.icon) },
            itemContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DropdownWalletIcon(account.icon)
                    Text(
                        modifier = Modifier.padding(start = 12.dp),
                        text = it.first.middleOverflow(),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            },
        )
        AccountActions(walletConnectKit = walletConnectKit)
    }
}

@Composable
private fun DropdownWalletIcon(icon: String?) =
    AsyncImage(
        modifier = Modifier.size(24.dp),
        model = ImageRequest.Builder(LocalContext.current)
            .data(icon)
            .build(),
        contentDescription = "Wallet Icon",
    )