package dev.mslalith.focuslauncher.core.domain.iconpack

import dev.mslalith.focuslauncher.core.launcherapps.manager.iconpack.IconPackManager
import javax.inject.Inject

class ReloadIconPackUseCase @Inject constructor(
    private val iconPackManager: IconPackManager
) {
    suspend operator fun invoke() = iconPackManager.reloadIconPack()
}
