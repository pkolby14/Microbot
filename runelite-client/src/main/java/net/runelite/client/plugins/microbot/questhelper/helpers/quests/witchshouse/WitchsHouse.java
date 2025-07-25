/*
 * Copyright (c) 2020, Zoinkwiz <https://github.com/Zoinkwiz>
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
package net.runelite.client.plugins.microbot.questhelper.helpers.quests.witchshouse;

import net.runelite.client.plugins.microbot.questhelper.bank.banktab.BankSlotIcons;
import net.runelite.client.plugins.microbot.questhelper.panel.PanelDetails;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.BasicQuestHelper;
import net.runelite.client.plugins.microbot.questhelper.requirements.Requirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.Conditions;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.NpcCondition;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.ObjectCondition;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.player.FreeInventorySlotRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.util.LogicType;
import net.runelite.client.plugins.microbot.questhelper.requirements.var.VarplayerRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.Zone;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.ZoneRequirement;
import net.runelite.client.plugins.microbot.questhelper.rewards.ExperienceReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.QuestPointReward;
import net.runelite.client.plugins.microbot.questhelper.steps.*;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;

import java.util.*;

public class WitchsHouse extends BasicQuestHelper
{
	//Items Required
	ItemRequirement cheese, leatherGloves, houseKey, magnet, shedKey, ball;

	//Items Recommended
	ItemRequirement armourAndWeapon;

	Requirement inHouse, inUpstairsHouse, inDownstairsHouseWest, inDownstairsHouseEast, inDownstairsHouse, inHouseOrGarden,
		ratHasMagnet, inShed, experimentNearby;

	QuestStep talkToBoy, getKey, goDownstairs, enterGate, goDownstairsFromTop, openCupboardAndLoot, openCupboardAndLoot2, goBackUpstairs, useCheeseOnHole,
		enterHouse, searchFountain, enterShed, enterShedWithoutKey, killWitchsExperiment, returnToBoy, pickupBall, grabBall;

	//Zones
	Zone house, upstairsHouse, downstairsHouseEast, downstairsHouseWest, garden1, garden2, garden3, shed;

	@Override
	public Map<Integer, QuestStep> loadSteps()
	{
		initializeRequirements();
		setupConditions();
		setupSteps();
		Map<Integer, QuestStep> steps = new HashMap<>();

		steps.put(0, talkToBoy);

		ConditionalStep getTheMagnet = new ConditionalStep(this, getKey);
		getTheMagnet.addStep(new Conditions(inHouse, magnet), useCheeseOnHole);
		getTheMagnet.addStep(new Conditions(inDownstairsHouse, magnet), goBackUpstairs);
		getTheMagnet.addStep(new Conditions(inDownstairsHouseWest, new ObjectCondition(ObjectID.MAGNETCBOPEN)), openCupboardAndLoot2);
		getTheMagnet.addStep(inDownstairsHouseWest, openCupboardAndLoot);
		getTheMagnet.addStep(inDownstairsHouseEast, enterGate);
		getTheMagnet.addStep(inHouse, goDownstairs);
		getTheMagnet.addStep(inUpstairsHouse, goDownstairsFromTop);
		getTheMagnet.addStep(houseKey.alsoCheckBank(questBank), enterHouse);

		steps.put(1, getTheMagnet);
		steps.put(2, getTheMagnet);

		ConditionalStep killExperiment = new ConditionalStep(this, getKey);
		killExperiment.addStep(new Conditions(inShed, experimentNearby), killWitchsExperiment);
		killExperiment.addStep(inShed, grabBall);
		killExperiment.addStep(new Conditions(ratHasMagnet, inHouseOrGarden, shedKey), enterShed);
		killExperiment.addStep(new Conditions(ratHasMagnet, inHouseOrGarden), searchFountain);
		killExperiment.addStep(new Conditions(ratHasMagnet, inDownstairsHouse), goBackUpstairs);
		killExperiment.addStep(new Conditions(inHouse, magnet), useCheeseOnHole);
		killExperiment.addStep(new Conditions(inDownstairsHouse, magnet), goBackUpstairs);
		killExperiment.addStep(new Conditions(inDownstairsHouseWest, new ObjectCondition(ObjectID.MAGNETCBOPEN)), openCupboardAndLoot2);
		killExperiment.addStep(inDownstairsHouseWest, openCupboardAndLoot);
		killExperiment.addStep(inDownstairsHouseEast, enterGate);
		killExperiment.addStep(inHouse, goDownstairs);
		killExperiment.addStep(inUpstairsHouse, goDownstairsFromTop);
		killExperiment.addStep(houseKey.alsoCheckBank(questBank), enterHouse);

		steps.put(3, killExperiment);
		// TODO: Add 'pick up diary', 'read diary' after step 3
		steps.put(5, killExperiment);

		ConditionalStep returnBall = new ConditionalStep(this, getKey);
		returnBall.addStep(ball.alsoCheckBank(questBank), returnToBoy);
		returnBall.addStep(inShed, pickupBall);
		returnBall.addStep(inHouseOrGarden, enterShedWithoutKey);
		returnBall.addStep(houseKey.alsoCheckBank(questBank), enterHouse);
		returnBall.addStep(inDownstairsHouse, goBackUpstairs);
		returnBall.addStep(inUpstairsHouse, goDownstairsFromTop);

		steps.put(6, returnBall);
		return steps;
	}

	@Override
	protected void setupRequirements()
	{
		cheese = new ItemRequirement("Cheese (multiple if you mess up)", ItemID.CHEESE);
		leatherGloves = new ItemRequirement("Leather gloves", ItemID.LEATHER_GLOVES, 1, true).isNotConsumed();
		leatherGloves.canBeObtainedDuringQuest();
		houseKey = new ItemRequirement("Door key", ItemID.WITCHES_DOORKEY);
		magnet = new ItemRequirement("Magnet", ItemID.MAGNET);
		shedKey = new ItemRequirement("Key", ItemID.WITCHES_SHEDKEY);
		shedKey.setHighlightInInventory(true);
		ball = new ItemRequirement("Ball", ItemID.BALL);
		armourAndWeapon = new ItemRequirement("Combat gear and food for monsters up to level 53", -1, -1).isNotConsumed();
		armourAndWeapon.setDisplayItemId(BankSlotIcons.getCombatGear());
	}

	@Override
	protected void setupZones()
	{
		house = new Zone(new WorldPoint(2901, 3466, 0), new WorldPoint(2907, 3476, 0));
		upstairsHouse = new Zone(new WorldPoint(2900, 3466, 1), new WorldPoint(2907, 3476, 1));
		downstairsHouseWest = new Zone(new WorldPoint(2897, 9870, 0), new WorldPoint(2902, 9878, 0));
		downstairsHouseEast = new Zone(new WorldPoint(2903, 9870, 0), new WorldPoint(2909, 9878, 0));
		garden1 = new Zone(new WorldPoint(2900, 3459, 0), new WorldPoint(2933, 3465, 0));
		garden2 = new Zone(new WorldPoint(2908, 3466, 0), new WorldPoint(2933, 3467, 0));
		garden3 = new Zone(new WorldPoint(2908, 3467, 0), new WorldPoint(2912, 3475, 0));
		shed = new Zone(new WorldPoint(2934, 3459, 0), new WorldPoint(2937, 3467, 0));
	}

	public void setupConditions()
	{
		inHouse = new ZoneRequirement(house);
		inUpstairsHouse = new ZoneRequirement(upstairsHouse);
		inDownstairsHouseWest = new ZoneRequirement(downstairsHouseWest);
		inDownstairsHouseEast = new ZoneRequirement(downstairsHouseEast);
		inDownstairsHouse = new ZoneRequirement(downstairsHouseEast, downstairsHouseWest);
		inHouseOrGarden = new ZoneRequirement(house, garden1, garden2, garden3);
		ratHasMagnet = new VarplayerRequirement(226, 3);
		inShed = new ZoneRequirement(shed);
		experimentNearby = new Conditions(LogicType.OR,
			new NpcCondition(NpcID.SHAPESHIFTERGLOB),
			new NpcCondition(NpcID.SHAPESHIFTERSPIDER),
			new NpcCondition(NpcID.SHAPESHIFTERBEAR),
			new NpcCondition(NpcID.SHAPESHIFTERWOLF));
	}

	public void setupSteps()
	{
		talkToBoy = new NpcStep(this, NpcID.BALLBOY, new WorldPoint(2928, 3456, 0), "Talk to the Boy in Taverley to start.");
		talkToBoy.addDialogSteps("What's the matter?", "Ok, I'll see what I can do.", "Yes.");
		getKey = new ObjectStep(this, ObjectID.WITCHPOT, new WorldPoint(2900, 3474, 0), "Look under the potted plant just outside the witch's house.");
		enterHouse = new ObjectStep(this, ObjectID.WITCHHOUSEDOOR, new WorldPoint(2900, 3473, 0), "Enter the witch's house.", houseKey);
		goDownstairs = new ObjectStep(this, ObjectID.GRIM_WITCH_LADDER_DOWN, new WorldPoint(2907, 3476, 0), "Go down the ladder to the basement.");
		enterGate = new ObjectStep(this, ObjectID.SHOCKGATER, new WorldPoint(2902, 9873, 0), "Go through the gate " +
			"whilst wearing gloves. Search the nearby boxes if you don't have gloves.", leatherGloves);
		openCupboardAndLoot = new ObjectStep(this, ObjectID.MAGNETCBSHUT, new WorldPoint(2898, 9874, 0), "Open the cupboard and get a magnet from it");
		openCupboardAndLoot2 = new ObjectStep(this, ObjectID.MAGNETCBOPEN, new WorldPoint(2898, 9874, 0), "Open the cupboard and get a magnet from it");
		openCupboardAndLoot.addSubSteps(openCupboardAndLoot2);

		goBackUpstairs = new ObjectStep(this, ObjectID.GRIM_WITCH_LADDER_UP, new WorldPoint(2907, 9876, 0), "Climb back up the ladder.");
		goDownstairsFromTop = new ObjectStep(this, ObjectID.GRIM_WITCH_HOUSE_SPOOKYSTAIRSTOP, new WorldPoint(2907, 3471, 1), "Go back downstairs.");
		useCheeseOnHole = new ObjectStep(this, ObjectID.WITCHMOUSEHOLE, new WorldPoint(2903, 3466, 0), "Use the cheese on the mouse hole in the south room, then use the magnet on the mouse which emerges.", cheese, magnet);
		searchFountain = new ObjectStep(this, ObjectID.WITCHFOUNTAIN, new WorldPoint(2910, 3471, 0), "Enter the garden and sneak around the perimeter to search the fountain. If the witch spots you you'll be teleported outside.");
		enterShed = new ObjectStep(this, ObjectID.WITCHSHEDDOOR, new WorldPoint(2934, 3463, 0), "Use the shed key on the shed door to enter.", shedKey);
		enterShedWithoutKey = new ObjectStep(this, ObjectID.WITCHSHEDDOOR, new WorldPoint(2934, 3463, 0), "Enter the shed.");
		enterShed.addSubSteps(enterShedWithoutKey);
		enterShed.addIcon(ItemID.WITCHES_SHEDKEY);

		grabBall = new DetailedQuestStep(this, new WorldPoint(2936, 3470, 0), "If an experiment hasn't spawned, attempt to pick up the ball once.", ball);
		killWitchsExperiment = new NpcStep(this, NpcID.SHAPESHIFTERGLOB, new WorldPoint(2935, 3463, 0), "Kill all four forms of the Witch's experiment (levels 19, 30, 42, and 53). You can safe spot the last two forms from the crate in the south of the room.");
		((NpcStep) killWitchsExperiment).addAlternateNpcs(NpcID.SHAPESHIFTERSPIDER, NpcID.SHAPESHIFTERBEAR, NpcID.SHAPESHIFTERWOLF);
		killWitchsExperiment.addSubSteps(grabBall);

		pickupBall = new DetailedQuestStep(this, new WorldPoint(2936, 3470, 0), "Pick up the ball.", ball);
		returnToBoy = new NpcStep(this, NpcID.BALLBOY, new WorldPoint(2928, 3456, 0), "Return the ball to the boy. Make sure the witch doesn't spot you or you'll have to get the ball back again..");
	}

	@Override
	public List<ItemRequirement> getItemRequirements()
	{
		ArrayList<ItemRequirement> reqs = new ArrayList<>();
		reqs.add(cheese);
		reqs.add(leatherGloves);
		return reqs;
	}

	@Override
	public List<ItemRequirement> getItemRecommended()
	{
		ArrayList<ItemRequirement> reqs = new ArrayList<>();
		reqs.add(armourAndWeapon);
		return reqs;
	}

	@Override
	public List<String> getCombatRequirements()
	{
		return Collections.singletonList("Witch's experiment (level 19, 30, 42 and 53)");
	}

	@Override
	public List<Requirement> getGeneralRecommended()
	{
		return Collections.singletonList(new FreeInventorySlotRequirement(2));
	}

	@Override
	public QuestPointReward getQuestPointReward()
	{
		return new QuestPointReward(4);
	}

	@Override
	public List<ExperienceReward> getExperienceRewards()
	{
		return Collections.singletonList(new ExperienceReward(Skill.HITPOINTS, 6325));
	}

	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> allSteps = new ArrayList<>();
		allSteps.add(new PanelDetails("Start the quest", Collections.singletonList(talkToBoy), cheese, leatherGloves, armourAndWeapon));
		allSteps.add(new PanelDetails("Accessing the garden", Arrays.asList(getKey, enterHouse, goDownstairs, enterGate,
			openCupboardAndLoot, goBackUpstairs, useCheeseOnHole)));
		allSteps.add(new PanelDetails("Defeat the witch's experiment", Arrays.asList(searchFountain, enterShed, killWitchsExperiment, pickupBall, returnToBoy)));
		return allSteps;
	}
}
