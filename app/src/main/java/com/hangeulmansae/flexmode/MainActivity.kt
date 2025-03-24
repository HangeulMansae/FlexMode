package com.hangeulmansae.flexmode

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.hangeulmansae.flexmode.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val windowInfoTracker = WindowInfoTracker.getOrCreate(this)

        lifecycleScope.launch {
            // WindowLayoutInfo에 대한 정보를 수집
            windowInfoTracker.windowLayoutInfo(this@MainActivity)
                .collect { newLayoutInfo ->
                    /**
                     * 폴더블 디바이스에 존재하는 정보에 대한 것을 가져옴
                     * 만약 폴더블이 아니라서 없을 경우에는 Null을 반환하게 함
                      */
                    val foldingFeature =
                        newLayoutInfo.displayFeatures.filterIsInstance<FoldingFeature>()
                            .firstOrNull()
                    /**
                     * 폴더블이 맞을 경우에는 관련 상태에 따라 작업을 진행하도록 함
                     */
                    foldingFeature?.let {
                        /**
                         * 만약 접힌 상태라면
                         */
                        if (it.state == FoldingFeature.State.HALF_OPENED) {
                            /**
                             * 힌지의 영역을 알아냄
                             */
                            val hingeBounds = it.bounds

                            /**
                             * 만약 힌지가 세로로 있는 거라면 => 폴드라면
                             */
                            val isVerticalHinge = hingeBounds.height() >= hingeBounds.width()
                            if (isVerticalHinge) {
                                ConstraintSet().apply {
                                    clone(binding.main)
                                    this.setHorizontalWeight(binding.blue.id, 0.5F)
                                    this.setHorizontalWeight(binding.green.id, 0.5F)
                                    applyTo(binding.main)
                                }
                            }
                            /**
                             * 만약 힌지가 가로로 있는 거라면 => 플립이라면
                             */
                            else{
                                // 플립을 현재 테스트 할 기기가 없는 관계로...
                            }
                        }
                        /**
                         * 만약 아니라면 => 다시 폴드의 다 접었을 떄 쓰는 화면 or 펼쳐진 상태라면 원래대로 복구
                         */
                        else {
                            val hingeBounds = it.bounds
                            val isVerticalHinge = hingeBounds.height() >= hingeBounds.width()
                            if (isVerticalHinge) {
                                ConstraintSet().apply {
                                    clone(binding.main)
                                    this.setHorizontalWeight(binding.blue.id, 0.8F)
                                    this.setHorizontalWeight(binding.green.id, 0.2F)
                                    applyTo(binding.main)
                                }
                            }
                        }
                    }
                }
        }
    }
}