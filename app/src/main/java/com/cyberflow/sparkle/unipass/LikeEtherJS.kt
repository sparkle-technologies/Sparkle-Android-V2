package com.cyberflow.sparkle.unipass

import android.util.Log
import com.unipass.smartAccount.ChainID
import com.unipass.smartAccount.ChainOptions
import com.unipass.smartAccount.EOASigner
import com.unipass.smartAccount.SmartAccount
import com.unipass.smartAccount.SmartAccountInitOptions
import com.unipass.smartAccount.SmartAccountOptions
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys
import uniffi.shared.SendingTransactionOptions
import uniffi.shared.Transaction

/**

demo提供的私钥
0xa73c09635a405d858f0aa373fba9f2980afa76639eb4347a273cf92110d2350a
EOA  address= 0x810d9ef492fab67c413fce57faa1b6707735b04d
CA address=0x79e77f4426e9a55d2f0960ce7828bdcaa6efca15

web3auth给的
0x42b5d1bc8d41dfdfe2a0e0a173cd1beb5222c1504872df7daf9a385260b8a445
EOA  address= 0xcfacdd82313c7752d4290a931a777124d07d6914
CA address=0x4da68551afb950877572fe36ef68e347a659f84e

支付gas费 USDC
https://mumbai.polygonscan.com/address/0x87f0e95e11a49f56b329a1c143fb22430c07332a
自己发的测试币
USDSparkle (USDS) 合约地址   0xC27a2E187D05b690FC3588A635111B462020124D
自己的钱包  里面有测试币 MATIC  1000个USDC   1000个USDS
0xE1c026085863e37321DbF7871c6d28a79153c888

EOA通过CA进行操作  使用的是CA里面的钱作为gas费  比如用USDC 消耗的是CA账号的钱

FeeOption
token=0x569f5ff11e259c8e0639b4082a0db91581a6b83e, name=USDT, symbol=USDT, decimals=6, to=0xba1fa99a0ce4daede485b3195a609310bbd76b64, amount=178, error=null
token=0x87f0e95e11a49f56b329a1c143fb22430c07332a, name=USDC, symbol=USDC, decimals=6, to=0xba1fa99a0ce4daede485b3195a609310bbd76b64, amount=199, error=null
token=0x0000000000000000000000000000000000000000, name=MATIC, symbol=MATIC, decimals=18, to=0xba1fa99a0ce4daede485b3195a609310bbd76b64, amount=355302004263624, error=null
 */
class LikeEtherJS {

    //    var sPrivatekeyInHex = "0xa73c09635a405d858f0aa373fba9f2980afa76639eb4347a273cf92110d2350a"
    var sPrivatekeyInHex = "0x42b5d1bc8d41dfdfe2a0e0a173cd1beb5222c1504872df7daf9a385260b8a445"

    companion object {
        val TAG = "LikeEtherJS"
        const val appId = "9e145ea3e5525ee793f39027646c4511"
        const val unipassServerUrl = "https://t.wallet.unipass.vip/wallet-coustom-auth"
        const val rpcUrl = "https://node.wallet.unipass.id/polygon-mumbai"
        const val relayerUrl = "https://t.wallet.unipass.vip/relayer-v2-polygon"
        const val TOKEN_USDC = "0x87f0e95e11a49f56b329a1c143fb22430c07332a"
        const val TOKEN_USDT = "0x569f5ff11e259c8e0639b4082a0db91581a6b83e"
        const val TOKEN_MATIC = "0x0000000000000000000000000000000000000000"
        val SELECT_CHAIN_ID: ChainID = ChainID.POLYGON_MUMBAI
    }

