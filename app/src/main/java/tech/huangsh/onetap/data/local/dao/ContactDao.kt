package tech.huangsh.onetap.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import tech.huangsh.onetap.data.model.Contact

/**
 * 联系人数据访问对象
 */
@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY `order` ASC, createdAt ASC")
    fun getAllContacts(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getContactById(id: Long): Contact?

    @Query("SELECT * FROM contacts WHERE name LIKE '%' || :searchQuery || '%' ORDER BY `order` ASC")
    fun searchContacts(searchQuery: String): Flow<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact): Long

    @Update
    suspend fun updateContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteContactById(id: Int)

    @Query("UPDATE contacts SET `order` = :newOrder WHERE id = :contactId")
    suspend fun updateContactOrder(contactId: Long, newOrder: Int)

    @Query("SELECT MAX(`order`) FROM contacts")
    suspend fun getMaxOrder(): Int?

    @Query("SELECT COUNT(*) FROM contacts")
    fun getContactCount(): Flow<Int>

    @Query("DELETE FROM contacts")
    suspend fun deleteAllContacts()
}