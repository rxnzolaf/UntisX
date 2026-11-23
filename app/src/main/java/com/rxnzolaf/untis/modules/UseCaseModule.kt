package com.rxnzolaf.untis.modules

import com.rxnzolaf.untis.domain.GetMessagesOfDayUseCase
import com.rxnzolaf.untis.domain.GetMessagesOfDayUseCaseImpl
import com.rxnzolaf.untis.domain.GetMessagesUseCase
import com.rxnzolaf.untis.domain.GetMessagesUseCaseImpl
import com.rxnzolaf.untis.domain.GetRoomFinderItemsUseCase
import com.rxnzolaf.untis.domain.GetRoomFinderItemsUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface UseCaseModule {
	@Binds
	fun bindGetMessagesOfDayUseCase(
		implementation: GetMessagesOfDayUseCaseImpl
	): GetMessagesOfDayUseCase

	@Binds
	fun bindGetMessagesUseCase(
		implementation: GetMessagesUseCaseImpl
	): GetMessagesUseCase

	@Binds
	fun bindGetRoomFinderItemsUseCase(
		implementation: GetRoomFinderItemsUseCaseImpl
	): GetRoomFinderItemsUseCase
}
