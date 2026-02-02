package com.tikkatimer.domain.usecase.timer

import com.tikkatimer.domain.repository.TimerRepository
import javax.inject.Inject

/**
 * 타이머 프리셋 삭제 UseCase
 */
class DeleteTimerPresetUseCase
    @Inject
    constructor(
        private val repository: TimerRepository,
    ) {
        /**
         * 프리셋 삭제
         */
        suspend operator fun invoke(presetId: Long) {
            repository.deletePreset(presetId)
        }
    }
