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

package com.valaphee.redsynth.util

class BoundingBox {
    var minimumX = 0
        private set
    var minimumY = 0
        private set
    var minimumZ = 0
        private set
    var maximumX = 0
        private set
    var maximumY = 0
        private set
    var maximumZ = 0
        private set
    var isEmpty = true
        private set

    constructor()

    constructor(minimumX: Int, minimumY: Int, minimumZ: Int, maximumX: Int, maximumY: Int, maximumZ: Int) {
        set(minimumX, minimumY, minimumZ, maximumX, maximumY, maximumZ)
    }

    fun set(minimumX: Int, minimumY: Int, minimumZ: Int, maximumX: Int, maximumY: Int, maximumZ: Int) {
        this.minimumX = minimumX
        this.minimumY = minimumY
        this.minimumZ = minimumZ
        this.maximumX = maximumX
        this.maximumY = maximumY
        this.maximumZ = maximumZ
        isEmpty = false
    }

    fun add(x: Int, y: Int, z: Int) {
        if (isEmpty) {
            this.minimumX = x
            this.minimumY = y
            this.minimumZ = z
            this.maximumX = x
            this.maximumY = y
            this.maximumZ = z
            isEmpty = false
        } else {
            if (x < minimumX) minimumX = x
            if (y < minimumY) minimumY = y
            if (z < minimumZ) minimumZ = z
            if (x > maximumX) maximumX = x
            if (y > maximumY) maximumY = y
            if (z > maximumZ) maximumZ = z
        }
    }

    fun intersects(boundingBox: BoundingBox) = minimumX <= boundingBox.maximumX && maximumX >= boundingBox.minimumX && minimumY <= boundingBox.maximumY && maximumY >= boundingBox.minimumY && minimumZ <= boundingBox.maximumZ && maximumZ >= boundingBox.minimumZ
}
