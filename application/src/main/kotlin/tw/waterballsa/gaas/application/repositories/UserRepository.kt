package tw.waterballsa.gaas.application.repositories

import tw.waterballsa.gaas.domain.User
import tw.waterballsa.gaas.domain.User.Id

interface UserRepository {
    fun findById(id: Id): User?
    fun existsByIdentitiesIn(identityProviderId: String): Boolean
    fun existsUserByEmail(email: String): Boolean
    fun createUser(user: User): User
    fun deleteAll()
    fun findAllById(ids: Collection<Id>): List<User>
}
