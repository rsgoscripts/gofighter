

import java.awt.Color;
import java.awt.Graphics;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.powerbot.script.Condition;
import org.powerbot.script.MessageEvent;
import org.powerbot.script.MessageListener;
import org.powerbot.script.PaintListener;
import org.powerbot.script.PollingScript;
import org.powerbot.script.Script;
import org.powerbot.script.Tile;
import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.rt4.Constants;
import org.powerbot.script.rt4.Item;
import org.powerbot.script.rt4.Npc;

@Script.Manifest(name = "GOFighter", description = "Kill Anything Anywhere!")
public class GOFighter extends PollingScript<ClientContext> implements PaintListener, MessageListener {

	private Npc toAttack = null;

	public int STRstartXP = ctx.skills.experience(Constants.SKILLS_STRENGTH), STRcurrentXP = 0,
			STRstartLvl = ctx.skills.level(Constants.SKILLS_STRENGTH), STRcurrentLvl = 0, npcsKilled = 0, invCount = -1;

	public int ATKstartXP = ctx.skills.experience(Constants.SKILLS_ATTACK),
			ATKstartLvl = ctx.skills.level(Constants.SKILLS_ATTACK), ATKcurrentXP = 0, ATKcurrentLvl = 0;
	public int DEFstartXP = ctx.skills.experience(Constants.SKILLS_DEFENSE),
			DEFstartLvl = ctx.skills.level(Constants.SKILLS_DEFENSE), DEFcurrentXP = 0, DEFcurrentLvl = 0;

	public static int npcIds[] = null, foodId = -1, minHpToEat = -1, maxDist = -1, lootIds[] = null;
	private String npcName = null, currentState = "";
	private boolean npcFound = false, isAttacking = false, bonesToPeaches = false, eatFood = false, start = false,
			multiZone = false, npcKilled = false, goLoot = false;
	private long timeOut = 0, millis = -1;
	public STATES state = null;
	private GOGUI gui = new GOGUI();
	public static boolean lootEnabled = false;

	private Tile startLoc = null;

	private enum STATES {
		FINDING, ATTACKING, IN_COMBAT, LOOT, IDLE
	};

