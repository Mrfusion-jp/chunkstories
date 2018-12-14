package io.xol.chunkstories.graphics.vulkan.systems.world

import io.xol.chunkstories.api.voxel.Voxel
import io.xol.chunkstories.api.voxel.VoxelFormat
import io.xol.chunkstories.api.voxel.VoxelSide
import io.xol.chunkstories.api.workers.Task
import io.xol.chunkstories.api.workers.TaskExecutor
import io.xol.chunkstories.api.world.chunk.ChunkHolder
import io.xol.chunkstories.graphics.common.UnitCube
import io.xol.chunkstories.graphics.vulkan.VulkanGraphicsBackend
import io.xol.chunkstories.graphics.vulkan.buffers.VulkanVertexBuffer
import io.xol.chunkstories.graphics.vulkan.textures.VirtualTexturing
import io.xol.chunkstories.world.chunk.CubicChunk
import io.xol.chunkstories.world.chunk.deriveddata.AutoRebuildingProperty
import org.joml.Vector4f
import org.lwjgl.system.MemoryUtil
import java.util.*

class TaskCreateChunkMesh(val backend: VulkanGraphicsBackend, val chunk: CubicChunk, attachedProperty: AutoRebuildingProperty, updates: Int) : AutoRebuildingProperty.UpdateTask(attachedProperty, updates) {
    lateinit var rawChunkData: IntArray

    inline fun opaque(voxel: Voxel) = voxel.solid || voxel.name == "water"

    inline fun opaque(data: Int) = if(data == 0) false else opaque(chunk.world.contentTranslator.getVoxelForId(VoxelFormat.id(data))!!)

    inline fun opaque(x2: Int, y2: Int, z2: Int): Boolean = opaque(data(x2, y2, z2))

    inline fun data(x2: Int, y2: Int, z2: Int): Int =
            if (x2 in 0..31 && y2 in 0..31 && z2 in 0..31)
                rawChunkData[x2 * 32 * 32 + y2 * 32 + z2]
            else
                chunk.world.peekRaw(x2 + chunk.chunkX * 32, y2 + chunk.chunkY * 32, z2 + chunk.chunkZ * 32)

    override fun update(taskExecutor: TaskExecutor): Boolean {
        if (chunk.holder().state !is ChunkHolder.State.Available)
            return true

        val neighborsPresent = neighborsIndexes.count { (x, y, z) ->
            val neighbor = chunk.world.getChunk(chunk.chunkX + x, chunk.chunkY + y, chunk.chunkZ + z)
            (neighbor != null || (chunk.chunkY + y < 0) || (chunk.chunkY + y >= chunk.world.worldInfo.size.heightInChunks))
        }
        if (neighborsPresent < neighborsIndexes.size)
            return true

        val rng = Random(1)
        var count = 0
        val vertexBuffer: VulkanVertexBuffer?
        val virtualTexturingContext: VirtualTexturing.VirtualTexturingContext?

        val chunkDataRef = chunk.voxelDataArray
        if (chunk.isAirChunk || chunkDataRef == null) {
            vertexBuffer = null
            virtualTexturingContext = null
        } else {
            rawChunkData = chunkDataRef
            virtualTexturingContext = backend.virtualTexturing.getVirtualTexturingContext()

            val buffer = MemoryUtil.memAlloc(1024 * 1024 * 4)

            val color = Vector4f()
            for (x in 0..31) {
                for (y in 0..31) {
                    for (z in 0..31) {
                        val currentVoxelData = rawChunkData[x * 32 * 32 + y * 32 + z]
                        val currentVoxel = chunk.world.contentTranslator.getVoxelForId(VoxelFormat.id(currentVoxelData))!!

                        if (opaque(currentVoxel)) {
                            fun face(data: Int, face: UnitCube.CubeFaceData, side: VoxelSide) {
                                if(opaque(data))
                                    return

                                val tex = currentVoxel.voxelTextures[side.ordinal]
                                if(tex.color != null)
                                    color.set(tex.color)

                                val textureName = "voxels/textures/"+tex.name+".png"
                                //val textureName = "voxels/textures/"+(if(Math.random() > 0.5) "stone" else "dirt")+".png"
                                //println(textureName)
                                val textureId = (virtualTexturingContext.translate(backend.textures[textureName]) as VirtualTexturing.TranslationResult.Success).id
                                //val textureId = (virtualTexturingContext.translate(backend.textures.defaultTexture2D) as VirtualTexturing.TranslationResult.Success).id
                                //val textureId = 0

                                if (color.w < 1.0f)
                                    color.mul(Vector4f(0f, 1f, 0.3f, 1.0f))

                                val sunlight = VoxelFormat.sunlight(data) / 15f
                                color.mul(sunlight * 0.9f + rng.nextFloat() * 0.1f)

                                for((vertex, texcoord) in face.vertices) {
                                    buffer.putFloat(vertex[0] + x + chunk.chunkX * 32f)
                                    buffer.putFloat(vertex[1] + y + chunk.chunkY * 32f)
                                    buffer.putFloat(vertex[2] + z + chunk.chunkZ * 32f)

                                    buffer.putFloat(color.x())
                                    buffer.putFloat(color.y())
                                    buffer.putFloat(color.z())

                                    buffer.putFloat(face.normalDirection.x())
                                    buffer.putFloat(face.normalDirection.y())
                                    buffer.putFloat(face.normalDirection.z())

                                    buffer.putFloat(texcoord[0])
                                    buffer.putFloat(texcoord[1])

                                    buffer.putInt(textureId)
                                    count++
                                }
                            }

                            face(data(x, y - 1, z), UnitCube.bottomFace, VoxelSide.BOTTOM)
                            face(data(x, y + 1, z), UnitCube.topFace, VoxelSide.TOP)

                            face(data(x - 1, y, z), UnitCube.leftFace, VoxelSide.LEFT)
                            face(data(x + 1, y, z), UnitCube.rightFace, VoxelSide.RIGHT)

                            face(data(x, y, z - 1), UnitCube.backFace, VoxelSide.BACK)
                            face(data(x, y, z + 1), UnitCube.frontFace, VoxelSide.FRONT)
                        }
                    }
                }
            }

            buffer.flip()

            if (buffer.remaining() > 0) {
                vertexBuffer = VulkanVertexBuffer(backend, buffer.limit().toLong())
                vertexBuffer.upload(buffer)
            } else
                vertexBuffer = null

            MemoryUtil.memFree(buffer)

            virtualTexturingContext.updateContents()
        }

        //val generatedData = VulkanChunkRenderData.ChunkMeshInstance(chunk, vertexBuffer, count)
        //(chunk.meshData as VulkanChunkRenderData).acceptNewData(generatedData)
        (chunk.meshData as ChunkVkMeshProperty).acceptNewData(vertexBuffer, virtualTexturingContext, count)
        return true
    }

    companion object {
        val neighborsIndexes = generateNeighbors()

        fun generateNeighbors(): List<Triple<Int, Int, Int>> {
            val list = mutableListOf<Triple<Int, Int, Int>>()

            for (x in -1..1)
                for (y in -1..1)
                    for (z in -1..1)
                        list += Triple(x, y, z)

            return list.filterNot { (x, y, z) -> (x == 0 && y == 0 && z == 0) }
        }
    }
}