    private suspend fun initSmartContract(): SmartAccount? {
        try {
            val signer = EOASigner(sPrivatekeyInHex)
            signer.address()

            Log.e(TAG, "EOA  address= ${signer.address()}")

            /// init smart account instance
            val smartAccountOption = SmartAccountOptions(
                signer,
//                null,
                appId,
                unipassServerUrl,
                arrayOf(
                    ChainOptions(
                        SELECT_CHAIN_ID,
                        rpcUrl,
                        relayerUrl
                    )
                )
            )

            val smartAccount = SmartAccount(smartAccountOption)
            smartAccount.init(SmartAccountInitOptions(ChainID.POLYGON_MUMBAI))
            /// get smart account address

            val address = smartAccount.address()
            Log.e(TAG, "CA address=$address")
            Log.e(TAG, "CA isDeployed=${smartAccount.isDeployed()}")
            Log.e(TAG, "CA chainId=${smartAccount.chainId()}")

            return smartAccount
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    suspend fun testSmartAccount() {
        try {
            val smartAccount = initSmartContract()
            if (smartAccount == null) {
                Log.e(TAG, "initSmartContract failed")
                return
            }

            // send 1 USDSparkle (USDS) to 0x87F0E95E11a49f56b329A1c143Fb22430C07332a
            val tx = Transaction(
                "0xC27a2E187D05b690FC3588A635111B462020124D",   // USDSparkle (USDS) 合约地址
                "a9059cbb00000000000000000000000087f0e95e11a49f56b329a1c143fb22430c07332a0000000000000000000000000000000000000000000000000de0b6b3a7640000",
                "0x0"
            )

            /// simulate transaction to fetch tx fee
            val simulateRet = smartAccount.simulateTransaction(tx)
            Log.e(TAG, "SimulateRet: $simulateRet")

            // token=0x569f5ff11e259c8e0639b4082a0db91581a6b83e, name=USDT, symbol=USDT, decimals=6, to=0xba1fa99a0ce4daede485b3195a609310bbd76b64, amount=178, error=null
            // token=0x87f0e95e11a49f56b329a1c143fb22430c07332a, name=USDC, symbol=USDC, decimals=6, to=0xba1fa99a0ce4daede485b3195a609310bbd76b64, amount=199, error=null
            // token=0x0000000000000000000000000000000000000000, name=MATIC, symbol=MATIC, decimals=18, to=0xba1fa99a0ce4daede485b3195a609310bbd76b64, amount=355302004263624, error=null
            val sendTransactionOptions = SendingTransactionOptions(null)
//            val sendTransactionOptions = SendingTransactionOptions(null, smartAccountOption.chainOptions[0].chainId.iD.toULong())
            if (simulateRet!!.isFeeRequired) {
                sendTransactionOptions.fee =
                    simulateRet?.feeOptions?.find { it.token.lowercase() == TOKEN_USDC.lowercase() }
            }
            // send transaction to get tx hash
            Log.e(TAG, sendTransactionOptions.toString())
            val txHash = smartAccount.sendTransaction(tx, sendTransactionOptions)
            Log.e(TAG, "txHash.toString()=$txHash")

//            // get transaction receipt from chain
            val receipt =
                smartAccount.waitTransactionReceiptByHash(txHash!!, 2, SELECT_CHAIN_ID, 60)
            Log.e(TAG, "waitTransactionReceiptByHash receipt=$receipt")

        } catch (err: Exception) {
            err.printStackTrace()
            Log.e(TAG, "-----$err")
        }
    }

    suspend fun signMsg() {
        val smartAccount = initSmartContract()
        if (smartAccount == null) {
            Log.e(TAG, "initSmartContract failed")
            return
        }

        val msg = "HAHA"
        val signedMsg = smartAccount.signMessage(msg)
        Log.e(TAG, "signMsg:  msg=$msg")
        Log.e(TAG, "signMsg:  signedMsg=$signedMsg")
    }

    fun createAccount() {
        val ecKeyPair: ECKeyPair = Keys.createEcKeyPair()
        val pk = ecKeyPair.publicKey.toString(16)
        val privateKey = ecKeyPair.privateKey.toString(16)
        val result = "publicKey=$pk   privateKey=$privateKey"
        Log.e(TAG, "createAccount: publicKey=$pk   privateKey=$privateKey")
    }
}