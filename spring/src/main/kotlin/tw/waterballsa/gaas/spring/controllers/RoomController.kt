package tw.waterballsa.gaas.spring.controllers

import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tw.waterballsa.gaas.application.usecases.CreateRoomUsecase
import tw.waterballsa.gaas.application.usecases.Presenter
import tw.waterballsa.gaas.domain.GameRegistration
import tw.waterballsa.gaas.domain.Room
import tw.waterballsa.gaas.events.CreatedRoomEvent
import tw.waterballsa.gaas.events.DomainEvent
import tw.waterballsa.gaas.spring.controllers.RoomController.CreateRoomViewModel
import tw.waterballsa.gaas.spring.extensions.getEvent
import javax.validation.Valid
import javax.validation.constraints.Pattern

@RestController
@RequestMapping("/rooms")
class RoomController(
    private val createRoomUsecase: CreateRoomUsecase
) {
    @PostMapping
    fun createRoom(
        @AuthenticationPrincipal principal: OidcUser,
        @RequestBody @Valid request: CreateRoomRequest
    ): ResponseEntity<Any> {
        val presenter = CreateRoomPresenter()
        createRoomUsecase.execute(request.toRequest(principal.subject), presenter)
        return presenter.viewModel
            ?.let { ResponseEntity.status(CREATED).body(it) }
            ?: ResponseEntity.noContent().build()
    }

    class CreateRoomRequest(
        private val name: String,
        private val gameId: String,
        @field:Pattern(regexp = """^\d{4}$""", message = "The length must be 4 and can only contain digits.")
        private val password: String? = null,
        private val maxPlayers: Int,
        private val minPlayers: Int,
    ) {
        fun toRequest(hostId: String): CreateRoomUsecase.Request =
            CreateRoomUsecase.Request(
                gameId = gameId,
                hostId = hostId,
                maxPlayers = maxPlayers,
                minPlayers = minPlayers,
                name = name,
                password = password
            )
    }

    class CreateRoomPresenter : Presenter {
        var viewModel: CreateRoomViewModel? = null
            private set

        override fun present(vararg events: DomainEvent) {
            viewModel = events.getEvent(CreatedRoomEvent::class)?.toViewModel()
        }

        private fun CreatedRoomEvent.toViewModel(): CreateRoomViewModel =
            CreateRoomViewModel(
                id = roomId,
                game = game.toView(),
                host = host.toView(),
                currentPlayers = currentPlayers,
                maxPlayers = maxPlayers,
                minPlayers = minPlayers,
                name = name,
                isLocked = isLocked
            )
    }

    data class CreateRoomViewModel(
        val id: Room.Id,
        val name: String,
        val game: Game,
        val host: Player,
        val isLocked: Boolean,
        val currentPlayers: Int,
        val maxPlayers: Int,
        val minPlayers: Int,
    ) {
        data class Game(val id: String, val name: String)
        data class Player(val id: String, val nickname: String)
    }
}

private fun GameRegistration.toView(): CreateRoomViewModel.Game =
    CreateRoomViewModel.Game(id!!.value, displayName)

private fun Room.Player.toView(): CreateRoomViewModel.Player =
    CreateRoomViewModel.Player(id.value, nickname)
