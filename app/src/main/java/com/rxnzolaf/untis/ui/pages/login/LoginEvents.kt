package com.rxnzolaf.untis.ui.pages.login

sealed class LoginEvents {
	data object ClearFocus : LoginEvents()
}
