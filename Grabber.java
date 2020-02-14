import java.awt.Color;
import java.awt.Graphics2D;

import org.osbot.rs07.api.filter.NameFilter;
import org.osbot.rs07.api.filter.PositionFilter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

@ScriptManifest(author = "Simon", info = "Grabs", logo = "", name = "TeleGraber", version = 1)
public class Grabber extends Script {

	Position banco = new Position(2946, 3368, 0);
	Position wine = new Position(2938, 3517, 1);
	Position tile = new Position(2939, 3517, 1);
	Position destiny = new Position(2939, 3517, 0);
	Area tele = new Area(2961, 3375, 2970, 3385);
	Area abajo = new Area(2930, 3515, 2930, 3516);
	GroundItem jugo;
	RS2Object esca;
	WebWalkEvent walk;
	WalkingEvent w;
	int pos = 1;
	int estado;
	int location;
	int agarrados = 0;
	long inicio;
	

	@Override
	public void onStart() throws InterruptedException {
		log("Checkeando Inventario");
		Chequeo();
		inicio = inventory.getAmount(245);
		super.onStart();
	}

	@SuppressWarnings({ "unchecked", })
	@Override
	public int onLoop() throws InterruptedException {
		// chequea Si el inventario no esta lleno de wines
		long actual = getInventory().getAmount(245);
		jugo = getGroundItems().closest(new PositionFilter<GroundItem>(wine),
				new NameFilter<GroundItem>("Wine of zamorak"));
		if (inventory.getAmount(245) < 26) {
			if (pos == 1) {
				if (myPosition().equals(tile)) {
					estado = 1;
					location = 1;
					magic.castSpell(Spells.NormalSpells.TELEKINETIC_GRAB);
					if (jugo != null && jugo.getPosition().equals(wine)) {
						estado = 2;
						jugo.interact("Cast");
						new ConditionalSleep(5000, 2500) {
							@Override
							public boolean condition() throws InterruptedException {
								return !(inventory.getAmount(245) > actual);
							}
						};
						return 2500;
					} else {
						if (actual > inicio)
							agarrados += (actual - inicio);
						inicio = actual;
					}
				} else {
					estado = 3;
					location = 4;
					if (!myPosition().equals(destiny)) {
						walk = new WebWalkEvent(destiny);
						walk.useSimplePath();
						execute(walk);
						new ConditionalSleep(20000, 3000) {
							@Override
							public boolean condition() throws InterruptedException {
								// TODO Auto-generated method stub
								return myPosition().equals(destiny);
							}
						}.sleep();
					} else {
						esca = getObjects().closest("Ladder");
						esca.interact("Climb");
					}
				}

			} else if (pos == 2) {
				//hacer algo abajo
			}
		} else if (inventory.getAmount(245) == 26) {
			estado = 3;
			location = 3;
			if (!myPosition().equals(banco)) {
				if (!tele.contains(myPlayer())) {
					magic.castSpell(Spells.NormalSpells.FALADOR_TELEPORT);
					new ConditionalSleep(6000, 1500) {
						@Override
						public boolean condition() throws InterruptedException {
							return myPlayer() != null && myPlayer().isOnScreen() && !myPlayer().isAnimating();
						}
					}.sleep();
				} else {
					w = new WalkingEvent(banco);
					w.setMinDistanceThreshold(0);
					w.setMiniMapDistanceThreshold(0);
					execute(w);
				}
			} else {
				bank.open();
				bank.depositAll(245);
				if (inventory.getAmount(245) == 0) {
					bank.close();
				}
			}
		}
		return 600;
	}

	@Override
	public void onPaint(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.drawString("En Inventario: " + inventory.getAmount(245), 10, 300);
		g.drawString("Vinos Agarrados: " + agarrados, 10, 312);
		if (estado == 1) {
			g.drawString("Estado Actual: Esperando", 10, 324);
		} else if (estado == 2) {
			g.drawString("Estado Actual: Agarrando", 10, 324);
		} else if (estado == 3) {
			g.drawString("Estado Actual: Caminando", 10, 324);
		}
		if (location == 1) {
			g.drawString("Locacion Actual: Zona de Vinos Arriba", 10, 336);
		} else if (location == 2) {
			g.drawString("Locacion Actual: Zona de Vinos Abajo", 10, 336);
		} else if (location == 3) {
			g.drawString("Locacion Actual: Zona de Banco", 10, 336);
		} else if (location == 4) {
			g.drawString("Locacion Actual: Caminando al banco", 10, 336);
		} else if (location == 5) {
			g.drawString("Locacion Actial: Caminando a Wines", 10, 336);
		}
		super.onPaint(g);
	}

