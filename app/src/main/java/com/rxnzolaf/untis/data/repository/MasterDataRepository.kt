package com.rxnzolaf.untis.data.repository

import androidx.compose.runtime.compositionLocalOf
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import com.rxnzolaf.untis.data.database.entities.CustomClassEntity
import com.rxnzolaf.untis.data.database.entities.CustomRoomEntity
import com.rxnzolaf.untis.data.database.entities.CustomSubjectEntity
import com.rxnzolaf.untis.data.database.entities.CustomTeacherEntity
import com.rxnzolaf.untis.data.database.entities.CustomSubjectNameEntity
import com.rxnzolaf.untis.data.database.entities.CustomWeekdayNameEntity
import com.rxnzolaf.untis.data.database.entities.ElementEntity
import com.rxnzolaf.untis.data.database.entities.KlasseEntity
import com.rxnzolaf.untis.data.database.entities.RoomEntity
import com.rxnzolaf.untis.data.database.entities.SubjectEntity
import com.rxnzolaf.untis.data.database.entities.TeacherEntity
import com.rxnzolaf.untis.data.database.entities.User
import com.rxnzolaf.untis.data.database.entities.UserDao
import com.rxnzolaf.untis.data.database.entities.UserWithData
import com.rxnzolaf.untis.models.PeriodItem.Companion.ELEMENT_NAME_UNKNOWN
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

interface MasterDataRepository {
	val user: User?
	val userData: UserWithData?

	val allClasses: StateFlow<List<PeriodElement>>
	val allTeachers: StateFlow<List<PeriodElement>>
	val allSubjects: StateFlow<List<PeriodElement>>
	val allRooms: StateFlow<List<PeriodElement>>

	fun getShortName(id: Long, type: ElementType? = null, default: String = ELEMENT_NAME_UNKNOWN): String
	fun getShortName(periodElement: PeriodElement, default: String = ELEMENT_NAME_UNKNOWN): String

	fun getLongName(id: Long, type: ElementType, default: String = ELEMENT_NAME_UNKNOWN): String
	fun getLongName(periodElement: PeriodElement, default: String = ELEMENT_NAME_UNKNOWN): String

	fun getWeekdayDisplayName(dayOfWeek: DayOfWeek): String
	fun getWeekdayDisplayShortName(dayOfWeek: DayOfWeek): String

	fun isAllowed(id: Long, type: ElementType? = null): Boolean
	fun isAllowed(periodElement: PeriodElement): Boolean
}

@Singleton
class DefaultMasterDataRepository : MasterDataRepository {
	override val user: User? = null
	override val userData: UserWithData? = null

	override val allClasses: StateFlow<List<PeriodElement>> = MutableStateFlow(emptyList())
	override val allTeachers: StateFlow<List<PeriodElement>> = MutableStateFlow(emptyList())
	override val allSubjects: StateFlow<List<PeriodElement>> = MutableStateFlow(emptyList())
	override val allRooms: StateFlow<List<PeriodElement>> = MutableStateFlow(emptyList())

	override fun getShortName(id: Long, type: ElementType?, default: String): String = "$type:$id"
	override fun getShortName(periodElement: PeriodElement, default: String) = getShortName(periodElement.id, periodElement.type, default)

	override fun getLongName(id: Long, type: ElementType, default: String): String = "$type:$id"
	override fun getLongName(periodElement: PeriodElement, default: String) = getLongName(periodElement.id, periodElement.type)

	override fun getWeekdayDisplayName(dayOfWeek: DayOfWeek): String = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
	override fun getWeekdayDisplayShortName(dayOfWeek: DayOfWeek): String = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

	override fun isAllowed(id: Long, type: ElementType?): Boolean = true
	override fun isAllowed(periodElement: PeriodElement): Boolean = true
}

