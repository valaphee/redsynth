/*
 * Copyright (c) 2022, Valaphee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.valaphee.redsynth.redstone

enum class CellType(
    val power: List<Int?>
) {
    None(
        listOf<Int?>(
                N,
            N, N, N,
              N,
                N,
            N, N, N,
              N,
                N,
            N, N, N,
              N
        )
    ),
    Block(
        listOf<Int?>(
                N,
            N, N, N,
              N,
                N,
            N, N, N,
              N,
                N,
            N, N, N,
              N
        )
    ),
    PoweredBlock(
        listOf(
                N,
            N, L, N,
              N,
                L,
            L, H, L,
              L,
                N,
            N, L, N,
              N
        )
    ),
    RedstoneWire(
        listOf(
                N,
            N, N, N,
              N,
                I,
            I, H, I,
              I,
                N,
            N, L, N,
              N
        )
    ),
    RedstoneRepeater(
        listOf(
                N,
            N, N, N,
              N,
                N,
            X, N, H,
              N,
                N,
            N, N, N,
              N
        )
    ),
    RedstoneTorch(
        listOf(
                N,
            N, H, N,
              N,
                L,
            L, H, L,
              L,
                N,
            N, X, N,
              N
        )
    ),
    RedstoneWallTorch(
        listOf(
                N,
            N, H, N,
              N,
                L,
            X, H, L,
              L,
                N,
            N, H, N,
              N
        )
    );
}

val N = null
const val X = -1
const val I = 0
const val L = 1
const val H = 2
