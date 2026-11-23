package com.rxnzolaf.untis.screenshots

import androidx.activity.compose.setContent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.hilt.navigation.compose.hiltViewModel
import com.rxnzolaf.untis.activities.MainActivity
import com.rxnzolaf.untis.ui.pages.roomfinder.RoomFinder
import com.rxnzolaf.untis.ui.pages.roomfinder.RoomFinderViewModel
import com.rxnzolaf.untis.utils.WithScreenshot
import com.rxnzolaf.untis.utils.takeScreenshot
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class RoomFinderScreenshot {
	@get:Rule
	var rule = createAndroidComposeRule<MainActivity>()

	@get:Rule
	var hiltRule = HiltAndroidRule(this)

	@Before
	fun init() {
		hiltRule.inject()
	}

	@Test
	fun roomFinderScreenshot() {
		rule.activity.setContent {
			WithScreenshot {
				val viewModel: RoomFinderViewModel = hiltViewModel()
				viewModel.selectHour(2)
				RoomFinder(viewModel)
			}
		}
		rule.takeScreenshot("roomfinder.png")
	}
}
