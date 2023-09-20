package dev.pinkroom.walletconnectkit.sign.dapp

import android.util.Log
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

internal object DAppDelegate : SignClient.DappDelegate {

    val TAG = "DAppDelegate"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _events = MutableSharedFlow<Sign.Model?>()

    val events = _events.asSharedFlow()

    init {
        SignClient.setDappDelegate(this)
    }

    override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {
        Log.e(TAG, "onSessionApproved: ")
        scope.launch { _events.emit(approvedSession) }
    }

    override fun onSessionRejected(rejectedSession: Sign.Model.RejectedSession) {
        Log.e(TAG, "onSessionRejected: " )
        scope.launch { _events.emit(rejectedSession) }
    }

    override fun onSessionUpdate(updatedSession: Sign.Model.UpdatedSession) {
        Log.e(TAG, "onSessionUpdate: " )
        scope.launch { _events.emit(updatedSession) }
    }

    override fun onSessionEvent(sessionEvent: Sign.Model.SessionEvent) {
        Log.e(TAG, "onSessionEvent: " )
        scope.launch { _events.emit(sessionEvent) }
    }

    override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {
        Log.e(TAG, "onSessionDelete: ")
        scope.launch { _events.emit(deletedSession) }
    }

    override fun onSessionExtend(session: Sign.Model.Session) {
        Log.e(TAG, "onSessionExtend: " )
        scope.launch { _events.emit(session) }
    }

    override fun onSessionRequestResponse(response: Sign.Model.SessionRequestResponse) {
        Log.e(TAG, "onSessionRequestResponse: " )
        scope.launch { _events.emit(response) }
    }

    override fun onConnectionStateChange(state: Sign.Model.ConnectionState) {
        Log.e(TAG, "onConnectionStateChange: " )
        scope.launch { _events.emit(state) }
    }

    override fun onError(error: Sign.Model.Error) {
        Log.e(TAG, "onError: " )
        scope.launch { _events.emit(error) }
    }

    fun deletedSession(sessionTopic: String) =
        onSessionDelete(Sign.Model.DeletedSession.Success(sessionTopic, "Delete Session"))
}