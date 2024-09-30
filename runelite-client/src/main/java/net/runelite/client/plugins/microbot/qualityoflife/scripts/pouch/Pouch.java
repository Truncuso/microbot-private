<<<<<<< HEAD
/*
 * Copyright (c) 2019 Hydrox6 <ikada@protonmail.ch>
 * Copyright (c) 2019 Adam <Adam@sigterm.info>
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
=======
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
package net.runelite.client.plugins.microbot.qualityoflife.scripts.pouch;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.ItemID;
<<<<<<< HEAD

enum Pouch
{
	SMALL(3),
	MEDIUM(6, 3),
	LARGE(9, 7),
	GIANT(12, 9);
=======
import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import java.util.Arrays;

public enum Pouch
{
	SMALL(new int[] {ItemID.SMALL_POUCH}, 3, 3, 1),
	MEDIUM(new int[] { ItemID.MEDIUM_POUCH, ItemID.MEDIUM_POUCH_5511}, 6, 3, 25),
	LARGE(new int[] {ItemID.LARGE_POUCH, ItemID.LARGE_POUCH_5513}, 9, 7, 50),
	GIANT(new int[] {ItemID.GIANT_POUCH, ItemID.GIANT_POUCH_5515}, 12, 9, 75);
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6

	private final int baseHoldAmount;
	private final int degradedBaseHoldAmount;

	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
<<<<<<< HEAD
	private int holding;
	@Getter(AccessLevel.PACKAGE)
	private boolean degraded;
	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private boolean unknown = true;

	Pouch(int holdAmount)
	{
		this(holdAmount, -1);
	}

	Pouch(int holdAmount, int degradedHoldAmount)
	{
		this.baseHoldAmount = holdAmount;
		this.degradedBaseHoldAmount = degradedHoldAmount;
	}

	int getHoldAmount()
=======
	private int[] itemIds;
	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PACKAGE)
	private int holding;
	@Getter(AccessLevel.PACKAGE)
	private boolean degraded;
	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PACKAGE)
	private boolean unknown = true;
	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private int levelRequired;


	Pouch(int[] itemIds, int holdAmount, int degradedHoldAmount, int levelRequired)
	{
		this.itemIds = itemIds;
		this.baseHoldAmount = holdAmount;
		this.degradedBaseHoldAmount = degradedHoldAmount;
		this.levelRequired = levelRequired;
	}

	public int getHoldAmount()
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
	{
		return degraded ? degradedBaseHoldAmount : baseHoldAmount;
	}

<<<<<<< HEAD
	int getRemaining()
=======
	public int getRemaining()
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
	{
		final int holdAmount = degraded ? degradedBaseHoldAmount : baseHoldAmount;
		return holdAmount - holding;
	}

	void addHolding(int delta)
	{
		holding += delta;

		final int holdAmount = degraded ? degradedBaseHoldAmount : baseHoldAmount;
		if (holding < 0)
		{
			holding = 0;
		}
		if (holding > holdAmount)
		{
			holding = holdAmount;
		}
	}

	void degrade(boolean state)
	{
		if (state != degraded)
		{
			degraded = state;
			final int holdAmount = degraded ? degradedBaseHoldAmount : baseHoldAmount;
			holding = Math.min(holding, holdAmount);
		}
	}

	static Pouch forItem(int itemId)
	{
		switch (itemId)
		{
			case ItemID.SMALL_POUCH:
				return SMALL;
			case ItemID.MEDIUM_POUCH:
			case ItemID.MEDIUM_POUCH_5511:
				return MEDIUM;
			case ItemID.LARGE_POUCH:
			case ItemID.LARGE_POUCH_5513:
				return LARGE;
			case ItemID.GIANT_POUCH:
			case ItemID.GIANT_POUCH_5515:
				return GIANT;
			default:
				return null;
		}
	}
<<<<<<< HEAD
=======

	public boolean fill() {
		if (!hasRequiredRunecraftingLevel()) return false;
		if (!hasItemsToFillPouch()) return false;
		if (!hasPouchInInventory()) return false;

		if (getRemaining() > 0) {
			for (int i = 0; i < itemIds.length; i++) {
				if (Rs2Inventory.interact(itemIds[i], "fill"))
					return true;
			}
		}

		return false;
	}

	public boolean empty() {
		if (!hasRequiredRunecraftingLevel()) return false;
		if (!hasPouchInInventory()) return false;

		if (getHolding() > 0)  {
			for (int i = 0; i < itemIds.length; i++) {
				if (Rs2Inventory.interact(itemIds[i], "empty"))
					return true;
			}
		}

		return false;
	}

	public boolean check() {
		if (!hasRequiredRunecraftingLevel()) return false;

		for (int i = 0; i < itemIds.length; i++) {
			if (Rs2Inventory.interact(itemIds[i], "check"))
				return true;
		}

		return false;
	}

	public boolean hasRequiredRunecraftingLevel() {
		return Rs2Player.getSkillRequirement(Skill.RUNECRAFT, getLevelRequired());
	}

	public boolean hasItemsToFillPouch() {
		return Rs2Inventory.hasItem(ItemID.PURE_ESSENCE) || Rs2Inventory.hasItem(ItemID.DAEYALT_ESSENCE) || Rs2Inventory.hasItem(ItemID.GUARDIAN_ESSENCE);
	}

	public boolean hasPouchInInventory() {
		return Rs2Inventory.hasItem(itemIds);
	}

	public boolean isDegraded() {
		return degraded;
	}
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
}