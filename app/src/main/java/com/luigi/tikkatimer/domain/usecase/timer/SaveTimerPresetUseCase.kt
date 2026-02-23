package com.luigi.tikkatimer.domain.usecase.timer

import com.luigi.tikkatimer.domain.model.TimerPreset
import com.luigi.tikkatimer.domain.repository.TimerRepository
import javax.inject.Inject

/**
 * 타이머 프리셋 저장 UseCase
 */
class SaveTimerPresetUseCase
    @Inject
    constructor(
        private val repository: TimerRepository,
    ) {
        /**
         * 새 프리셋 저장
         * @return 생성된 프리셋의 ID
         */
        suspend operator fun invoke(preset: TimerPreset): Long {
            return repository.addPreset(preset)
        }
    }
