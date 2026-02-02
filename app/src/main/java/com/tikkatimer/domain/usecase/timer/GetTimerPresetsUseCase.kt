package com.tikkatimer.domain.usecase.timer

import com.tikkatimer.domain.model.TimerPreset
import com.tikkatimer.domain.repository.TimerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 타이머 프리셋 목록 조회 UseCase
 */
class GetTimerPresetsUseCase
    @Inject
    constructor(
        private val repository: TimerRepository,
    ) {
        /**
         * 모든 프리셋을 사용 빈도순으로 조회
         */
        operator fun invoke(): Flow<List<TimerPreset>> {
            return repository.getAllPresets()
        }
    }
