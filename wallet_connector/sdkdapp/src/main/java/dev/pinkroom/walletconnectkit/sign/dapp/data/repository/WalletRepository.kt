package dev.pinkroom.walletconnectkit.sign.dapp.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import com.google.gson.Gson
import dev.pinkroom.walletconnectkit.core.WalletConnectKitConfig
import dev.pinkroom.walletconnectkit.sign.dapp.data.model.ExplorerResponse
import dev.pinkroom.walletconnectkit.sign.dapp.data.model.Wallet
import dev.pinkroom.walletconnectkit.sign.dapp.data.model.toWallet
import dev.pinkroom.walletconnectkit.sign.dapp.data.service.ExplorerService
import java.io.BufferedReader
import java.io.InputStreamReader

internal class WalletRepository(
    private val context: Context,
    private val walletConnectKitConfig: WalletConnectKitConfig,
    private val explorerService: ExplorerService,
) {

    suspend fun getWalletsInstalled(chains: List<String>): List<Wallet> {
        val installedWalletsIds = getInstalledWalletsIds()
        val wallets = getAllWallets(chains)

        if (wallets == null) {
            context?.run {
                Toast.makeText(
                    this,
                    "fail to connect to wallet-connect, pls ensure your network",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        val allWallets = wallets?.listings?.map { it.value }?.map {
            it.toWallet(walletConnectKitConfig.projectId)
        } ?: return emptyList()
        return allWallets.filter { installedWalletsIds.contains(it.packageName) }
    }

    private fun getInstalledWalletsIds(): List<String> {
        val installedWallets = queryInstalledWallets()
        return installedWallets.map { it.activityInfo.packageName }
    }

    private fun queryInstalledWallets(): MutableList<ResolveInfo> {
        val intent = Intent(Intent.ACTION_VIEW, "wc://".toUri())
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()),
            )
        } else {
            context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        }
    }

    private suspend fun getAllWallets(chains: List<String>): ExplorerResponse? {
        try {
            // todo : save wallet info to local file, no need to request every time
            val json = readJSONFromAssets(context, "wallet.json")
            if(json.isNotEmpty()){
                Log.e("TAG", "getAllWallets: json is not empty", )
               return Gson().fromJson<ExplorerResponse>(json, ExplorerResponse::class.java)
            }else{
                val re = explorerService.getWallets(walletConnectKitConfig.projectId, chains.joinToString(","))
                return re.body()
            }
        } catch (e: Exception) {
            return null
        }
    }
}

fun readJSONFromAssets(context: Context, path: String): String {
    val TAG = "[ReadJSON]"
    try {
        val file = context.assets.open("$path")
        Log.e(TAG, " Found File: $file.")
        val bufferedReader = BufferedReader(InputStreamReader(file))
        val stringBuilder = StringBuilder()
        bufferedReader.useLines { lines ->
            lines.forEach {
                stringBuilder.append(it)
            }
        }
        Log.e(TAG, "getJSON   stringBuilder: $stringBuilder.")
        val jsonString = stringBuilder.toString()
        Log.e(TAG, " JSON as String: $jsonString.")
        return jsonString
    } catch (e: Exception) {
        Log.e(TAG, "Error reading JSON: $e.")
        e.printStackTrace()
        return ""
    }
}