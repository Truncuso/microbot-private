/*
<<<<<<<< HEAD:runelite-client/src/main/java/net/runelite/client/plugins/loottracker/NpcMetadata.java
 * Copyright (c) 2023, Adam <Adam@sigterm.info>
========
 * Copyright (c) 2021, Adam <Adam@sigterm.info>
>>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6:runelite-client/src/main/java/net/runelite/client/plugins/gpu/GLBuffer.java
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
<<<<<<<< HEAD:runelite-client/src/main/java/net/runelite/client/plugins/loottracker/NpcMetadata.java
package net.runelite.client.plugins.loottracker;

import lombok.Data;

@Data
class NpcMetadata
{
	int id;
	int r1;
	int r2;
	int r3;
	int r4;
	int r5;
	int r6;
	int r7;
	int r8;
========
package net.runelite.client.plugins.gpu;

class GLBuffer
{
	String name;
	int glBufferId = -1;
	int size = -1;
	long clBuffer = -1;

	GLBuffer(String name)
	{
		this.name = name;
	}
>>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6:runelite-client/src/main/java/net/runelite/client/plugins/gpu/GLBuffer.java
}