	public void Chequeo() throws InterruptedException {
		if (!equipment.isWearingItem(EquipmentSlot.CHEST, "Zamorak monk top")
				|| !equipment.isWearingItem(EquipmentSlot.LEGS, "Zamorak monk bottom")
				|| !equipment.isWearingItem(EquipmentSlot.WEAPON, "Staff of air") || !inventory.contains("Law rune")
				|| !inventory.contains("Water rune")) {
			if (!myPosition().equals(banco)) {
				w = new WalkingEvent(banco);
				w.setMinDistanceThreshold(0);
				w.setMiniMapDistanceThreshold(0);
				execute(w);
				new ConditionalSleep(5000, 10) {
					@Override
					public boolean condition() throws InterruptedException {
						return myPosition().equals(banco);
					}
				}.sleep();
			}
			bank.open();
			new ConditionalSleep(2000, 5) {
				@Override
				public boolean condition() throws InterruptedException {
					return bank.isOpen();
				}
			};
			if (!equipment.isWearingItem(EquipmentSlot.CHEST, "Zamorak monk top")
					&& !inventory.contains("Zamorak monk top") && !bank.contains("Zamorak monk top")) {
				log("Comprar Zamorak monk top");
				stop();
			} else if (!equipment.isWearingItem(EquipmentSlot.LEGS, "Zamorak monk bottom")
					&& !inventory.contains("Zamorak monk bottom") && !bank.contains("Zamorak monk bottom")) {
				log("Comprar Zamorak monk bottom");
				stop();
			} else if (!equipment.isWearingItem(EquipmentSlot.WEAPON, "Staff of air")
					&& !inventory.contains("Staff of air") && !bank.contains("Staff of air")) {
				log("Comprar Staff of air");
				stop();
			} else if (!inventory.contains("Law rune") && !bank.contains("Law rune")) {
				log("Comprar Law runes");
				stop();
			} else if (!inventory.contains("Water rune") && !bank.contains("Water rune")) {
				log("Comprar Water runes");
				stop();
			}
			if(!inventory.contains("Law rune")) {
				bank.withdrawAll("Law rune");
			} new ConditionalSleep(2000, 1000) {
				@Override
				public boolean condition() throws InterruptedException {
					return inventory.contains("Law rune");
				}
			};
			if(!inventory.contains("Water rune")) {
				bank.withdrawAll("Water rune");
			} new ConditionalSleep(2000, 1000) {
				@Override
				public boolean condition() throws InterruptedException {
					return inventory.contains("Water rune");
				}
			};
			if(!equipment.isWearingItem(EquipmentSlot.CHEST, "Zamorak monk top")
					&& !inventory.contains("Zamorak monk top")) {
				bank.withdraw("Zamorak monk top", 1);
			} new ConditionalSleep(2000, 1000) {
				@Override
				public boolean condition() throws InterruptedException {
					return inventory.contains("Zamorak monk top");
				}
			};
			if(!equipment.isWearingItem(EquipmentSlot.LEGS, "Zamorak monk bottom")
					&& !inventory.contains("Zamorak monk bottom")) {
				bank.withdraw("Zamorak monk bottom", 1);
			} new ConditionalSleep(2000, 1000) {
				@Override
				public boolean condition() throws InterruptedException {
					return inventory.contains("Zamorak monk bottom");
				}
			};
			if(!equipment.isWearingItem(EquipmentSlot.WEAPON, "Staff of air")
					&& !inventory.contains("Staff of air")) {
				bank.withdraw("Staff of air", 1);
			} new ConditionalSleep(2000, 1000) {
				@Override
				public boolean condition() throws InterruptedException {
					return inventory.contains("Staff of air");
				}
			};
			bank.close();
			new ConditionalSleep(4000, 1000) {
				@Override
				public boolean condition() throws InterruptedException {
					return !bank.isOpen();
				}
			}.sleep();
			if(!equipment.isWearingItem(EquipmentSlot.CHEST, "Zamorak monk top")) {
				//inventory.interact("Wear", "Zamorak monk top");
				equipment.equip(EquipmentSlot.CHEST, "Zamorak monk top");
			} new ConditionalSleep(4000, 1000) {
				@Override
				public boolean condition() throws InterruptedException {
					return equipment.isWearingItem(EquipmentSlot.CHEST, "Zamorak monk top");
				}
			}.sleep();
			if(!equipment.isWearingItem(EquipmentSlot.LEGS, "Zamorak monk bottom")) {
				//inventory.interact("Wear", "Zamorak monk bottom");
				equipment.equip(EquipmentSlot.LEGS, "Zamorak monk bottom");
			} new ConditionalSleep(4000, 1000) {
				@Override
				public boolean condition() throws InterruptedException {
					return equipment.isWearingItem(EquipmentSlot.LEGS, "Zamorak monk bottom");
				}
			}.sleep();
			if(!equipment.isWearingItem(EquipmentSlot.WEAPON, "Staff of air")) {
				//inventory.interact("Wield", "Staff of air");
				equipment.equip(EquipmentSlot.WEAPON, "Staff of air");
			} new ConditionalSleep(4000, 1000) {
				@Override
				public boolean condition() throws InterruptedException {
					return equipment.isWearingItem(EquipmentSlot.WEAPON, "Staff of air");
				}
			}.sleep();
		} else {
			log("Listo para iniciar, Exitos");
		}
	}
}
