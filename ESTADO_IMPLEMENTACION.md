# Estado de implementación

## Implementado

- Estructura Maven + plugin.yml.
- Arquitectura modular según GDD.
- ItemFactory con todos los items principales, materiales, seeds y productos.
- Recetas Early, Mid, Late, materiales, plantillas y Home Crystal.
- Smithing obligatorio para Mid/Late.
- UnlockManager con dependencias Early -> Mid -> Late.
- Recipe Book por discoverRecipes al desbloquear.
- CraftingGuard y bloqueo de Smithing.
- SetManager con cache por eventos.
- CombatModifierListener para daño recibido y daño causado.
- ProjectileListener para velocidad/daño de arcos/ballestas y recuperación de flechas de Arquero.
- PotionListener para duración de efectos y curación.
- DropManager para mobs/bloques/loot de bastion.
- FishingManager con rarezas.
- PlantManager con plantas registradas por ubicación.
- HomeManager, Home Crystal y TPA.

## Pendiente de test real en servidor

- Ajustes finos de eventos exactos de Paper 1.21.4, especialmente Smithing y LootGenerateEvent.
- Verificar si todos los materiales visuales de plantas son ideales en cada bioma.
- Ajustar porcentajes de balance con PvP real.
- Revisar si se necesita resource pack para CustomModelData.

## Decisión técnica

No se añadieron habilidades activas, GUI, economía, warps, spawn, back, claims ni sistemas RPG.
