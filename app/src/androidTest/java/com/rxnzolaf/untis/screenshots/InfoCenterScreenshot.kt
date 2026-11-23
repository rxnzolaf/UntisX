package com.rxnzolaf.untis.screenshots

import androidx.activity.compose.setContent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.rxnzolaf.untis.activities.MainActivity
import com.rxnzolaf.untis.ui.pages.infocenter.InfoCenter
import com.rxnzolaf.untis.utils.WithScreenshot
import com.rxnzolaf.untis.utils.takeScreenshot
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@HiltAndroidTest
class InfoCenterScreenshot {
	@get:Rule
	var rule = createAndroidComposeRule<MainActivity>()

	@get:Rule
	var hiltRule = HiltAndroidRule(this)

	@Before
	fun init() {
		hiltRule.inject()
	}

	@Test
	fun infoCenterScreenshot() {
		rule.activity.setContent {
			WithScreenshot {
				InfoCenter()
			}
		}
		rule.takeScreenshot("infocenter.png")
	}
}