@Singleton
class UntisMasterDataRepository @Inject constructor(
	private val userDao: UserDao,
	private val userRepository: UserRepository,
	private val customNamesRepository: CustomNamesRepository
) : MasterDataRepository {
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO);

	override val user: User?
		get() = (userRepository.userState.value as? UserRepository.UserState.User)?.user

	private val _userData = MutableStateFlow<UserWithData?>(null)
	override val userData: UserWithData?
		get() = _userData.value

	private val _allClasses = MutableStateFlow<Map<Long, KlasseEntity>>(emptyMap())
	private val _customClasses = MutableStateFlow<Map<Long, CustomClassEntity>>(emptyMap())
	override val allClasses: StateFlow<List<PeriodElement>> =
		combine(_allClasses, _customClasses) { untisMap, customMap ->
			if (user?.let { it.apiHost == "custom" || it.isCustom } == true) {
				customMap.values.sortedBy { it.name }.map { PeriodElement(ElementType.CLASS, it.id) }
			} else {
				untisMap.values.map { PeriodElement(ElementType.CLASS, it.id) }
			}
		}.stateIn(scope, SharingStarted.Eagerly, emptyList())

	private val _allTeachers = MutableStateFlow<Map<Long, TeacherEntity>>(emptyMap())
	private val _customTeachers = MutableStateFlow<Map<Long, CustomTeacherEntity>>(emptyMap())
	override val allTeachers: StateFlow<List<PeriodElement>> =
		combine(_allTeachers, _customTeachers) { untisMap, customMap ->
			if (user?.let { it.apiHost == "custom" || it.isCustom } == true) {
				customMap.values.sortedBy { it.name }.map { PeriodElement(ElementType.TEACHER, it.id) }
			} else {
				untisMap.values.map { PeriodElement(ElementType.TEACHER, it.id) }
			}
		}.stateIn(scope, SharingStarted.Eagerly, emptyList())

	private val _allSubjects = MutableStateFlow<Map<Long, SubjectEntity>>(emptyMap())
	private val _customSubjects = MutableStateFlow<Map<Long, CustomSubjectEntity>>(emptyMap())
	override val allSubjects: StateFlow<List<PeriodElement>> =
		combine(_allSubjects, _customSubjects) { untisMap, customMap ->
			if (user?.let { it.apiHost == "custom" || it.isCustom } == true) {
				customMap.values.sortedBy { it.name }.map { PeriodElement(ElementType.SUBJECT, it.id) }
			} else {
				untisMap.values.map { PeriodElement(ElementType.SUBJECT, it.id) }
			}
		}.stateIn(scope, SharingStarted.Eagerly, emptyList())

	private val _allRooms = MutableStateFlow<Map<Long, RoomEntity>>(emptyMap())
	private val _customRooms = MutableStateFlow<Map<Long, CustomRoomEntity>>(emptyMap())
	override val allRooms: StateFlow<List<PeriodElement>> =
		combine(_allRooms, _customRooms) { untisMap, customMap ->
			if (user?.let { it.apiHost == "custom" || it.isCustom } == true) {
				customMap.values.sortedBy { it.name }.map { PeriodElement(ElementType.ROOM, it.id) }
			} else {
				untisMap.values.map { PeriodElement(ElementType.ROOM, it.id) }
			}
		}.stateIn(scope, SharingStarted.Eagerly, emptyList())

	private val _customSubjectNames = MutableStateFlow<Map<String, CustomSubjectNameEntity>>(emptyMap())
	private val _customWeekdayNames = MutableStateFlow<Map<Int, CustomWeekdayNameEntity>>(emptyMap())

	init {
		scope.launch {
			userRepository.userState.collectLatest { userState ->
				(userState as? UserRepository.UserState.User)?.user?.let { user ->
					scope.launch {
						customNamesRepository.getSubjectNamesFlow(user.id).collect { _customSubjectNames.value = it }
					}
					scope.launch {
						customNamesRepository.getWeekdayNamesFlow(user.id).collect { _customWeekdayNames.value = it }
					}

					if (user.apiHost == "custom" || user.isCustom) {
						_userData.value = null
						_allClasses.value = emptyMap()
						_allTeachers.value = emptyMap()
						_allSubjects.value = emptyMap()
						_allRooms.value = emptyMap()
						scope.launch { userDao.getCustomClassesFlow(user.id).collect { _customClasses.value = it.associateBy { c -> c.id } } }
						scope.launch { userDao.getCustomTeachersFlow(user.id).collect { _customTeachers.value = it.associateBy { t -> t.id } } }
						scope.launch { userDao.getCustomRoomsFlow(user.id).collect { _customRooms.value = it.associateBy { r -> r.id } } }
						scope.launch { userDao.getCustomSubjectsFlow(user.id).collect { _customSubjects.value = it.associateBy { s -> s.id } } }
					} else {
						_customClasses.value = emptyMap()
						_customTeachers.value = emptyMap()
						_customRooms.value = emptyMap()
						_customSubjects.value = emptyMap()
						userDao.getByIdWithDataFlow(user.id).collectLatest { userData ->
							_userData.value = userData
							_allClasses.value = userData?.let { prepareElements(it.klassen) } ?: emptyMap()
							_allTeachers.value = userData?.let { prepareElements(it.teachers) } ?: emptyMap()
							_allSubjects.value = userData?.let { prepareElements(it.subjects) } ?: emptyMap()
							_allRooms.value = userData?.let { prepareElements(it.rooms) } ?: emptyMap()
						}
					}
				} ?: run {
					_userData.value = null
					_allClasses.value = emptyMap()
					_allTeachers.value = emptyMap()
					_allSubjects.value = emptyMap()
					_allRooms.value = emptyMap()
					_customClasses.value = emptyMap()
					_customTeachers.value = emptyMap()
					_customRooms.value = emptyMap()
					_customSubjects.value = emptyMap()
					_customSubjectNames.value = emptyMap()
					_customWeekdayNames.value = emptyMap()
				}
			}
		}
	}

	private fun <T : ElementEntity> prepareElements(elements: List<T>) =
		elements.filter { it.active }.sortedBy { it.name }.associateBy { it.id }

	override fun getShortName(id: Long, type: ElementType?, default: String): String {
		val isCustomUser = user?.let { it.apiHost == "custom" || it.isCustom } == true
		val baseName = if (isCustomUser) {
			when (type) {
				ElementType.CLASS -> _customClasses.value[id]?.name
				ElementType.TEACHER -> _customTeachers.value[id]?.run { shortName.ifEmpty { name } }
				ElementType.SUBJECT -> _customSubjects.value[id]?.run { shortName.ifEmpty { name } }
				ElementType.ROOM -> _customRooms.value[id]?.name
				else -> null
			} ?: default
		} else {
			when (type) {
				ElementType.CLASS -> _allClasses.value[id]?.name
				ElementType.TEACHER -> _allTeachers.value[id]?.name
				ElementType.SUBJECT -> _allSubjects.value[id]?.name
				ElementType.ROOM -> _allRooms.value[id]?.name
				else -> null
			} ?: default
		}

		if (type == ElementType.SUBJECT) {
			return CustomNamesRepository.resolveSubjectShortName(_customSubjectNames.value, id.toString(), baseName)
		}
		return baseName
	}

	override fun getShortName(periodElement: PeriodElement, default: String) =
		getShortName(periodElement.id, periodElement.type, default)

	override fun getLongName(id: Long, type: ElementType, default: String): String {
		val isCustomUser = user?.let { it.apiHost == "custom" || it.isCustom } == true
		val baseName = if (isCustomUser) {
			when (type) {
				ElementType.CLASS -> _customClasses.value[id]?.name
				ElementType.TEACHER -> _customTeachers.value[id]?.name
				ElementType.SUBJECT -> _customSubjects.value[id]?.name
				ElementType.ROOM -> _customRooms.value[id]?.name
				else -> null
			} ?: default
		} else {
			when (type) {
				ElementType.CLASS -> _allClasses.value[id]?.longName
				ElementType.TEACHER -> _allTeachers.value[id]?.run { "$firstName $lastName" }
				ElementType.SUBJECT -> _allSubjects.value[id]?.longName
				ElementType.ROOM -> _allRooms.value[id]?.longName
				else -> null
			} ?: default
		}

		if (type == ElementType.SUBJECT) {
			return CustomNamesRepository.resolveSubjectLongName(_customSubjectNames.value, id.toString(), baseName)
		}
		return baseName
	}

	override fun getLongName(periodElement: PeriodElement, default: String) =
		getLongName(periodElement.id, periodElement.type, default)

	override fun getWeekdayDisplayName(dayOfWeek: DayOfWeek): String {
		return CustomNamesRepository.resolveWeekdayName(_customWeekdayNames.value, dayOfWeek)
	}

	override fun getWeekdayDisplayShortName(dayOfWeek: DayOfWeek): String {
		return CustomNamesRepository.resolveWeekdayShortName(_customWeekdayNames.value, dayOfWeek)
	}

	override fun isAllowed(id: Long, type: ElementType?): Boolean {
		val isCustomUser = user?.let { it.apiHost == "custom" || it.isCustom } == true
		if (isCustomUser) {
			return when (type) {
				ElementType.CLASS -> _customClasses.value.containsKey(id)
				ElementType.TEACHER -> _customTeachers.value.containsKey(id)
				ElementType.SUBJECT -> _customSubjects.value.containsKey(id)
				ElementType.ROOM -> _customRooms.value.containsKey(id)
				else -> false
			}
		}
		return when (type) {
			ElementType.CLASS -> _allClasses.value[id]?.displayable
			ElementType.TEACHER -> _allTeachers.value[id]?.displayAllowed
			ElementType.SUBJECT -> _allSubjects.value[id]?.displayAllowed
			ElementType.ROOM -> _allRooms.value[id]?.displayAllowed
			else -> null
		} ?: false
	}

	override fun isAllowed(periodElement: PeriodElement) = isAllowed(periodElement.id, periodElement.type)
}

val LocalMasterDataRepository = compositionLocalOf<MasterDataRepository> { DefaultMasterDataRepository() }