	@Override
	public void poll() {
		if (!start) {
			if (gui.confirmed) {
				if (foodId > -1) {
					eatFood = true;
				}
				startLoc = ctx.players.local().tile();
				gui.dispose();
				start = true;
				millis = System.currentTimeMillis();
			}
			return;
		}

		if (ctx.players.local().tile().distanceTo(startLoc) > maxDist && state != STATES.ATTACKING
				&& state != STATES.IN_COMBAT) {
			ctx.movement.step(startLoc);
			currentState = "Exceeded Attack Boundaries!";
			Condition.wait(new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					return !ctx.players.local().inMotion() && ctx.players.local().tile().distanceTo(startLoc) < 4;
				}
			});
		}
		if (eatFood) { // Unfinished
			if (foodId > -1) {
				if (ctx.players.local().health() <= minHpToEat && ctx.players.local().health() != -1) {
					if (ctx.inventory.select().id(foodId).count() > 0) {
						final Item foodItem = ctx.inventory.select().id(foodId).first().peek();
						foodItem.interact("Eat");
						currentState = "Eating Food!";
						Time.sleep(1250);
					}
				}
			}
		}

		if (bonesToPeaches) { // Unfinished
			if (state != STATES.ATTACKING || state != STATES.IN_COMBAT) {
			}
		}
		if (ctx.players.local().inCombat() && !isAttacking && toAttack != null) {
			npcFound = false;
			isAttacking = false;
		}
		if (timeOut > -1) {
			long time = (System.currentTimeMillis() - timeOut);
			if (time > 4999 && !ctx.players.local().inMotion()) {
				if (ctx.players.local().interacting().equals(toAttack))
					time = -1;
				else {
					currentState = "Timed Out...";
					reset();
					// Time.sleep(1250);
				}
			}
		}
		switch (getState()) {
		case LOOT:

			break;
		case FINDING:
			state = STATES.FINDING;
			// if (goLoot)
			// return;
			if (toAttack == null) {
				if (npcIds != null) {
					currentState = "Finding Monster...";
					toAttack = ctx.npcs.select().id(npcIds).nearest().poll();
					if (ctx.players.local().tile().distanceTo(toAttack.tile()) < 6) {
						if (!multiZone) {
							if (toAttack.inViewport() && !toAttack.inCombat()) {
								npcFound = true;
								currentState = "Monster Found!";
							} else if (!toAttack.inViewport() && !toAttack.inCombat()) {
								ctx.camera.turnTo(toAttack.tile());
								Time.sleep(750);
								npcFound = true;
								currentState = "Monster Found!";
							}
						} else {
							if (toAttack.inViewport()) {
								npcFound = true;
								currentState = "Monster Found!";
							} else if (!toAttack.inViewport()) {
								ctx.camera.turnTo(toAttack.tile());
								ctx.movement.step(toAttack.tile());
								Time.sleep(750);
								npcFound = true;
								currentState = "Monster Found!";
							}
						}
					} else {
						ctx.movement.step(toAttack.tile());
						Condition.wait(new Callable<Boolean>() {
							@Override
							public Boolean call() throws Exception {
								return !ctx.players.local().inMotion();
							}
						});
					}
				}
			}
			break;
		case ATTACKING:
			npcKilled = false;
			state = STATES.ATTACKING;
			currentState = "Attacking Monster!";
			timeOut = 1750;
			if (toAttack != null) {
				if (toAttack.inViewport()) {
					if (!toAttack.inCombat() && !multiZone) {
						toAttack.click("Attack");
						currentState = "Attacked Monster!";
						timeOut = System.currentTimeMillis();
						Time.sleep(1750);
						npcFound = false;
						isAttacking = true;
					} else if ((toAttack.inCombat() || !toAttack.inCombat()) && multiZone) {
						toAttack.click("Attack");
						currentState = "Attacked Monster!";
						timeOut = System.currentTimeMillis();
						Time.sleep(1750);
						npcFound = false;
						isAttacking = true;
					}
				} else {
					ctx.camera.turnTo(toAttack.tile());
				}
			} else
				npcFound = false;
			break;
		case IN_COMBAT:
			state = STATES.IN_COMBAT;
			if (ctx.players.local().inCombat()) {
				timeOut = -1;
				currentState = "In Combat!";
				if (toAttack.health() < 1) {
					currentState = "Killed Monster!";
					npcKilled = true;
					npcsKilled++;
					reset();
				}
			}
			break;
		case IDLE:
			state = STATES.IDLE;
			// currentState = "IDLE!"; Used for Dev. purposes
			reset();
			break;
		}

	}

	public void doAntiban() {

	}

	public void repaint(final Graphics g) {
		final long millis2 = System.currentTimeMillis() - millis;
		String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis2),
				TimeUnit.MILLISECONDS.toMinutes(millis2)
						- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis2)),
				TimeUnit.MILLISECONDS.toSeconds(millis2)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis2)));
		// System.out.println(hms);
		long second = (millis2 / 1000);
		long minute = (second / 60);
		long hour = (minute / 60);
		String runTime = "H" + hour + "M" + minute + "S" + second;
		g.setColor(Color.WHITE);
		STRcurrentXP = ctx.skills.experience(Constants.SKILLS_STRENGTH);
		STRcurrentLvl = ctx.skills.level(Constants.SKILLS_STRENGTH);
		ATKcurrentXP = ctx.skills.experience(Constants.SKILLS_ATTACK);
		ATKcurrentLvl = ctx.skills.level(Constants.SKILLS_ATTACK);
		DEFcurrentXP = ctx.skills.experience(Constants.SKILLS_DEFENSE);
		DEFcurrentLvl = ctx.skills.level(Constants.SKILLS_DEFENSE);
		g.drawString("Current State: " + currentState, 10, 270);
		final int str = (STRcurrentXP - STRstartXP), str2 = (STRcurrentLvl - STRstartLvl);
		final int atk = (ATKcurrentXP - ATKstartXP), atk2 = (ATKcurrentLvl - ATKstartLvl);
		final int def = (DEFcurrentXP - DEFstartXP), def2 = (DEFcurrentLvl - DEFstartLvl);
		if (str > 0)
			g.drawString("(STR) Levels Gained: " + str2 + " | XP Gained: " + str, 10, 290);
		else if (atk > 0)
			g.drawString("(ATK) Levels Gained: " + atk2 + " | XP Gained: " + atk, 10, 290);
		else if (def > 0)
			g.drawString("(DEF) Levels Gained: " + def2 + " | XP Gained: " + def, 10, 290);
		g.drawString("Monsters Killed: " + npcsKilled, 10, 310);
		g.drawString("RunTime: " + hms, 10, 330);
	}

	private String cock = "";

	public void onStart() {
		System.out.println("STARTED!");
	}

	public STATES getState() {
		if (toAttack == null && !npcFound && !isAttacking && !goLoot && (!ctx.players.local().inCombat() || npcKilled))
			return STATES.FINDING;
		else if (toAttack != null && npcFound && !isAttacking && (!ctx.players.local().inCombat() || npcKilled))
			return STATES.ATTACKING;
		else if (toAttack != null && !npcFound && isAttacking)
			return STATES.IN_COMBAT;
		return STATES.IDLE;
	}

	public void reset() {
		timeOut = -1;
		toAttack = null;
		npcFound = false;
		isAttacking = false;
		goLoot = false;
	}

	@Override
	public void messaged(final MessageEvent _event) {
		final String msg = _event.source();

		cock = msg;
	}

}
