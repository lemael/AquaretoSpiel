package service.network

import tools.aqua.bgw.core.BoardGameApplication
import edu.udo.cs.sopra.ntf.*
import entity.HOTSEAT
import entity.onlinePlayerIs
import service.AbstractRefreshingService
import tools.aqua.bgw.net.client.BoardGameClient
import tools.aqua.bgw.net.client.NetworkLogging
import tools.aqua.bgw.net.common.annotations.GameActionReceiver
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import tools.aqua.bgw.net.common.response.*

/**
 * [BoardGameClient] implementation for network communication.
 *
 * @param networkService the [NetworkService] to potentially forward received messages to.
 */
class NetworkClient(
    playerName: String,
    host: String,
    secret: String,
    var networkService: NetworkService,
): BoardGameClient(playerName, host, secret, NetworkLogging.VERBOSE) {

    /** the identifier of this game session; can be null if no session started yet. */
    var sessionID: String? = null

    /** the name of the opponent player; can be null if no message from the opponent received yet */
    var otherPlayerName: MutableList<String> = mutableListOf()

    /**
     * Handle a [CreateGameResponse] sent by the server. Will await the guest player when its
     * status is [CreateGameResponseStatus.SUCCESS]. As recovery from network problems is not
     * implemented in NetWar, the method disconnects from the server and throws an
     * [IllegalStateException] otherwise.
     *
     * @throws IllegalStateException if status != success or currently not waiting for a game creation response.
     */
    override fun onCreateGameResponse(response: CreateGameResponse) {
        BoardGameApplication.runOnGUIThread {
            //check(networkService.connectionState == ConnectionState.WAITING_FOR_HOST_CONFIRMATION)
            { "unexpected CreateGameResponse" }
            var myPlayerName = this.playerName


            when (response.status) {
                CreateGameResponseStatus.SUCCESS -> {
                    //networkService.updateConnectionState(ConnectionState.WAITING_FOR_GUESTS)
                    sessionID = response.sessionID
                    networkService.onAllRefreshables { refreshAfterSessionCreated(sessionID?: "Session must be there ",myPlayerName) }
                }
                else -> disconnectAndError(response.status)
            }
        }
    }

    /**
     * Handle a [JoinGameResponse] sent by the server. Will await the init message when its
     * status is [JoinGameResponseStatus.SUCCESS]. As recovery from network problems is not
     * implemented in NetWar, the method disconnects from the server and throws an
     * [IllegalStateException] otherwise.
     *
     * @throws IllegalStateException if status != success or currently not waiting for a join game response.
     */
    override fun onJoinGameResponse(response: JoinGameResponse) {
        BoardGameApplication.runOnGUIThread {
            //check(networkService.connectionState == ConnectionState.WAITING_FOR_JOIN_CONFIRMATION)
            { "unexpected JoinGameResponse" }

            when (response.status) {
                JoinGameResponseStatus.SUCCESS -> {
                    otherPlayerName.addAll( response.opponents)
                    sessionID = response.sessionID
                    //networkService.updateConnectionState(ConnectionState.WAITING_FOR_INIT)
                }
                else -> disconnectAndError(response.status)
            }

            networkService.onAllRefreshables { refreshAfterSelfJoined(this@NetworkClient.playerName,otherPlayerName) }
        }
    }

    /**
     * Handle a [PlayerJoinedNotification] sent by the server. As War only supports two players,
     * this will immediately start the hosted game (and send the init message to the opponent).
     *
     * @throws IllegalStateException if not currently expecting any guests to join.
     */
    override fun onPlayerJoined(notification: PlayerJoinedNotification) {
        BoardGameApplication.runOnGUIThread {
            //check(networkService.connectionState == ConnectionState.WAITING_FOR_GUESTS )
            { "not awaiting any guests."}
            otherPlayerName.add(notification.sender)
            networkService.onAllRefreshables { refreshAfterPlayerJoined(otherPlayerName) }

        }
    }

    /**
     * Handle a [GameActionResponse] sent by the server. Does nothing when its
     * status is [GameActionResponseStatus.SUCCESS]. As recovery from network problems is not
     * implemented in NetWar, the method disconnects from the server and throws an
     * [IllegalStateException] otherwise.
     */
    override fun onGameActionResponse(response: GameActionResponse) {
        BoardGameApplication.runOnGUIThread {
            /*check(networkService.connectionState == ConnectionState.WAITING_FOR_MY_TURN ||
                    networkService.connectionState == ConnectionState.WAITING_FOR_OPPONENT)
            { "not currently playing in a network game."}
*/
            when (response.status) {
                GameActionResponseStatus.SUCCESS -> {} // do nothing in this case
                else -> disconnectAndError(response.status)
            }
        }
    }

    /**
     * handle a [InitGameMessage] sent by the server
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onInitReceived(message: InitGameMessage, sender: String) {
        BoardGameApplication.runOnGUIThread {
            var myPlayerName = this.playerName
            //TODO: Pass the local players name and player type, instead of placeholder constants

            networkService.startNewJoinedGame(message, myPlayerName, onlinePlayerIs )
        }
    }

    /**
     * handle a [AddTileToTruckMessage] sent by the server
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onAddTileToTruck(message: AddTileToTruckMessage, sender: String) {
        BoardGameApplication.runOnGUIThread {

            networkService.receiveAddTileToTruck(
                message = message,
            )
        }
    }

    /**
     * handle a [TakeTruckMessage] sent by the server
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onTakeTruck(message: TakeTruckMessage, sender: String) {
        BoardGameApplication.runOnGUIThread {

            networkService.receiveTakeTruck(
                message = message,
            )
        }
    }

    /**
     * handle a [BuyExpansionMessage] sent by the server
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onBuyExpansion(message: BuyExpansionMessage, sender: String) {
        BoardGameApplication.runOnGUIThread {

            networkService.receiveBuyExpansion(
                message = message,
            )
        }
    }

    /**
     * handle a [MoveCoworkerMessage] sent by the server
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onMoveCoworker(message: MoveCoworkerMessage, sender: String) {
        BoardGameApplication.runOnGUIThread {

            networkService.receiveMoveCoworker(
                message = message,
            )
        }
    }

    /**
     * handle a [MoveTileMessage] sent by the server
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onMoveTile(message: MoveTileMessage, sender: String) {
        BoardGameApplication.runOnGUIThread {

            networkService.receiveMoveTile(
                message = message,
            )
        }
    }

    /**
     * handle a [DiscardMessage] sent by the server
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onDiscard(message: DiscardMessage, sender: String) {
        BoardGameApplication.runOnGUIThread {

            networkService.receiveDiscard(
                message = message,
            )
        }
    }

    private fun disconnectAndError(message: Any) {
        networkService.disconnect()
        error(message)
    }

}