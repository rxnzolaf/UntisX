package com.rxnzolaf.untis.modules

import com.rxnzolaf.untis.data.repository.InfoCenterRepository
import com.rxnzolaf.untis.data.repository.MasterDataRepository
import com.rxnzolaf.untis.data.repository.MessagesRepository
import com.rxnzolaf.untis.data.repository.TimetableRepository
import com.rxnzolaf.untis.data.repository.UntisInfoCenterRepository
import com.rxnzolaf.untis.data.repository.UntisMasterDataRepository
import com.rxnzolaf.untis.data.repository.UntisMessagesRepository
import com.rxnzolaf.untis.data.repository.UntisTimetableRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
	@Binds
	fun bindMasterDataRepository(
		implementation: UntisMasterDataRepository
	): MasterDataRepository

	@Binds
	fun bindTimetableRepository(
		implementation: UntisTimetableRepository
	): TimetableRepository

	@Binds
	fun bindInfoCenterRepository(
		implementation: UntisInfoCenterRepository
	): InfoCenterRepository

	@Binds
	fun bindMessagesRepository(
		implementation: UntisMessagesRepository
	): MessagesRepository
}
