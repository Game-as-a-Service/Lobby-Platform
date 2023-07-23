package tw.waterballsa.gaas.application.usecases

import tw.waterballsa.gaas.application.repositories.UserRepository
import tw.waterballsa.gaas.domain.User
import tw.waterballsa.gaas.exceptions.NotFoundException.Companion.notFound
import javax.inject.Named

@Named
class GetUserUseCase(
    private val userRepository: UserRepository,
) {
    fun execute(request: Request, presenter: Presenter) {
        with(request) {
            val user = userRepository.findByIdentity(userIdentity)
                ?: throw notFound(User::class).identifyBy("userIdentity", userIdentity)
            presenter.present(user)
        }
    }

    class Request(val userIdentity: String)

    interface Presenter {
        fun present(user: User)
    }
}
