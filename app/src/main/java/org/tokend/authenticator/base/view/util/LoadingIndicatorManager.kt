package org.tokend.authenticator.base.view.util

class LoadingIndicatorManager(private val showLoading: () -> Unit,
                              private val hideLoading: () -> Unit) {
    private val requests = mutableSetOf<String>()

    var isLoading: Boolean = false
        private set

    fun setLoading(isLoading: Boolean, tag: String = "main") {
        if (isLoading) {
            show(tag)
        } else {
            hide(tag)
        }
    }

    fun show(tag: String = "main") {
        requests.add(tag)
        updateVisibility()
    }

    fun hide(tag: String = "main") {
        requests.remove(tag)
        updateVisibility()
    }

    private fun updateVisibility() {
        isLoading = if (requests.size > 0) {
            if (!isLoading) {
                showLoading()
            }
            true
        } else {
            if (isLoading) {
                hideLoading()
            }
            false
        }
    }
}